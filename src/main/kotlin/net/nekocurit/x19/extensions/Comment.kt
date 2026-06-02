package net.nekocurit.x19.extensions

import net.nekocurit.utils.easyMultiPage
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.api.getItemComments
import net.nekocurit.x19.api.getTopItemComments

/**
 * 根据顶量获取组件评论 增强版
 *
 * @param id 组件Id
 * @param page 当前页码
 * @param length 每页长度
 */
suspend fun WPLauncherAccountAPI.getTopItemComments(
    id: ULong,
    page: Int = 0,
    length: Int = 5
) = easyMultiPage(page = page, length = length, baseLength = 5) { i -> getTopItemComments(id, i) }

/**
 * 获取组件评论 增强版
 *
 * @param id 组件Id
 * @param masterId 如果不为 0 则获取指定评论下的子评论
 * @param page 当前页码
 * @param length 每页长度
 */
suspend fun WPLauncherAccountAPI.getItemComments(
    id: ULong,
    masterId: ULong = 0UL,
    page: Int = 0,
    length: Int = 5
) =  easyMultiPage(page = page, length = length, baseLength = 5) { i -> getItemComments(id, masterId, i) }