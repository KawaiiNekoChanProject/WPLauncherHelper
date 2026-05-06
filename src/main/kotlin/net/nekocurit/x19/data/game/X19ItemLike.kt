package net.nekocurit.x19.data.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.nekocurit.x19.data.X19Entity

@Serializable
data class X19ItemLike(
    @SerialName("entity_id")
    val id: ULong,
    @SerialName("item_id")
    val itemId: ULong,
    @SerialName("has_like")
    private val rawHasLike: Int
): X19Entity() {
    val like
        get() = rawHasLike
            .takeIf { it != 0 }
            ?.let { it == 1 }
}