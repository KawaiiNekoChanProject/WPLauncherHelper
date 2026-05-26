package net.nekocurit.x19

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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
import kotlin.random.Random

object WPLauncherAPI {

    val client = HttpClient(Java) {
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 15_000
        }
        install(ContentNegotiation) {
            json(json, contentType = ContentType.Any)
        }
        defaultRequest {
            header("User-Agent", "WPFLauncher/0.0.0.0")
        }
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
        setBody(RequestX19Authentication(cookie, login).generateRequestBody())
    }
        .let { NetEaseEncryptUtils.httpDecrypt(it.bodyAsBytes()) }
        .let { json.decodeFromString<ResponseX19Base>(it) }
        .throwOnNotOk()
        .asX19AuthenticationEntity()
}
