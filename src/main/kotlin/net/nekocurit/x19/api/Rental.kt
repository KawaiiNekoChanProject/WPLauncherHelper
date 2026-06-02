package net.nekocurit.x19.api

import io.ktor.client.call.*
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.ResponseX19BaseMulti
import net.nekocurit.x19.data.game.X19RentalServer
import net.nekocurit.x19.data.game.X19RentalServerJoinInfo

/**
 * 搜索租赁服
 *
 * @param key 关键词 可直接填写服务器号
 */
suspend fun WPLauncherAccountAPI.searchRentalServers(key: String) = postWithAuth("/rental-server/query/available-public-server", """{"offset":0,"sort_type":0,"world_name_key":"$key"}""")
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19RentalServer>(this)

/**
 * 获取租赁服信息
 *
 * @param serverId 服务器号
 */
suspend fun WPLauncherAccountAPI.getRentalServerInfo(serverId: ULong) = postWithAuth("/rental-server-details/get", """{"server_id":"$serverId"}""")
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19RentalServer>(this)

/**
 * 获取租赁服直连信息
 *
 * @param serverId 服务器号
 * @param password 密码
 */
suspend fun WPLauncherAccountAPI.getRentalServerJoinInfo(serverId: ULong, password: String = "") = postWithAuth("/rental-server-world-enter/get", """{"server_id":"$serverId","pwd":"$password"}""")
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19RentalServerJoinInfo>(this)