package net.nekocurit.x19.extensions

import net.nekocurit.x19.WPLauncherAPI
import net.nekocurit.x19.data.cookie.AbstractWPLauncherCookie

suspend fun WPLauncherAPI.login(cookie: AbstractWPLauncherCookie) = cookie
    .also { uniCookie(it) }
    .let { authentication(cookie, loginCookie(cookie)) }
    .toAccountAPI(client)