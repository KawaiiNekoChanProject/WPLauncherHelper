package net.nekocurit.x19.extensions

import net.nekocurit.cipher.Skip32Cipher
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.api.getItemDownload
import net.nekocurit.x19.api.getSkin
import net.nekocurit.x19.data.X19Entity
import net.nekocurit.x19.data.game.X19ClientType
import net.nekocurit.x19.data.skin.X19DefaultSkins
import net.nekocurit.x19.data.skin.X19Skin
import net.nekocurit.x19.extensions.X19SimplePlayerSkin.Companion.toSimple

val cipher = Skip32Cipher()

/**
 * 根据 游戏内玩家 UUID 获取玩家皮肤
 *
 * @param id UUID().toString()
 */
suspend fun WPLauncherAccountAPI.getSkinFromUUID(id: String) = getSkinFromId(cipher.computeUserIdFromUuid(id))


/**
 * 根据 启动器Id 获取玩家皮肤
 *
 * @param id 启动器用户标识
 */
suspend fun WPLauncherAccountAPI.getSkinFromId(id: ULong) = getSkin(id).toSimple(this)

data class X19SimplePlayerSkin(
    val java: Detail,
    val bedrock: Detail
): X19Entity() {
    companion object {
        suspend fun List<X19Skin>.toSimple(api: WPLauncherAccountAPI) = X19SimplePlayerSkin(
            firstOrNull { it.clientType == X19ClientType.Java }
                ?.let { Detail(it.skinId, it.skinMode, api.getItemDownload(it.skinId).components.first().resourceUrl) }
                ?: Detail.DEFAULT,
            firstOrNull { it.clientType == X19ClientType.Bedrock }
                ?.let { Detail(it.skinId, it.skinMode, api.getItemDownload(it.skinId).components.first().resourceUrl) }
                ?: Detail.DEFAULT
        )
    }
    data class Detail(
        val id: ULong,
        val mode: X19Skin.SkinMode,
        val url: String
    ) {
        companion object {
            val DEFAULT = Detail(X19DefaultSkins.STEVE, X19Skin.SkinMode.DEFAULT, X19DefaultSkins.STEVE_DOWNLOAD_URL)
        }
    }
}