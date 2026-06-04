package net.nekocurit.x19.api

import io.ktor.client.call.*
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.game.X19NetworkServerJoinInfo

/**
 * 获取网络服务器连接信息
 *
 * @param serverId 服务器Id
 */
suspend fun WPLauncherAccountAPI.getNetworkServerAddress(serverId: ULong) = postWithAuth("/item-address/get", """{"item_id":"$serverId"}""")
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19NetworkServerJoinInfo>(this)