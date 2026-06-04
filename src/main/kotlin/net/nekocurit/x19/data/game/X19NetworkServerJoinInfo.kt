package net.nekocurit.x19.data.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.nekocurit.x19.data.X19Entity

@Serializable
data class X19NetworkServerJoinInfo(
    @SerialName("entity_id")
    val id: ULong,
    @SerialName("isp_enable")
    val ispEnable: Boolean,
    @SerialName("ip")
    val host: String,
    val port: Int
): X19Entity() {
    val address
        get() = "$host:$port"
}