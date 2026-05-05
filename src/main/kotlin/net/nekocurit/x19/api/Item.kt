package net.nekocurit.x19.api

import io.ktor.client.call.*
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.item.X19ItemDetails

/**
 * 获取组件详细信息
 *
 * @param itemId 组件标识
 */
suspend fun WPLauncherAccountAPI.getItemDetails(itemId: ULong) = postWithAuth("/item-details/get_v2", """{"item_id":"$itemId"}""")
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19ItemDetails>(this)