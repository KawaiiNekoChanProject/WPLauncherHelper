package net.nekocurit.i4399.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

@Serializable
class RespondI4399ChangePasswordBase(
    @SerialName("c")
    val code: Int,
    @SerialName("d")
    val details: JsonElement = JsonNull,
    @SerialName("e")
    val message: String = ""
) {
    fun checkError() = apply {
        if (!isOk()) error(message)
    }
    fun isOk() = code == 200
}