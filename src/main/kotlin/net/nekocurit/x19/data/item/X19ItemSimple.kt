package net.nekocurit.x19.data.item

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.nekocurit.utils.serializer.InstantLongSSerializer
import net.nekocurit.x19.api.getItemDetails
import net.nekocurit.x19.data.X19Entity
import kotlin.time.Instant

@Serializable
data class X19ItemSimple(
    val name: String,
    @SerialName("entity_id")
    val id: ULong,
    @SerialName("developer_name")
    val developerName: String,
    @SerialName("brief_summary")
    val simpleDescription: String,
    @SerialName("download_num")
    val totalDownload: ULong,
    @SerialName("like_num")
    val totalLikes: ULong,
    @SerialName("publish_time")
    @Serializable(with = InstantLongSSerializer::class)
    val publishAt: Instant,
    @SerialName("item_version")
    val version: String
): X19Entity() {
    suspend fun getFullDetails() = api.getItemDetails(id)
}