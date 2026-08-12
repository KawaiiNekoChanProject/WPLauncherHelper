package net.nekocurit.i4399

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import net.nekocurit.i4399.data.I4399Profile
import net.nekocurit.i4399.data.Request4399SetIdCardAndRealName
import net.nekocurit.i4399.data.RespondI4399ChangePasswordBase
import net.nekocurit.i4399.game_sdk.utils.decodeForms
import net.nekocurit.i4399.game_sdk.utils.toParameters
import net.nekocurit.i4399.state.State4399Captcha
import net.nekocurit.utils.json
import net.nekocurit.utils.newPrivateHttpClient
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class I4399(
    val client: HttpClient,
    var appId: String = "www_home"
) {
    companion object {
        val internal: HttpClientConfig<*>.() -> Unit = {
            install(HttpCookies)
            install(ContentNegotiation) {
                json(json)
            }
            defaultRequest {
                url(I4399_API)
            }
        }
        fun newInstance(appId: String = "www_home") = I4399(newPrivateHttpClient(internal), appId)
        fun <T : HttpClientEngineConfig> newInstance(
            engine: HttpClientEngineFactory<T>,
            engineConfig: T.() -> Unit = { },
            appId: String = "www_home"
        ) = I4399(newPrivateHttpClient(engine, engineConfig, internal), appId)
    }

    /**
     * 登陆账号
     *
     * @param username 用户名
     * @param password 密码
     * @param ocr 如果遇到图像识别挑战则调用此处获取结果
     * @param doRealName 如果账号实名过期则调用此处获取新的证件
     */
    suspend fun login(
        username: String,
        password: String,
        ocr: suspend (ByteArray) -> String,
        doRealName: suspend () -> Pair<String, String>
    ) {
        val forms1 = client.get("ptlogin/loginFrame.do?appId=www_home").decodeForms("""<a class="login_feedback"""")

        val captcha = client.get("ptlogin/verify.do?username=$username&appId=$appId&t=${Clock.System.now()}&inputWidth=iptw2&v=1")
            .bodyAsText()
            .let { Regex("""javascript:UniLoginChangPIC\('(.*?)'\)""").find(it)?.groupValues?.get(1) }
            ?.let { session ->
                val img = client.get("ptlogin/captcha.do?captchaId=$session").bodyAsBytes()
                return@let State4399Captcha(session, ocr(img))
            }

        forms1["username"] = username
        forms1["password"] = I4399EncryptUtils.encrypt(password)
        forms1["autoLogin"] = "on"
        captcha?.also { captcha ->
            forms1["sessionId"] = captcha.session
            forms1["inputCaptcha"] = captcha.code
        }

        client.submitForm("ptlogin/login.do?v=1", forms1.toParameters())
            .also {
                val response = it.bodyAsText()

                @Suppress("SpellCheckingInspection")
                if (response.contains("needVerifyIdcard = false")) {
                    // 无实名自动完成实名
                    if (response.contains("needVerifyIdcard = true")) {
                        val realName = doRealName()

                        client.submitForm(
                            url = "ptlogin/setIdcardAndRealname.do",
                            formParameters = Request4399SetIdCardAndRealName(realName.first, realName.second).toParameters()
                        )
                    }
                } else {
                    if (response == "请稍后再试~") error("请求频率过快")
                    Regex("""<div[^>]*id="Msg"[^>]*>(.*?)</div>""")
                        .find(response)
                        ?.also { group -> error(group.groupValues[1]) }
                    Regex(""" eventHandles.__errorCallback\('(.*?)'\);""")
                        .find(response)
                        ?.also { group -> error(group.groupValues[1]) }
                }
            }
    }

    /**
     * 创建账号并登录
     *
     * @param username 用户名
     * @param password 密码
     * @param personal 实名信息
     * @param ocr 如果遇到图像识别挑战则调用此处获取结果
     */
    suspend fun register(
        username: String,
        password: String,
        personal: Pair<String, String>,
        ocr: suspend (ByteArray) -> String
    ) {
        val respond = client.get("ptlogin/regFrame.do?regMode=reg_normal&appId=$appId")
        delay(3.seconds)
        val forms1 = respond.decodeForms("""<div id="Msg" class="login_hor login_err_tip">""")
        val captcha = respond.bodyAsText()
            .let { Regex("""javascript:UniLoginChangPIC\('(.*?)'\)""").find(it)?.groupValues?.get(1) }
            ?.let { session ->
                val img = client.get("ptlogin/captcha.do?captchaId=$session").bodyAsBytes()
                return@let State4399Captcha(session, ocr(img))
            }

        if (client.get("ptlogin/isExist.do?username=$username&appId=$appId&regMode=reg_normal&v=1").bodyAsText() != "0") error("用户名已被占用")

        forms1["username"] = username
        forms1["password"] = I4399EncryptUtils.encrypt(password)
        forms1["passwordveri"] = I4399EncryptUtils.encrypt(password)
        forms1["realname"] = I4399EncryptUtils.encrypt(personal.first)
        forms1["idcard"] = I4399EncryptUtils.encrypt(personal.second)
        forms1["reg_eula_agree"] = "on"
        forms1["autoLogin"] = "on"

        captcha?.also { captcha ->
            forms1["sessionId"] = captcha.session
            forms1["inputCaptcha"] = captcha.code
        }

        client.submitForm("ptlogin/register.do", forms1.toParameters())
            .bodyAsText()
            .also { text ->
                if (text.contains("请稍后再试~")) error("风控拦截")
                Regex("""<div class="login_error">\s*<strong>(.+)<br>&nbsp;</strong>\s*</div>""")
                    .find(text)
                    ?.groupValues[1]
                    ?.also { error(it) }
                Regex("""<div[^>]*id="Msg"[^>]*class="login_hor login_err_tip"[^>]*>\s*(.*?)\s*</div>""")
                    .find(text)
                    ?.groupValues[1]
                    ?.also { error(it) }
            }
    }


    /**
     * 获取当前登录账号的 4399 资料
     */
    suspend fun getProfile() = I4399Profile(client.get("https://u.4399.com/profile/index.html").bodyAsText())

    /**
     * 更新 4399 资料
     *
     * @param nick 昵称
     * @param sex 性别 false=男 true=女
     * @param year 生日/年
     * @param month 生日/月
     * @param day 生日/日
     * @param province 所在省
     * @param city 所在城市
     */
    suspend fun updateProfile(nick: String, sex: Boolean = false, year: UInt = 2000U, month: UInt = 11U, day: UInt = 11U, province: String = "北京", city: String = "东城") {
        require(month in 1U..12U) { "Month should be between 1 and 12" }
        require(day in 1U..31U) { "Day should be between 1 and 31" }

        val hash = client.get("https://u.4399.com/profile/modify.html")
            .bodyAsText()
            .let { Regex("""<input\s+type="hidden"\s+name="__HASH__"\s+value="([^"]+)"""").find(it)?.groupValues[1] }
            ?: error("no hash")

        client.submitForm(
            url = "https://u.4399.com/profile/modify-save.html",
            formParameters = Parameters.build {
                append("__HASH__", hash)
                append("birthday", "$year-$month-$day")
                append("nick", nick)
                append("sex", if (sex) "2" else "1")
                append("bir_year", year.toString())
                append("bir_month", month.toString())
                append("bir_day", day.toString())
                append("local_province", province)
                append("local_city", city)
                append("qq", "")
            }
        )
    }

    /**
     * 更改账号密码 更改后会从所有设备退出登录
     *
     * @param old 旧密码
     * @param new 新密码
     */
    suspend fun changePassword(old: String, new: String) {
        client.submitForm(
            url = "https://u.4399.com/security/service/security/verifyConfirm",
            formParameters = Parameters.build {
                append("type", "pwd")
                append("code", old)
            }
        ).body<RespondI4399ChangePasswordBase>().checkError()
        client.submitForm(
            url = "https://u.4399.com/security/service/security/pwd",
            formParameters = Parameters.build {
                append("pwd", I4399EncryptUtils.encrypt2(new))
                append("pwdC", I4399EncryptUtils.encrypt2(new))
                append("ver", "2")
            }
        ).body<RespondI4399ChangePasswordBase>().checkError()
    }

    /**
     * 登出账号
     */
    suspend fun logout() {
        client.get("https://u.4399.com/signout.html")
    }
}