package net.nekocurit.x19.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("SpellCheckingInspection")
@Serializable
data class ResponseX19UniSAuth(
    val code: Int,
    @SerialName("msg")
    val message: String = "",
    @SerialName("subcode")
    val subCode: Int,
    val aid: ULong = 0UL,
    @SerialName("sdkuid")
    val sdkUid: String = "",
    @SerialName("first_login_platform")
    val platformFirstLogin: String = ""
) {
    val isOk
        get() = code == 200
}