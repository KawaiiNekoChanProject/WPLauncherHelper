package net.nekocurit.x19.extensions

import net.nekocurit.utils.retry
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.api.buyItem
import net.nekocurit.x19.api.createPurchaseOrder
import net.nekocurit.x19.api.isPurchase
import kotlin.time.Duration.Companion.seconds

/**
 * 购买指定物品如果检测到没有购买
 *
 * @param itemId 物品Id
 */
suspend fun WPLauncherAccountAPI.buyItemIfNotHave(itemId: ULong) {
    // 有的时候会返回购买尚未成功 但实际上已成功购买
    retry(3, 2.seconds) {
        if (isPurchase(itemId)) return@retry
        buyItem(createPurchaseOrder(itemId))
    }
}