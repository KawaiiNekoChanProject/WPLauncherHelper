package net.nekocurit.x19.api

import io.ktor.client.call.*
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.ResponseX19BaseMulti
import net.nekocurit.x19.data.user.X19User
import net.nekocurit.x19.data.user.X19UserDetails
import net.nekocurit.x19.data.user.X19UserPublicState

suspend fun WPLauncherAccountAPI.getUser(id: ULong) = postWithAuth(
    path = "/user/query/search-by-uid",
    body = """{"user_id":"$id"}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19User>(this)

suspend fun WPLauncherAccountAPI.getUser(ids: Collection<ULong>) = postWithAuth(
    path = "/user/query/search-by-ids",
    body = """{"entity_ids":[${ids.joinToString(",")}]}"""
)
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19User>(this)

/**
 * 搜索陌生人
 *
 * @param input 名称或者邮箱
 * @param isMail 是否为邮箱搜索
 */
suspend fun WPLauncherAccountAPI.searchFriends(input: String, isMail: Boolean = input.contains("@")) = postWithAuth("/user-search-friend", """{"name_or_mail":"$input","mail_flag":$isMail}""")
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19UserDetails>(this)

/**
 * 主动添加好友
 *
 * @param id 用户Id
 * @param message 消息
 * @param selfNick 自身名称 通过`getSelfDetail`获取
 */
suspend fun WPLauncherAccountAPI.sendFriendRequest(id: ULong, message: String = "", selfNick: String = session.name) = postWithAuth(
    path = "/user-apply-friend",
    body = """{"fid":$id,"comment":"$selfNick","message":"$message"}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()

/**
 * 获取用户状态
 *
 * @param id 用户Id
 */
suspend fun WPLauncherAccountAPI.getUserPublicState(id: ULong) = postWithAuth(
    path = "/user-stat/get-user-state",
    body = """{"search_id":"$id"}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19UserPublicState>(this)

/**
 * 为其它用户点赞/取消点赞
 * 该端点具有调用频率限制
 *
 * @param id 用户Id
 * @param state true=点赞, false=取消
 */
suspend fun WPLauncherAccountAPI.likeUser(id: ULong, state: Boolean = true) = postWithAuth(
    path = "/user-personal-page-like/update",
    body = """{"entity_id":"${session.id}","visitor_user_id":"${session.id}","personal_page_owner_user_id":"$id","has_like":$state}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()