package net.nekocurit.utils

import kotlinx.serialization.json.JsonPrimitive
import java.security.MessageDigest

fun String.encodeMd5() = MessageDigest.getInstance("MD5")
    .digest(toByteArray())
    .toHexString()

fun String.toJsonString() = JsonPrimitive(this).toString()
