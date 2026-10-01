package net.nekocurit.x19.extensions

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.nekocurit.utils.nextString
import net.nekocurit.x19.data.cookie.AbstractWPLauncherCookie
import kotlin.random.Random

fun AbstractWPLauncherCookie.toCookieWithUni() = JsonObject(toCookie() + ("client_login_sn" to JsonPrimitive(Random.nextString(16))))