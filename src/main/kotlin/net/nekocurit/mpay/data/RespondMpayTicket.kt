package net.nekocurit.mpay.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RespondMpayTicket(
    @SerialName("guide_text")
    val message: String,
    val ticket: String
)
