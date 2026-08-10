package net.nekocurit.i4399.game_sdk.entity

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable
import net.nekocurit.i4399.game_sdk.I4399GameSDKAPI

@Serializable
data class I4399GameSDKCaptcha(
    val id: String,
    val code: String
) {
    companion object {

        suspend fun Map<String, String>.doCaptcha(api: I4399GameSDKAPI, block: suspend (ByteArray) -> String) = this["captcha_id"]
            ?.let { newInstance(api, it, block) }

        suspend fun newInstance(api: I4399GameSDKAPI, id: String, block: suspend (ByteArray) -> String) = I4399GameSDKCaptcha(
            id = id,
            code = block(api.client.get("/ptlogin/captcha.do?captchaId=$id").bodyAsBytes())
        )

        fun MutableMap<String, String>.appendCaptcha(entity: I4399GameSDKCaptcha?) = apply {
            entity?.also { entity ->
                this["captcha_id"] = entity.id
                this["captcha"] = entity.code
            }
        }
        fun ParametersBuilder.appendCaptcha(entity: I4399GameSDKCaptcha?) = apply {
            entity?.also { entity ->
                append("captcha_id", entity.id)
                append("captcha", entity.code)
            }
        }
    }
}