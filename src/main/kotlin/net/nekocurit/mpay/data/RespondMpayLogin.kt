package net.nekocurit.mpay.data

import kotlinx.serialization.Serializable
import net.nekocurit.mpay.entity.MpayUser

@Serializable
data class RespondMpayLogin(
    val user: MpayUser
)
