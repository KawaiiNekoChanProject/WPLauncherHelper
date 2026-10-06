package net.nekocurit.x19.api

import io.ktor.client.call.body
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.ResponseX19BaseMulti
import net.nekocurit.x19.data.friend.X19Friend
import net.nekocurit.x19.data.friend.X19PendingFriend

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
    .unit()

/**
 * 删除好友
 *
 * @param id 用户Id
 */
suspend fun WPLauncherAccountAPI.deleteFriend(id: ULong) = postWithAuth(
    path = "/user-del-friend",
    body = """{"fid":$id}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .unit()

/**
 * 获取好友列表
 */
suspend fun WPLauncherAccountAPI.getFriends() = postWithAuth(
    path = "/user-allfriends-with-detail"
)
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19Friend>(this)

/**
 * 获取待定好友列表 (即主动添加登录号待审核列表, 获取列表后可以同意/拒绝)
 */
suspend fun WPLauncherAccountAPI.getPendingFriends() = postWithAuth(
    path = "/user-addme-friends"
)
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19PendingFriend>(this)

/**
 * 处理好友申请
 *
 * @param id 用户Id
 * @param accept 是否接受
 */
suspend fun WPLauncherAccountAPI.replyFriendRequest(id: ULong, accept: Boolean) = postWithAuth(
    path = "/user-reply-friend",
    body = """{"fid":$id,"accept":$accept}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .unit()

/**
 * 设置好友备注
 *
 * @param id 用户Id
 * @param mark 备注内容 不能包含特殊字符
 */
suspend fun WPLauncherAccountAPI.setFriendMark(id: ULong, mark: String) = postWithAuth(
    path = "/user-change-friend-mark",
    body = """{"uid":$id,"cid":0,"mark":"$mark"}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .unit()