package net.nekocurit.mpay.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.nekocurit.mpay.entity.MpayUser

@Serializable
data class RespondMpayTicket(
    @SerialName("guide_text")
    val message: String,
    val ticket: String
)
