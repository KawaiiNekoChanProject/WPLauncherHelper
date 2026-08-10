package net.nekocurit.x19.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.nekocurit.utils.nextString
import net.nekocurit.utils.json
import net.nekocurit.x19.NetEaseEncryptUtils
import net.nekocurit.x19.WPLUpdaterAPI
import net.nekocurit.x19.data.cookie.AbstractWPLauncherCookie
import net.nekocurit.x19.data.entity.X19LoginOtp
import kotlin.random.Random

/**
 * 用于: `https://x19obtcore.nie.netease.com:8443/authentication-otp`
 */
@Suppress("SpellCheckingInspection")
@Serializable
data class RequestX19Authentication(
    @SerialName("otp_token")
    val otpToken: String,
    @SerialName("otp_pwd")
    val otpPassword: String = "",
    val aid: ULong,
    @SerialName("sauth_json")
    val sauth: String,
    @SerialName("sa_data")
    val environment: String,
    val version: Version,
) {
    @Serializable
    data class Environment(
        @SerialName("os_name")
        val osName: String = "windows",
        @SerialName("os_ver")
        val osVersion: String = "Microsoft Windows 10",
        @SerialName("mac_addr")
        val macAddress: String,
        val udid: String,
        @SerialName("app_ver")
        val appVersion: String,
        @SerialName("sdk_ver")
        val sdkVersion: String = "",
        val network: String = "",
        val disk: String,
        val is64bit: Int = 1,
        @SerialName("video_card1")
        val videCard1: String = "NVIDIA GeForce GTX 4060",
        @SerialName("video_card2")
        val videCard2: String = "",
        @SerialName("video_card3")
        val videCard3: String = "",
        @SerialName("video_card4")
        val videCard4: String = "",
        @SerialName("launcher_type")
        val launcherType: String = "PC_java",
        @SerialName("pay_channel")
        val payChannel: String = "netease"
    ) {
        constructor(cookie: AbstractWPLauncherCookie, aid: ULong, latest: String): this(
            macAddress = "00-00-00-00-00-00",
            udid = cookie.getUdId(),
            appVersion = latest,
            disk = Random(aid.toLong()).nextString(6, "0123456789ABCEF")
        )

        override fun toString() = json.encodeToString(this)
    }

    companion object {
        suspend fun fromLoginOtpRespond(cookie: AbstractWPLauncherCookie, data: X19LoginOtp) = RequestX19Authentication(cookie, data, WPLUpdaterAPI.newInstance().get().version)
    }

    constructor(cookie: AbstractWPLauncherCookie, data: X19LoginOtp, latest: String): this(
        otpToken = data.otpToken,
        aid = data.aid,
        sauth = cookie.toCookie().toString().replace(""""platform":"ad"""", """"platform":"pc""""),
        environment = Environment(cookie, data.aid, latest).toString(),
        version = Version(latest)
    )

    @Serializable
    data class Version(
        val version: String,
        @SerialName("launcher_md5")
        val md5Launcher: String = "",
        @SerialName("updater_md5")
        val md5Updater: String = "",
    )

    fun generateRequestBody() = NetEaseEncryptUtils.httpEncrypt(json.encodeToString(this))

}