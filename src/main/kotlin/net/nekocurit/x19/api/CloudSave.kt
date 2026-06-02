@file:Suppress("SpellCheckingInspection")

package net.nekocurit.x19.api

import io.ktor.client.call.*
import net.nekocurit.utils.json
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.ResponseX19BaseMulti
import net.nekocurit.x19.data.cloud_save.X19CloudSave
import net.nekocurit.x19.data.cloud_save.X19CloudSaveDownload
import net.nekocurit.x19.data.cloud_save.X19CloudSaveUpload

suspend fun WPLauncherAccountAPI.getCloudSaveList() = postWithAuth(
    path = "/cloud-save-v2/query/my-save-list",
    body = """{"item_id":"${session.id}","offset":0,"length":50}"""
)
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19CloudSave>(this)

suspend fun WPLauncherAccountAPI.createCloudSave() = postWithAuth(
    path = "/cloud-save-v2",
    body = """{"vip_only":"no"}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19CloudSave>(this)

suspend fun WPLauncherAccountAPI.getCloudSave(id: ULong) = postWithAuth(
    path = "/cloud-save-v2/get",
    body = """{"entity_id":"$id"}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19CloudSave>(this)

suspend fun WPLauncherAccountAPI.updateCloudSave(data: X19CloudSave) = postWithAuth(
    path = "/cloud-save-v2/update",
    body = json.encodeToString(data)
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19CloudSave>(this)

suspend fun WPLauncherAccountAPI.deleteCloudSave(id: ULong) = postWithAuth(
    path = "/cloud-save-v2/delete",
    body = """{"entity_id":"$id"}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19CloudSave>(this)

suspend fun WPLauncherAccountAPI.requestUploadCloudSave(id: ULong) = postWithAuth(
    path = "/cloud-save-upload-v2",
    body = """{"url":null,"active_time":0,"partnum":0,"entity_id":"$id"}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19CloudSaveUpload>(this)

suspend fun WPLauncherAccountAPI.requestDownloadCloudSave(id: ULong) = postWithAuth(
    path = "/cloud-save-download-v2",
    body = """{"url":null,"active_time":0,"partnum":0,"md5":null,"size":0,"entity_id":"$id"}"""
)
    .body<ResponseX19Base>()
    .throwOnNotOk()
    .decode<X19CloudSaveDownload>(this)