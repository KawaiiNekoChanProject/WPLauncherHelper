package net.nekocurit.x19.data.cookie

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import net.nekocurit.utils.json

abstract class AbstractWPLauncherCookie {
    companion object {
        @Serializable
        data class Wrapped(
            @SerialName("sauth_json")
            val wrapped: String,
        ) {
            constructor(wrapped: AbstractWPLauncherCookie): this(wrapped.toCookie().toString())
        }
    }

    abstract fun getUdId(): String
    abstract fun toCookie(): JsonObject
    open fun toWrappedCookie(): JsonObject = json.encodeToJsonElement(Wrapped(this)).jsonObject

}