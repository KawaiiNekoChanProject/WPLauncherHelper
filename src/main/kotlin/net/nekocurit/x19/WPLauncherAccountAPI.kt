package net.nekocurit.x19

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import net.nekocurit.utils.json
import net.nekocurit.x19.data.ResponseX19Base
import net.nekocurit.x19.data.ResponseX19BaseMulti
import net.nekocurit.x19.data.entity.X19AuthenticationEntity
import net.nekocurit.x19.data.entity.X19AuthenticationEntity.Companion.asX19AuthenticationEntity
import net.nekocurit.x19.data.game.X19NetworkServerJoinInfo.Companion.asX19NetworkServerJoinInfo
import net.nekocurit.x19.data.game.X19RentalServer.Companion.asX19RentalServer
import net.nekocurit.x19.data.game.X19RentalServerJoinInfo.Companion.asX19RentalServerJoinInfo

class WPLauncherAccountAPI(var entity: X19AuthenticationEntity) {

    var cacheName = ""

    constructor(id: ULong, token: String): this(X19AuthenticationEntity(entityId = id, token = token))

    val client = HttpClient(Java) {
        install(ContentNegotiation) {
            json(json, contentType = ContentType.Any)
        }
        defaultRequest {
            header("User-Agent", "WPFLauncher/0.0.0.0")
        }
    }

    /**
     * 执行心跳
     * 执行后会更新 token 如果需要长期在线必须执行 否则账号会掉线
     */
    suspend fun refresh() = postWithAuth(
        path = "/authentication/update",
        body = """{"entity_id":${entity.entityId}}""",
        url = "https://x19obtcore.nie.netease.com:8443",
        hasEncrypt = true
    )
        .let { NetEaseEncryptUtils.httpDecrypt(it.bodyAsBytes()) }
        .let { json.decodeFromString<ResponseX19Base>(it) }
        .throwOnNotOk()
        .asX19AuthenticationEntity()
        .also { entity = it }

    /**
     * 向自定义端点发送数据
     * 
     * @parm path 路径
     * @param body 请求体
     * @param hasEncrypt 是否加密
     */
    suspend fun postWithAuth(path: String, body: String = "", url: String = "https://x19apigatewayobt.nie.netease.com", hasEncrypt: Boolean = false) = client.post("$url$path") {
        header("user-id", entity.entityId)
        header("user-token", NetEaseEncryptUtils.computeDynamicToken(entity.token, path, body))

        setBody(if (hasEncrypt) NetEaseEncryptUtils.httpEncrypt(body) else body)
    }

    /**
     * 执行内部登录状态标记
     * 账号一生中必须调用一次才能执行购买订单创建
     * 如果不需要创建购买订单 则不需要调用此接口
     */
    suspend fun doLoginStart() = postWithAuth("/interconn/web/game-play-v2/login-start", """{"strict_mode":true}""")
        .body<ResponseX19Base>()
        .throwOnNotOk()

    /**
     * 搜索租赁服
     *
     * @param key 关键词 可直接填写服务器号
     */
    suspend fun searchRentalServers(key: String) = postWithAuth("/rental-server/query/available-public-server", """{"offset":0,"sort_type":0,"world_name_key":"$key"}""")
        .body<ResponseX19BaseMulti>()
        .throwOnNotOk()
        .asX19RentalServer()

    /**
     * 获取租赁服信息
     *
     * @param serverId 服务器号
     */
    suspend fun getRentalServerInfo(serverId: ULong) = postWithAuth("/rental-server-details/get", """{"server_id":"$serverId"}""")
        .body<ResponseX19Base>()
        .throwOnNotOk()
        .asX19RentalServer()

    /**
     * 获取租赁服直连信息
     *
     * @param serverId 服务器号
     * @param password 密码
     */
    suspend fun getRentalServerJoinInfo(serverId: ULong, password: String = "") = postWithAuth("/rental-server-world-enter/get", """{"server_id":"$serverId","pwd":"$password"}""")
        .body<ResponseX19Base>()
        .throwOnNotOk()
        .asX19RentalServerJoinInfo()

    /**
     * 获取网络服务器连接信息
     *
     * @param serverId 服务器Id
     */
    suspend fun getNetworkServerAddress(serverId: ULong) = postWithAuth("/item-address/get", """{"item_id":"$serverId"}""")
        .body<ResponseX19Base>()
        .throwOnNotOk()
        .asX19NetworkServerJoinInfo()

    /**
     * 登出账号
     */
    suspend fun logout() = postWithAuth("/authentication/delete", """{"user_id":"${entity.entityId}","logout_type":0}""")
        .body<ResponseX19Base>()
        .throwOnNotOk()
}