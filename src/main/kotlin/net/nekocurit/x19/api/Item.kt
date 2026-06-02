package net.nekocurit.x19.api

import io.ktor.client.call.*
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.ResponseX19BaseMulti
import net.nekocurit.x19.data.item.X19ItemDetails
import net.nekocurit.x19.data.item.X19ItemSimple

/**
 * 列举有效组件列表
 *
 * @param itemType 组件类型  1=网络服务器, 2=Mod
 * @param page 当前页码, 每页长度 50
 */
suspend fun WPLauncherAccountAPI.getAvailableItems(itemType: UInt, masterType: UInt, page: Int = 0) = postWithAuth(
    path = "/item/query/available",
    body = """{"available_mc_versions":[],"item_type":$itemType,"length":50,"offset":${page * 50},"master_type_id":"$masterType","secondary_type_id":""}"""
)
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19ItemSimple>(this)

/**
 * 获取组件详细信息
 *
 * @param itemId 组件标识
 */
suspend fun WPLauncherAccountAPI.getItemDetails(itemId: ULong) = postWithAuth("/item-details/get_v2", """{"item_id":"$itemId"}""")
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19ItemDetails>(this)