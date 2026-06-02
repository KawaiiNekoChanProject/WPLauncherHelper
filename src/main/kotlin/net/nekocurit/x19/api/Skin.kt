package net.nekocurit.x19.api

import io.ktor.client.call.*
import net.nekocurit.x19.WPLauncherAccountAPI
import net.nekocurit.x19.data.ResponseX19BaseMulti
import net.nekocurit.x19.data.game.X19ClientType
import net.nekocurit.x19.data.skin.X19Skin
import net.nekocurit.x19.data.skin.X19Skin.SkinMode

/**
 * 获取皮肤
 *
 * @param userId 指定用户Id 省略默认为自身
 */
suspend fun WPLauncherAccountAPI.getSkin(userId: ULong = session.id) = postWithAuth(
    path = "/user-game-skin/query/search-by-type",
    body = """{"user_id":"$userId","game_type":null,"client_type":"all","entity_id":null}"""
)
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19Skin>(this)

/**
 * 设置皮肤
 *
 * @param itemId 皮肤ID
 * @param mode 是否应用纤细效果
 */
suspend fun WPLauncherAccountAPI.setSkin(itemId: ULong, mode: SkinMode = SkinMode.DEFAULT, clientType: X19ClientType = X19ClientType.Java) = postWithAuth(
    path = "/user-game-skin-multi",
    body = arrayOf(9, 8, 2, 10, 7)
        .joinToString(",") { """{"game_type":$it,"skin_type":31,"skin_id":"$itemId","skin_mode":${mode.ordinal},"client_type":"${clientType.id}","entity_id":null}""" }
        .let { """{"skin_settings":[$it]}"""}
)
    .body<ResponseX19BaseMulti>()
    .throwOnNotOk()
    .decode<X19Skin>(this)