package net.nekocurit.x19

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.nekocurit.utils.json
import net.nekocurit.utils.nextString
import net.nekocurit.x19.data.RequestX19Authentication
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.ResponseX19UniSAuth
import net.nekocurit.x19.data.cookie.AbstractWPLauncherCookie
import net.nekocurit.x19.data.entity.X19AuthenticationEntity.Companion.asX19AuthenticationEntity
import net.nekocurit.x19.data.entity.X19LoginOtp
import net.nekocurit.x19.data.entity.X19LoginOtp.Companion.asX19LoginOtp
import net.nekocurit.utils.newPrivateHttpClient
import kotlin.random.Random

class WPLauncherAPI(
    val client: HttpClient,
) {
    companion object {
        val internal: HttpClientConfig<*>.() -> Unit = {
            install(ContentNegotiation) {
                json(json, contentType = ContentType.Any)
            }
            defaultRequest {
                userAgent("WPFLauncher/0.0.0.0")
            }
        }
        fun newInstance() = WPLauncherAPI(newPrivateHttpClient(internal))
        fun <T : HttpClientEngineConfig> newInstance(
            engine: HttpClientEngineFactory<T>,
            engineConfig: T.() -> Unit = { }
        ) = WPLauncherAPI(newPrivateHttpClient(engine, engineConfig, internal))
    }

    suspend fun uniCookie(cookie: AbstractWPLauncherCookie) {
        client.post("https://mgbsdk.matrix.netease.com/x19/sdk/uni_sauth") {
            contentType(ContentType.Application.Json)
            setBody(JsonObject(cookie.toCookie() + ("client_login_sn" to JsonPrimitive(Random.nextString(16)))))
        }
            .body<ResponseX19UniSAuth>()
            .also {
                require(it.isOk) {it.message }
            }
    }

    suspend fun loginCookie(cookie: AbstractWPLauncherCookie) = client.post("https://x19obtcore.nie.netease.com:8443/login-otp") {
        contentType(ContentType.Application.Json)
        setBody(cookie.toWrappedCookie())
    }
        .body<ResponseX19Base>()
        .throwOnNotOk()
        .asX19LoginOtp()

    suspend fun authentication(cookie: AbstractWPLauncherCookie, login: X19LoginOtp) = client.post("https://x19obtcore.nie.netease.com:8443/authentication-otp") {
        contentType(ContentType.Application.Json)
        setBody(RequestX19Authentication.fromLoginOtpRespond(cookie, login).generateRequestBody())
    }
        .let { NetEaseEncryptUtils.httpDecrypt(it.bodyAsBytes()) }
        .let { json.decodeFromString<ResponseX19Base>(it) }
        .throwOnNotOk()
        .asX19AuthenticationEntity()

    /**
     * 向自定义端点发送数据
     *
     * @parm path 路径
     */
    suspend fun get(path: String, url: String = "https://x19apigatewayobt.nie.netease.com") = client.get("$url$path")
}