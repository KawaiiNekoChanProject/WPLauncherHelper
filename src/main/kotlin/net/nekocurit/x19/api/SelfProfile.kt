package net.nekocurit.x19.api

import io.ktor.client.call.body
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.user.X19SelfDetail

/**
 * 获取自身信息
 */
suspend fun WPLauncherAccountAPI.getSelfDetail() = postWithAuth("/user-detail", "")
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19SelfDetail>(this) { session.name = name }

/**
 * 初始化自身昵称
 * 仅可在新注册账号首次登录可调用
 *
 * @param name 全局昵称
 */
suspend fun WPLauncherAccountAPI.initNick(name: String) = postWithAuth("/nickname-setting", """{"name":"$name","entity_id":"${session.id}"}""")
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .also { session.name = name }

/**
 * 更新自身昵称
 *
 * @param name 全局昵称
 */
suspend fun WPLauncherAccountAPI.updateNick(name: String) = postWithAuth(
    path = "/pc-nickname-setting/update",
    body = """{"name":"$name"}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .also { session.name = name }