package net.nekocurit.x19.data.game

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

enum class X19GameType(val id: UInt) {
    /**
     * 老版本全局皮肤(?)
     */
    All(1U),
    /**
     * 网络游戏
     */
    Network(2U),
    UNKNOWN_7(7U),
    UNKNOWN_8(8U),
    /**
     * 本地联机
     */
    LocalMulti(9U),
    UNKNOWN_10(10U);
    object Serializer: KSerializer<X19GameType> {
        override val descriptor = PrimitiveSerialDescriptor("X19GameType", PrimitiveKind.INT)
        override fun serialize(encoder: Encoder, value: X19GameType) = encoder.encodeInt(value.id.toInt())
        override fun deserialize(decoder: Decoder) = decoder.decodeInt().toUInt()
            .let { value -> X19GameType.entries.first { it.id == value } }
    }
}