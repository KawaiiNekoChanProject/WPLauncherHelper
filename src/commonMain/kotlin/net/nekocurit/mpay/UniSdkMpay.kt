package net.nekocurit.mpay

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.*
import net.nekocurit.mpay.data.RespondMpayDeviceRegister
import net.nekocurit.mpay.data.RespondMpayLogin
import net.nekocurit.mpay.data.RespondMpayLoginError
import net.nekocurit.mpay.data.RespondMpayTicket
import net.nekocurit.mpay.entity.MpayDevice
import net.nekocurit.mpay.utils.MpayLoginEncrypt
import net.nekocurit.utils.json
import net.nekocurit.utils.md5
import net.nekocurit.utils.nextMacAddress
import net.nekocurit.utils.nextString
import net.nekocurit.x19.WPLUpdaterAPI
import net.nekocurit.utils.newPrivateHttpClient
import kotlin.io.encoding.Base64
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.uuid.Uuid

class UniSdkMpay(
    val client: HttpClient,
    val project: String = "aecfrxodyqaaaajp-g-x19",
    val version: String
) {

    companion object {
        val internal: HttpClientConfig<*>.() -> Unit = {
            install(ContentNegotiation) {
                json(json)
            }
            defaultRequest {
                url("https://service.mkey.163.com")
            }
        }
        @Suppress("SpellCheckingInspection")
        const val SCOPE = "nickname,avatar,realname_status,mobile_bind_status,mask_related_mobile,related_login_status"
        suspend fun newInstance() = newInstance(newPrivateHttpClient(internal))
        suspend fun <T : HttpClientEngineConfig> newInstance(
            engine: HttpClientEngineFactory<T>,
            engineConfig: T.() -> Unit = { }
        ) = newInstance(newPrivateHttpClient(engine, engineConfig, internal))
        suspend fun newInstance(client: HttpClient) = UniSdkMpay(client, version = WPLUpdaterAPI(client).get().version)
    }

    /**
     * 注册设备
     * 此接口调用频繁会风控 强烈建议缓存设备数据
     */
    suspend fun registerDevice(id: String = Uuid.random().toString().replace("-", "")) = client
        .submitForm(
            url = "/mpay/games/$project/devices",
            formParameters = buildMpayParameters {
                append("unique_id", id)
                append("brand", "Microsoft")
                append("device_model", "pc_mode")
                append("device_name", "Desktop-${Random.nextString(6)}")
                append("device_type", "Computer")
                append("init_urs_device", "0")
                append("mac", Random.nextMacAddress())
                append("resolution", "1920x1080")
                append("system_name", "windows")
                append("system_version", "10.0.22621")
            }
        )
        .checkError()
        .body<RespondMpayDeviceRegister>()
        .device
        .apply { uniqueId = id }

    /**
     * 登陆
     *
     * @param device 设备标识
     * @param email 邮箱
     * @param password 密码
     */
    suspend fun login(device: MpayDevice, email: String, password: String) = client
        .submitForm(
            url = "/mpay/games/$project/devices/${device.id}/users",
            formParameters = buildMpayParameters {
                append("opt_fields", SCOPE)
                append("params", MpayLoginEncrypt.encryptLoginParams(email, password, device))
                append("un", Base64.encode(email.toByteArray()))
            }
        )
        .checkError()
        .body<RespondMpayLogin>()
        .user

    /**
     * 请求发送一次性代码
     *
     * @param device 设备标识
     * @param phone 手机号
     */
    suspend fun requestSms(device: MpayDevice, phone: String) = client
        .submitForm(
            url = "/mpay/api/users/login/mobile/get_sms",
            formParameters = buildMpayParameters {
                append("device_id", device.id)
                append("mobile", phone)
            }
        )
        .checkError()
        .let { }

    /**
     * 手机号+一次性代码 登录
     * 使用 [requestSms] 发送一次性代码
     *
     * @param device 设备标识
     * @param phone 手机号
     * @param code 收到的验证码
     */
    suspend fun loginByPhoneSms(device: MpayDevice, phone: String, code: String) = client
        .submitForm(
            url = "/mpay/api/users/login/mobile/verify_sms",
            formParameters = buildMpayParameters {
                append("device_id", device.id)
                append("mobile", phone)
                @Suppress("SpellCheckingInspection")
                append("smscode", code)
                append("up_content", "")
            }
        )
        .checkError()
        .body<RespondMpayTicket>()
        .let { finishPhoneLogin(device, it, phone) }

    /**
     * 手机号+密码 登录
     *
     * @param device 设备标识
     * @param phone 手机号
     * @param password 密码
     */
    suspend fun loginByPhonePassword(device: MpayDevice, phone: String, password: String) = client
        .submitForm(
            url = "/mpay/api/users/login/mobile/verify_pwd?un=${Base64.encode(phone.toByteArray())}",
            formParameters = buildMpayParameters {
                append("device_id", device.id)
                append("login_for", "1")
                append("mcount_transaction_id", "3")
                append("mobile", phone)
                append("opt_fields", SCOPE)
                append("password", password.md5())
                append("up_content", "")

                val now = Clock.System.now().toEpochMilliseconds()
                append("transid", "${device.uniqueId}_${now}_100022997")
                append("uni_transaction_id", "${device.uniqueId}_${now}_100023001")
                append("urs_udid", device.uniqueId)
            }
        )
        .checkError()
        .body<RespondMpayTicket>()
        .let { finishPhoneLogin(device, it, phone) }

    suspend fun finishPhoneLogin(device: MpayDevice, ticket: RespondMpayTicket, phone: String) = client
        .submitForm(
            url = "/mpay/api/users/login/mobile/finish?un=${Base64.encode(phone.toByteArray())}",
            formParameters = buildMpayParameters {
                append("device_id", device.id)
                append("opt_fields", SCOPE)
                append("ticket", ticket.ticket)
            }
        )
        .checkError()
        .body<RespondMpayLogin>()
        .user

    @Suppress("SpellCheckingInspection")
    private fun buildMpayParameters(block: ParametersBuilder.() -> Unit) = Parameters.build {
        append("app_channel", "netease")
        append("app_mode", "2")
        append("app_type", "games")
        append("arch", "win_x64")
        append("cv", "c4.2.0")
        append("mcount_app_key", "EEkEEXLymcNjM42yLY3Bn6AO15aGy4yq")
        append("mcount_transaction_id", "0")
        append("process_id", "5555")
        append("sv", "10.0.22621")
        append("updater_cv", "c1.0.0")
        append("game_id", project)
        append("gv", version)

        block(this)
    }

    private suspend fun HttpResponse.checkError() = apply {
        if (!this.status.isSuccess()) error(body<RespondMpayLoginError>().toString())
    }


}