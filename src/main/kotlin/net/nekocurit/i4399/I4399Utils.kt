package net.nekocurit.i4399

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.nekocurit.i4399.state.State4399Captcha
import net.nekocurit.utils.nextString
import kotlin.random.Random

object I4399Utils {

    const val APP_ID = "kid_wdsj"

    suspend fun requestSession() = HttpClient {
        defaultRequest {
            url("https://ptlogin.4399.com/")
            header(HttpHeaders.UserAgent, Random.nextString(16))
        }
    }
        .get("ptlogin/regFrame.do?regMode=reg_normal&appId=$APP_ID")
        .headers
        .getAll(HttpHeaders.SetCookie)
        ?.joinToString("; ") { it.split(";")[0] }
        ?: error("请求会话失败")

    suspend fun register(
        username: String,
        password: String,
        personal: Pair<String, String>,
        session: String? = null,
        ocr: suspend (ByteArray) -> String
    ) {
        val session = session ?: requestSession()
        val client = HttpClient {
            defaultRequest {
                url("https://ptlogin.4399.com/")
                header(HttpHeaders.UserAgent, Random.nextString(16))
                header(HttpHeaders.Cookie, session)
            }
        }

        val captcha = client.get("ptlogin/regFrame.do?regMode=reg_normal&appId=$APP_ID")
            .bodyAsText()
            .let { Regex("""javascript:UniLoginChangPIC\('(.*?)'\)""").find(it)?.groupValues?.get(1) }
            ?.let { session ->
                val img = client.get("ptlogin/captcha.do?captchaId=$session").bodyAsBytes()
                return@let State4399Captcha(session, ocr(img))
            }

        if (client.get("ptlogin/isExist.do?username=$username&appId=$APP_ID&regMode=reg_normal&v=1").bodyAsText() != "0") error("用户名已被占用")

        client.submitForm(
            url = "ptlogin/register.do",
            formParameters = Parameters.build {
                append("appId", APP_ID)
                append("regMode", "reg_normal")
                append("regIdcard", "true")
                append("mainDivId", "popup_reg_div")
                append("showRegInfo", "true")
                append("includeFcmInfo", "false")
                append("expandFcmInput", "true")
                append("fcmFakeValidate", "false")
                append("realnameValidate", "true")
                append("userNameLabel", "4399用户名")
                append("level", "4")
                append("sec", "1")
                append("password", I4399EncryptUtils.encrypt(password))
                append("passwordveri", I4399EncryptUtils.encrypt(password))
                append("realname", I4399EncryptUtils.encrypt(personal.first))
                append("idcard", I4399EncryptUtils.encrypt(personal.second))
                append("username", username)
                append("reg_eula_agree", "on")
                append("autoLogin", "on")
                captcha?.also { captcha ->
                    append("sessionId", captcha.session)
                    append("inputCaptcha", captcha.code)
                }
            }
        )
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


}