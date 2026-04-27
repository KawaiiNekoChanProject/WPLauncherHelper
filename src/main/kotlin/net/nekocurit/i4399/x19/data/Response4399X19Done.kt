package net.nekocurit.i4399.x19.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import net.nekocurit.utils.json

@Serializable
data class Response4399X19Done(
    val code: Int,
    @SerialName("result")
    private val rawResult: JsonElement,
    val message: String
) {

    fun checkState() = this
        .apply {
            if (code != 100) error(message)
        }

    val result
        get() = json.decodeFromJsonElement<Result>(rawResult)

    @Serializable
    data class Result(
        val uid: ULong,
        val state: String
    )
}