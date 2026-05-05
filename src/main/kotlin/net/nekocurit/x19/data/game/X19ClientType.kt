package net.nekocurit.x19.data.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.nekocurit.utils.json

@Serializable
enum class X19ClientType {
    @SerialName("java")
    Java,
    @SerialName("cpp")
    Bedrock,
    /**
     * 旧全局
     */
    @SerialName("all")
    All;

    val id by lazy { json.encodeToString(this).replace("\"", "") }
}