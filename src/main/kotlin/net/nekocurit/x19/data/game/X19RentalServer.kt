package net.nekocurit.x19.data.game

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.nekocurit.x19.data.X19Entity

/**
 * @param id 这里不是启动器显示的服务器号, 而是内部 id
 * @param version 游戏版本 例如 `1.12.2`
 * @param name 对外显示的服务器号
 */
@Serializable
data class X19RentalServer(
    @SerialName("entity_id")
    val id: ULong,
    @SerialName("image_url")
    val avatarUrl: String,
    @SerialName("mc_version")
    val version: String,
    val name: String,
    @SerialName("owner_id")
    val ownerId: ULong,
    @SerialName("player_count")
    val playerCount: UInt,
    val pvp: Boolean,
    @SerialName("server_name")
    val displayName: String,
    @SerialName("server_type")
    val type: String,
    @SerialName("has_pwd")
    @Serializable(with = HasPasswordSerializer::class)
    val hasPassword: Boolean
): X19Entity() {

    object HasPasswordSerializer: KSerializer<Boolean> {
        override val descriptor = PrimitiveSerialDescriptor("HasPassword", PrimitiveKind.BOOLEAN)

        override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeInt(if (value) 1 else 0)
        override fun deserialize(decoder: Decoder) = decoder.decodeInt() == 1
    }
}