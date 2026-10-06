package net.nekocurit.x19.data.friend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.nekocurit.x19.api.replyFriendRequest
import net.nekocurit.x19.data.X19AuthEntity

@Serializable
data class X19PendingFriend(
    @SerialName("uid")
    val id: ULong,
    @SerialName("nickname")
    val name: String,
    @SerialName("headImage")
    val avatarUrl: String,
    @SerialName("frame_id")
    val frameUrl: String
): X19AuthEntity() {
    suspend fun accept() { api.replyFriendRequest(id, true) }
    suspend fun deny() { api.replyFriendRequest(id, false) }
}