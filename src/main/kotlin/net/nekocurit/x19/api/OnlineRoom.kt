package net.nekocurit.x19.api

import io.ktor.client.call.*
import net.nekocurit.utils.toJsonString
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.ResponseX19BaseMulti
import net.nekocurit.x19.data.online_room.X19OnlineRoom
import net.nekocurit.x19.data.online_room.X19OnlineRoomDetail
import net.nekocurit.x19.data.online_room.X19OnlineRoomJoinInfo
import net.nekocurit.x19.data.online_room.X19OnlineRoomMember

/**
 * 加入在线联机
 *
 * @param id 房间Id
 * @param password 房间密码
 */
@Suppress("SpellCheckingInspection")
suspend fun WPLauncherAccountAPI.onlineRoomJoin(id: ULong, password: String = "") = postWithAuth(
    path = "/online-lobby-room-enter",
    body = """{"room_id":"$id","password":${password.toJsonString()},"check_visibilily":true}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19OnlineRoom>(this)

/**
 * 离开之前加入的联机房间
 *
 * @param id 房间Id
 */
suspend fun WPLauncherAccountAPI.onlineRoomLeave(id: ULong) = postWithAuth(
    path = "/online-lobby-room-enter/leave-room",
    body = """{"room_id":"$id"}"""
)

/**
 * 查询在线房间成员列表
 *
 * @param id 房间Id
 */
suspend fun WPLauncherAccountAPI.onlineRoomGetMembers(id: ULong) = postWithAuth(
    path = "/online-lobby-member/query/list-by-room-id",
    body = """{"room_id":"$id"}"""
)
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19OnlineRoomMember>(this)

/**
 * 获取当前加入的房间的连接信息
 */
suspend fun WPLauncherAccountAPI.onlineRoomCurrentJoinInfo() = postWithAuth(
    path = "/online-lobby-game-enter"
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19OnlineRoomJoinInfo>(this)


/**
 * 搜索房间
 *
 * @param name 名称
 */
suspend fun WPLauncherAccountAPI.onlineRoomSearch(name: String) = postWithAuth(
    path = "/online-lobby-room/query/search-by-name",
    body = """{"room_name":${name.toJsonString()},"res_id":"","version":"1.21.50","offset":0,"length":50}"""
)
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19OnlineRoomDetail>(this)
