package net.nekocurit.x19.data.image

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.nekocurit.x19.data.X19AuthEntity
import net.nekocurit.x19_bucket.X19BucketSession

/**
 * @param token 上传凭据
 * @param url 上传端点 **这个不是图片直链**
 */
@Suppress("SpellCheckingInspection")
@Serializable
data class X19ImageUpload(
    val token: String,
    val url: String
): X19AuthEntity() {

    @Serializable
    data class ImageMetadata(
        @SerialName("fsize")
        val fileSize: ULong,
        val md5: String,
        val mime: String,
        @SerialName("picSize")
        val size: List<UInt>,
        val url: String
    )

    /**
     * 上传图片
     *
     * @param image 图片数据
     */
    suspend fun upload(image: ByteArray) = api.client.post(url) {
        header(HttpHeaders.Authorization, token)
        setBody(image)
    }
        .body<ImageMetadata>()

    fun toBucketSession() = X19BucketSession.parse(token)
}