package net.nekocurit.x19.data.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.nekocurit.x19.data.X19Entity

@Serializable
data class X19Purchase(
    @SerialName("buy_type")
    val bugType: Int,
    @SerialName("entity_id")
    val orderId: ULong,
): X19Entity()