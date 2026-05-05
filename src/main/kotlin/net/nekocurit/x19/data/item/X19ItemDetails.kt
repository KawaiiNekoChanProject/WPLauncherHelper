package net.nekocurit.x19.data.item

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.nekocurit.utils.serializer.InstantLongSSerializer
import net.nekocurit.x19.data.X19Entity
import kotlin.time.Instant

/**
 * @param currentOnline 只有组件类型为 网络服务器 时才适用
 */
@Serializable
data class X19ItemDetails(
    val name: String,
    @SerialName("brief_image_urls")
    val wallpaperUrls: List<String>,
    @SerialName("entity_id")
    val id: ULong,
    @SerialName("brief_summary")
    val simpleDescription: String,
    @SerialName("detail_description")
    val fullDescription: String,
    @SerialName("developer_name")
    private val rawDeveloperName: String,
    @SerialName("developer_urs")
    private val rawDeveloperEmail: String,
    @SerialName("download_num")
    val totalDownload: ULong,
    @SerialName("edit_time")
    @Serializable(with = InstantLongSSerializer::class)
    val editAt: Instant,
    @SerialName("publish_time")
    @Serializable(with = InstantLongSSerializer::class)
    val publishAt: Instant,
    @SerialName("like_num")
    val totalLikes: ULong,
    @SerialName("recommend_tag")
    val recommendTag: String,
    @SerialName("recommend_total")
    val recommendTotal: ULong,
    @SerialName("recommend_ratio")
    val recommendRatio: Double,
    @SerialName("network_tag")
    val networkTag: String,
    @SerialName("online_count")
    val currentOnline: ULong = 0UL,
    @SerialName("tag_list")
    val otherTags: List<Tag>
): X19Entity() {

    val developer
        get() = Developer(rawDeveloperName, rawDeveloperEmail)

    @Serializable
    data class Developer(
        val name: String,
        val email: String
    )

    @Serializable
    data class Tag(
        val tid: Int,
        @Suppress("SpellCheckingInspection")
        @SerialName("itype")
        val type: Int,
        val name: String
    )
}