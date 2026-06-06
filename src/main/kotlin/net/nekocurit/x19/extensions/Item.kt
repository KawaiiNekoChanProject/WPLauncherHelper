package net.nekocurit.x19.extensions

import net.nekocurit.utils.easyMultiPage
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.api.getAvailableItems


/**
 * 列举有效组件列表
 *
 * @param itemType 组件类型  1=网络服务器, 2=Mod
 * @param masterType 未知用途 0=默认参数
 * @param page 当前页码
 * @param length 每页长度
 */
suspend fun WPLauncherAccountAPI.getAvailableItems(
    itemType: UInt,
    masterType: UInt,
    page: Int = 0,
    length: Int = 50
) = easyMultiPage(page = page, length = length, baseLength = 50) { i -> getAvailableItems(itemType, masterType, i) }

suspend fun WPLauncherAccountAPI.getAvailableNetworkServers() = getAvailableItems(1U, 0U, 0, 500)