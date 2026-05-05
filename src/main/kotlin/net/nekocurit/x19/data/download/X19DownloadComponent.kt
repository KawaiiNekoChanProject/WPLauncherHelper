package net.nekocurit.x19.data.download

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.nekocurit.x19.data.X19Entity

@Serializable
data class X19DownloadComponent(
    @SerialName("entity_id")
    val id: ULong,
    @SerialName("res_url")
    val resourceUrl: String,
    @SerialName("res_size")
    val resourceSize: ULong,
    @SerialName("res_md5")
    val resourceMd5: String,
    @SerialName("res_name")
    val resourceName: String,
    @SerialName("res_version")
    val resourceVersion: UInt,
    @SerialName("java_version")
    val javaVersion: UInt,
    @SerialName("jar_md5")
    val jarMd5: String,
    @SerialName("mc_version_name")
    val mcVersion: String,
): X19Entity()