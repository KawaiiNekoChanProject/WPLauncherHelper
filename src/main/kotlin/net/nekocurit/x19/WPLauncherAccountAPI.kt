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
import net.nekocurit.x19.data.WPLauncherSession
import net.nekocurit.x19.data.entity.X19AuthenticationEntity
import net.nekocurit.x19.data.entity.X19AuthenticationEntity.Companion.asX19AuthenticationEntity
import net.nekocurit.x19.data.game.X19NetworkServerJoinInfo.Companion.asX19NetworkServerJoinInfo

class WPLauncherAccountAPI(var session: WPLauncherSession) {

    constructor(id: ULong, token: String): this(WPLauncherSession(id, token))
    constructor(entity: X19AuthenticationEntity): this(WPLauncherSession(entity))

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
        body = """{"entity_id":${session.id}}""",
        url = "https://x19obtcore.nie.netease.com:8443",
        hasEncrypt = true
    )
        .let { NetEaseEncryptUtils.httpDecrypt(it.bodyAsBytes()) }
        .let { json.decodeFromString<ResponseX19Base>(it) }
        .throwOnNotOk()
        .asX19AuthenticationEntity()
        .also { session.update(it) }

    /**
     * 向自定义端点发送数据
     * 
     * @parm path 路径
     * @param body 请求体
     * @param hasEncrypt 是否加密
     */
    suspend fun postWithAuth(path: String, body: String = "", url: String = "https://x19apigatewayobt.nie.netease.com", hasEncrypt: Boolean = false) = client.post("$url$path") {
        header("user-id", session.id)
        header("user-token", NetEaseEncryptUtils.computeDynamicToken(session.token, path, body))

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
    suspend fun logout() = postWithAuth("/authentication/delete", """{"user_id":"${session.id}","logout_type":0}""")
        .body<ResponseX19Base>()
        .throwOnNotOk()
}