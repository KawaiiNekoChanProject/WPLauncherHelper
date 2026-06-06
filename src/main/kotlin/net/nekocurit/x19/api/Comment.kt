package net.nekocurit.x19.api

import io.ktor.client.call.body
import kotlinx.serialization.json.JsonPrimitive
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.ResponseX19BaseMulti
import net.nekocurit.x19.data.item.X19Comment
import kotlin.time.Clock

/**
 * 根据顶量获取组件评论
 *
 * @param id 组件Id
 * @param page 翻页
 */
suspend fun WPLauncherAccountAPI.getTopItemComments(id: ULong, page: Int = 0) = postWithAuth(
    path = "/user-item-comment/query/rank-by-user-comment-like",
    body = """{"item_id":"$id","offset":${page * 5},"length":5}"""
)
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19Comment>(this) { itemId = id }

/**
 * 获取组件评论
 *
 * @param id 组件Id
 * @param masterId 如果不为 0 则获取指定评论下的子评论
 * @param page 翻页
 */
suspend fun WPLauncherAccountAPI.getItemComments(id: ULong, masterId: ULong = 0UL, page: Int = 0) = postWithAuth(
    path = "/user-item-comment/query/search-by-item",
    body = """{"item_id":"$id","master_id":"$masterId","offset":${page * 5},"length":5}"""
)
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19Comment>(this) { itemId = id }

suspend fun WPLauncherAccountAPI.getComment(id: ULong) = postWithAuth(
    path = "/user-item-comment/get",
    body = """{"entity_id":"$id"}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19Comment>(this) { itemId = id }

suspend fun WPLauncherAccountAPI.likeComment(userId: ULong, commentId: ULong) = postWithAuth(
    path = "/user-comment-like",
    // 虽然有 has_like 字段  但是实际上启动器无法取消顶状态
    body = """{"entity_id":"0","user_id":"$userId","comment_id":"$commentId","has_like":true}"""
)
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()


/**
 * 对组件发表评论
 *
 * @param itemId 物品Id
 * @param content 评论内容
 */
suspend fun WPLauncherAccountAPI.sendItemComment(itemId: ULong, content: String, replyId: ULong = 0UL) = postWithAuth(
    path = "/user-item-comment",
    body = """{"entity_id":"0","user_id":"${session.id}","item_id":"$itemId","content":${JsonPrimitive(content)},"master_id":"$replyId","reply_id":"$replyId","total_like":0,"create_time":${Clock.System.now().toEpochMilliseconds()},"comment_type":0}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()