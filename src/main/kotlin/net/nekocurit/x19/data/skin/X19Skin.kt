package net.nekocurit.x19.data.skin

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.nekocurit.x19.data.X19Entity
import net.nekocurit.x19.data.game.X19ClientType
import net.nekocurit.x19.data.game.X19GameType

@Serializable
data class X19Skin(
    @SerialName("user_id")
    val userId: ULong,
    @SerialName("game_type")
    @Serializable(with = X19GameType.Serializer::class)
    val gameType: X19GameType,
    @SerialName("skin_type")
    val skinType: UInt,
    @SerialName("skin_id")
    val skinId: ULong,
    @SerialName("skin_mode")
    @Serializable(with = SkinMode.Serializer::class)
    val skinMode: SkinMode,
    @SerialName("client_type")
    val clientType: X19ClientType
): X19Entity() {
    enum class SkinMode(val displayName: String) {
        DEFAULT("粗壮"),
        SLIM("纤细");
        object Serializer: KSerializer<SkinMode> {
            override val descriptor = PrimitiveSerialDescriptor("SkinMode", PrimitiveKind.INT)
            override fun serialize(encoder: Encoder, value: SkinMode) = encoder.encodeInt(value.ordinal)
            override fun deserialize(decoder: Decoder) = SkinMode.entries[decoder.decodeInt()]
        }
    }
}