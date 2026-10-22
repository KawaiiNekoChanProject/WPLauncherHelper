package net.nekocurit.x19_bucket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param imageSize 图片尺寸, 只有上传类型为图片时才存在
 */
@Serializable
data class X19BucketFile(
    @SerialName("fsize")
    val fileSize: ULong,
    val md5: String,
    val mime: String,
    @SerialName("picSize")
    val imageSize: List<UInt> = emptyList(),
    val url: String
)