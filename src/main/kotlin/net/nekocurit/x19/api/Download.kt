package net.nekocurit.x19.api

import io.ktor.client.call.*
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.download.X19Download

/**
 * 获取组件下载链接
 *
 * @param id 组件标识
 */
suspend fun WPLauncherAccountAPI.getItemDownload(id: ULong) = postWithAuth(
    path = "/user-item-download-v2",
    body = """{"item_id":"$id","offset":0,"length":5}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19Download>(this)