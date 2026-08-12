@file:Suppress("SpellCheckingInspection")

package net.nekocurit.i4399.game_sdk

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import net.nekocurit.i4399.I4399_API_URL
import net.nekocurit.i4399.I4399EncryptUtils
import net.nekocurit.i4399.I4399_API
import net.nekocurit.i4399.game_sdk.config.Config
import net.nekocurit.i4399.game_sdk.entity.I4399GameSDKCaptcha.Companion.appendCaptcha
import net.nekocurit.i4399.game_sdk.entity.I4399GameSDKCaptcha.Companion.doCaptcha
import net.nekocurit.i4399.game_sdk.entity.I4399GameSDKOauthSession
import net.nekocurit.i4399.game_sdk.entity.I4399GameSDKOauthSession.Companion.sessionNotNull
import net.nekocurit.i4399.game_sdk.utils.decodeForms
import net.nekocurit.i4399.game_sdk.utils.toParameters
import net.nekocurit.i4399.x19.data.Response4399X19Done
import net.nekocurit.i4399.x19.data.Response4399X19Oauth
import net.nekocurit.utils.newPrivateHttpClient
import net.nekocurit.utils.nextString
import net.nekocurit.x19.data.cookie.WPLauncherCookie4399Com
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class I4399GameSDKAPI(
    val config: Config,
    var session: I4399GameSDKOauthSession? = null,
    val client: HttpClient
) {
    companion object {
        val internal: HttpClientConfig<*>.() -> Unit = {
            install(HttpCookies)
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true }, contentType = ContentType.Any)
            }
            defaultRequest {
                url(I4399_API)
                // TODO: 实现 kotlin 版 fake-useragent
                header(HttpHeaders.UserAgent, "Mozilla/5.0 ${Random.nextString(16)}/${Random.nextString(16)}")
            }
        }
        fun newInstance(config: Config) = I4399GameSDKAPI(config, client = newPrivateHttpClient(internal))
        fun <T : HttpClientEngineConfig> newInstance(
            config: Config,
            engine: HttpClientEngineFactory<T>,
            engineConfig: T.() -> Unit = { }
        ) = I4399GameSDKAPI(config, client = newPrivateHttpClient(engine, engineConfig, internal))
    }

    /**
     * 生成 Oauth url
     * 此链接可直接在浏览器中使用模拟登陆
     *
     * @param device 设备码
     */
    suspend fun generateOauthUrl(device: String = "") = client.submitForm( // 此接口返回的是 json  但是 Content-Type 不对
        url = "https://m.4399api.com/openapiv2/oauth.html",
        formParameters = Parameters.build {
            append("usernames", "")
            append("top_bar", "1")
            append("state", "")
            append("device", config.generateDeviceRaw(device))
        }
    )
        .body<Response4399X19Oauth>()
        .result.loginUrl

    /**
     * 注册 Oauth session
     *
     * @param device 设备码
     */
    suspend fun registerDevice(device: String = "") = I4399GameSDKOauthSession.fromOauthRespond(client.get(generateOauthUrl(device)))
        .also { session = it }

    /**
     * 登陆账号
     *
     * @param username 用户名
     * @param password 密码
     * @param onCaptcha 验证码处理逻辑
     * @param onRealName 如果账号没有完成实名认证, 会从此处获取用于完成的信息
     */
    suspend fun login(
        username: String,
        password: String,
        onCaptcha: suspend (ByteArray) -> String,
        onRealName: suspend () -> Pair<String, String>
    ): WPLauncherCookie4399Com {
        val preFullSession = client.cookies(I4399_API_URL).count() > 2
        val forms1 = sessionNotNull.forms.toMutableMap()
        forms1["auth_action"] = "ORILOGIN"
        val forms2 = client.submitForm("/oauth2/authorize.do?channel=&sdk=op", formParameters = forms1.toParameters()).decodeForms()

        val captcha = forms2.doCaptcha(this, onCaptcha)

        forms2["username"] = username
        forms2["password"] = password
        forms2.appendCaptcha(captcha)

        return client.submitForm(url = "/oauth2/loginAndAuthorize.do?channel=&sdk=op", formParameters = forms2.toParameters())
            .let { respondLogin ->
                when (respondLogin.status) {
                    HttpStatusCode.OK -> respondLogin.bodyAsText()
                        .let login@{ text ->
                            // 通常失败提示
                            Regex("""<p\s+class="warning_tips\s+global_ico"\s+id="login_err_msg">\s*(.*?)\s*</p>""")
                                .find(text)
                                ?.groupValues[1]
                                ?.also { error(it) }

                            Regex("""<p\s+class="ipt_tips ipt_tips_err global_ico"\s+id="captcha1_err_msg">\s*(.*?)\s*</p>""")
                                .find(text)
                                ?.groupValues[1]
                                ?.also { error(it) }

                            // 需要完成账号实名认证
                            if (text.contains("/oauth2/setIdcardAndRealname.do")) {
                                val forms2 = respondLogin.decodeForms("""<div class="input_wrap_id finput_wrap">""")
                                onRealName().also { (name, card) ->
                                    forms2["realname"] = name
                                    forms2["idcard" ] = card
                                }

                                client.submitForm(url = "/oauth2/setIdcardAndRealname.do", formParameters = forms2.toParameters())
                                    .let realname@{ respondRealName ->
                                        when (respondRealName.status) {
                                            HttpStatusCode.Found -> return@login client.get(respondRealName.headers[HttpHeaders.Location]!!)
                                            else -> error("实名信息设置失败")
                                        }
                                    }
                            }

                            error("未知错误")
                        }
                    HttpStatusCode.Found -> client.get(respondLogin.headers[HttpHeaders.Location]!!)
                    else -> error("状态异常")
                }
            }
            .body<Response4399X19Done>()
            .also {
                // 由于 4399 问题, 当本地无完整 cookie 时 远程端无法重定向到实名认证界面, 需要再登陆一次
                if (it.message == "该账号异常，无法登录" && !preFullSession) return login(username, password, onCaptcha, onRealName)
            }
            .checkState()
            .let { WPLauncherCookie4399Com(it.result.uid, it.result.state) }
    }

    /**
     * 注册账号
     *
     * @param username 用户名
     * @param password 密码
     * @param personal 证件信息
     * @param onCaptcha 验证码处理逻辑
     */
    suspend fun register(
        username: String,
        password: String,
        personal: Pair<String, String>,
        onCaptcha: suspend (ByteArray) -> String
    ): WPLauncherCookie4399Com {
        val forms1 = sessionNotNull.forms.toMutableMap()
        forms1["auth_action"] = "register"
        forms1["reg_mode"] = "reg_normal"

        val forms2 = client.submitForm("/oauth2/authorize.do?channel=&sdk=op", formParameters = forms1.toParameters()).decodeForms()

        val captcha = forms2.doCaptcha(this, onCaptcha)

        forms2["username"] = username
        forms2["password"] = I4399EncryptUtils.encrypt(password)
        forms2.appendCaptcha(captcha)

        delay(5.seconds) // 4399有间隔检测或者是生成的会话凭据尚未被写入redis(仅为猜测) 不进行延迟无法注册

        val respondStep1 = client.submitForm(url = "/oauth2/registerAndAuthorize.do", formParameters = forms2.toParameters())
        when (respondStep1.status) {
            // 302 重定向到 oauth 重定向链接
            // 但此时并不代表能重新登录, 再次调用 `login` 方法完成实名认证
            HttpStatusCode.Found -> return login(username, password, onCaptcha) { personal }
            else -> {
                Regex("""<p class="ipt_tips ipt_tips_err global_ico" id="username1_err_msg">([\s\S]*?)</p>""")
                    .find(respondStep1.bodyAsText())
                    ?.also { error("创建账号失败: ${it.groupValues[1].trim()}") }
                Regex("""<p class="ipt_tips ipt_tips_warn">(.+?)</p>""")
                    .find(respondStep1.bodyAsText())
                    ?.also { error("创建账号失败: ${it.groupValues[1]}") }
                Regex("""<p>(.+?)</p>""")
                    .find(respondStep1.bodyAsText())
                    ?.also { error("创建账号失败: ${it.groupValues[1]}") }

                error("创建账号失败: ${respondStep1.bodyAsText().trim()}")
            }
        }
    }

}