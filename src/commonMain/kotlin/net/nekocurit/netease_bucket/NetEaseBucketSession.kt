package net.nekocurit.netease_bucket

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.content.ProgressListener
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteReadChannel
import korlibs.encoding.Base64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import net.nekocurit.utils.json
import net.nekocurit.utils.newPrivateHttpClient
import net.nekocurit.utils.serializer.InstantLongSSerializer
import net.nekocurit.x19.WPLauncherAPI
import kotlin.time.Instant

/**
 * NetEase 对象存储
 */
data class NetEaseBucketSession(
    val client: HttpClient,
    val signature: String,
    val rawPolicy: String
) {
    val policy = json.decodeFromString<Policy>(Base64.decode(rawPolicy).decodeToString())

    companion object {
        /**
         * @param timestamp 凭据生成时间
         * @param url 上传/访问端点
         * @param https 是否使用安全的HTTPS
         * @param uploadLimit 上传文件数量限制
         * @param removeMetadata 是否删除元素据
         * @param uid 用户ID
         * @param sizeLimit 文件尺寸限制
         * @param typeLimit 文件类型限制
         * @param review 是否需要审核
         * @param extraInfo 用户扩展信息
         */
        @Serializable
        data class Policy(
            @Serializable(with = InstantLongSSerializer::class)
            val timestamp: Instant,
            val method: String,
            val url: String,
            val https: Boolean,
            val uploadLimit: Int = 0,
            @SerialName("rmMeta")
            val removeMetadata: Boolean = false,
            val uid: String,
            @SerialName("fsizeLimit")
            val sizeLimit: List<Long>,
            @SerialName("mimeLimit")
            val typeLimit: List<String>,
            val review: Int = 0,
            val extraInfo: JsonElement = JsonNull
        ) {
            init {
                require(sizeLimit.size == 2) { "sizeLimit 长度错误, 应为2(最小限制与最大限制)" }
            }
        }

        fun parse(token: String): NetEaseBucketSession {
            val token = token.removePrefix("Policy ").split(":")
            require(token.size == 2) { "token 格式错误" }

            return NetEaseBucketSession(newPrivateHttpClient(WPLauncherAPI.internal), token[0], token[1])
        }

    }

    fun generateAuthorizationHeader() = buildString {
        append("Policy ")
        append(signature)
        append(":")
        append(rawPolicy)
    }

    /**
     * 上传文件
     *
     * 除非你知道在做什么, 否则你应该使用其它函数完成上传
     *
     * @param onUpload 上传进度回调
     * @param block 自己指定上传文件来源
     */
    suspend fun upload(
        onUpload: ProgressListener? = null,
        block: HttpRequestBuilder.() -> Unit
    ) = client.post(policy.url) {
        header(HttpHeaders.Authorization, generateAuthorizationHeader())
        block()
        onUpload(onUpload)
    }
        .apply {
            val body = bodyAsText()
            require(body.startsWith("{")) { body }
        }
        .body<X19BucketFile>()

    /**
     * 上传文件
     *
     * @param onUpload 上传进度回调, 注意 `contentLength` 参数恒定返回 null
     */
    suspend fun upload(
        channel: ByteReadChannel,
        onUpload: ProgressListener? = null
    ) = upload(onUpload) {
        setBody(channel)
    }

    /**
     * 上传文件
     *
     * 如果 IDE 函数签名识别错误, 请按照这个风格写
     *
     * ```kotlin
     * upload(raw) { bytesSentTotal, contentLength ->  }
     * ```
     *
     * @param onUpload 上传进度回调
     */
    suspend fun upload(
        raw: ByteArray,
        onUpload: ProgressListener? = null
    ) = upload(onUpload) {
        setBody(raw)
    }
}