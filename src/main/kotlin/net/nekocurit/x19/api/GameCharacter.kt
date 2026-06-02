package net.nekocurit.x19.api

import io.ktor.client.call.body
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.ResponseX19BaseMulti
import net.nekocurit.x19.data.game.X19GameCharacter

/**
 * 获取网络游戏已有角色列表
 *
 * @param serverId 服务器Id
 */
suspend fun WPLauncherAccountAPI.getNetworkServerCharacters(serverId: ULong) = postWithAuth("/game-character/query/user-game-characters", """{"offset":0,"length":10,"user_id":"${session.id}","game_id":"$serverId","game_type":2}""")
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19GameCharacter>(this)

/**
 * 创建网络游戏角色
 *
 * @param serverId 服务器Id
 * @param name 名称
 */
suspend fun WPLauncherAccountAPI.createNetworkServerCharacter(serverId: ULong, name: String) = postWithAuth("/game-character", """{"entity_id":"0","game_id":"$serverId","game_type":2,"user_id":"${session.id}","name":"$name","create_time":555555}""")
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19GameCharacter>(this)

/**
 * 请求删除网络游戏角色
 * 请求后会进入 30天 等待状态 之后通过 `applyDeleteNetworkServerRole` 删除角色
 *
 * @param roleId 角色Id
 */
suspend fun WPLauncherAccountAPI.requestDeleteNetworkServerCharacter(roleId: ULong) = postWithAuth("/game-character/pre-delete", """{"entity_id":"$roleId"}""")
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19GameCharacter>(this)

/**
 * 删除网络游戏角色
 * 需要先请求删除 `requestDeleteNetworkServerRole`
 *
 * @param roleId 角色Id
 */
suspend fun WPLauncherAccountAPI.applyDeleteNetworkServerCharacter(roleId: ULong) = postWithAuth("/game-character/delete", """{"entity_id":"$roleId"}""")
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19GameCharacter>(this)