package net.nekocurit.x19.extensions

import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.api.getItemDetails
import net.nekocurit.x19.api.requestItemLike
import net.nekocurit.x19.api.updateItemLike

/**
 * 设置是否喜欢指定组件
 *
 * @param itemId 物品Id
 * @param like 喜欢/不喜欢  null = 清空设置
 */
suspend fun WPLauncherAccountAPI.sendItemLike(itemId: ULong, like: Boolean?) {
    val commentId = getItemDetails(itemId).commentId ?: requestItemLike(itemId).id

    updateItemLike(commentId, itemId, like)
}