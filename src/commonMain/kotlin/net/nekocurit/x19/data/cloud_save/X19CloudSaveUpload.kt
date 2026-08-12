package net.nekocurit.x19.data.cloud_save

import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.nekocurit.x19.data.X19AuthEntity

@Suppress("SpellCheckingInspection")
@Serializable
data class X19CloudSaveUpload(
    @SerialName("entity_id")
    val id: ULong,
    @SerialName("partnum")
    val part: UInt,
    val url: String,
    @SerialName("active_time")
    val activeTime: UInt
): X19AuthEntity() {
    suspend fun upload(data: ByteArray) {
        api.client.put(url) {
            header(HttpHeaders.ContentType, "application/x-7z-compressed")
            header(HttpHeaders.Expect, "100-continue")
            setBody(data)
        }
    }
}