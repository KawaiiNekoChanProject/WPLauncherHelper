package net.nekocurit.x19.data

import net.nekocurit.x19.data.entity.X19AuthenticationEntity

data class WPLauncherSession(
    val id: ULong,
    var token: String,
    var name: String = ""
) {
    constructor(entity: X19AuthenticationEntity): this(entity.entityId, entity.token)
    fun update(entity: X19AuthenticationEntity) {
        require(entity.entityId == id) { "Entity ID mismatch." }
        token = entity.token
    }
}