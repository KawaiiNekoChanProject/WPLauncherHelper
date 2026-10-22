package net.nekocurit.x19.api

import io.ktor.client.call.body
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.image.X19ImageUpload

/**
 * 请求图片上传凭证
 *
 * 上传图片需要审核, 且会记录启动器ID, IP地址
 *
 *  **使用时请遵循中国大陆相关法律法规及 WPLauncher EULA, 严禁上传违规图片**
 */
suspend fun WPLauncherAccountAPI.requestImageUpload() = postWithAuth(
    path = "/image-upload-token",
    body = """{"token":null,"url":null,"verify":false,"entity_id":null}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19ImageUpload>(this)