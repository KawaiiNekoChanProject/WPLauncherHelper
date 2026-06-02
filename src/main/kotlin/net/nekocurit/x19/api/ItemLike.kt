package net.nekocurit.x19.api

import io.ktor.client.call.*
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.game.X19ItemLike


/**
 * 申请组件是否喜欢实例
 * 即启动器点开服务器详情页面中 推荐 / 不推荐
 * 需要稍后使用 `updateNetworkServerLike` 更新评论
 *
 * @param itemId 物品Id
 */
suspend fun WPLauncherAccountAPI.requestItemLike(itemId: ULong) = postWithAuth(
    path = "/user-item-like",
    body = """{"entity_id":"0","item_id":"$itemId","user_id":"${session.id}","has_like":-1}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19ItemLike>(this)

/**
 * 更新对组件是否喜欢
 *
 * @param like 实例
 * @param flag 是否喜欢  null = 清空状态
 */
suspend fun WPLauncherAccountAPI.updateItemLike(like: X19ItemLike, flag: Boolean?) = postWithAuth(
    path = "/user-item-like/update",
    body = """{"entity_id":"${like.id}","item_id":"${like.itemId}","user_id":"${session.id}","has_like":${flag.toHasLikeId()}}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19ItemLike>(this)

private fun Boolean?.toHasLikeId() = this?.let { flag -> if (flag) 1 else -1 } ?: 0