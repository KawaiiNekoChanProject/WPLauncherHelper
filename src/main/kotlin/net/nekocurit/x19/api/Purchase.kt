package net.nekocurit.x19.api

import io.ktor.client.call.*
import kotlinx.serialization.json.JsonObject
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.game.X19Purchase

/**
 * 是否已购买指定组件
 *
 * @param itemId 物品Id (可为 网络服务器Id, 皮肤Id 等)
 */
suspend fun WPLauncherAccountAPI.isPurchase(itemId: ULong) = postWithAuth(
    path = "/item/user-is-purchase-item",
    body = """{"item_id":"$itemId","length":0,"offset":0}"""
)
    .body<ResponseX19Base>()
    .entity is JsonObject

/**
 * 获得通用组件购买订单
 * 此为 内部状态 必须获取才能发布评论和状态
 *
 * @param itemId 物品Id
 */
suspend fun WPLauncherAccountAPI.createPurchaseOrder(itemId: ULong) = postWithAuth(
    path = "/user-item-purchase",
    body = """{"entity_id":0,"item_id":"$itemId","item_level":0,"user_id":"${session.id}","purchase_time":0,"last_play_time":0,"total_play_time":0,"receiver_id":"","buy_path":"PC_H5_LAUNCH_GAME","coupon_ids":null,"diamond":0,"activity_name":"","batch_count":1}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19Purchase>(this)

/**
 * 购买物品
 *
 * @param purchase 订单数据
 */
@Suppress("SpellCheckingInspection")
suspend fun WPLauncherAccountAPI.buyItem(purchase: X19Purchase) = postWithAuth(
    path = "/buy-item-result",
    body = """{"orderid":"${purchase.orderId}","buy_type":${purchase.bugType}}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()