package net.nekocurit.x19_bucket

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
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
 * X19 对象存储
 */
data class X19BucketSession(
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
            val uploadLimit: Int,
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

        fun parse(token: String): X19BucketSession {
            val token = token.removePrefix("Policy ").split(":")
            require(token.size == 2) { "token 格式错误" }

            return X19BucketSession(newPrivateHttpClient(WPLauncherAPI.internal), token[0], token[1])
        }

    }

    fun generateAuthorizationHeader() = buildString {
        append("Policy ")
        append(signature)
        append(":")
        append(rawPolicy)
    }

    suspend fun upload(raw: ByteArray) = client.post(policy.url) {
        header(HttpHeaders.Authorization, generateAuthorizationHeader())
        setBody(raw)
    }
        .apply {
            val body = bodyAsText()
            require(body.startsWith("{")) { body }
        }
        .body<X19BucketFile>()
}