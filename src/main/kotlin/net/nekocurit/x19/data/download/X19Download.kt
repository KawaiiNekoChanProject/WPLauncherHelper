package net.nekocurit.x19.data.download

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.nekocurit.x19.data.X19Entity

@Serializable
data class X19Download(
    @SerialName("entity_id")
    val id: ULong,
    @SerialName("item_id")
    val itemId: ULong,
    @SerialName("user_id")
    val userId: ULong,
    @SerialName("sub_entities")
    val components: List<X19DownloadComponent>,
    @SerialName("download_time")
    val downloadTime: ULong
): X19Entity()