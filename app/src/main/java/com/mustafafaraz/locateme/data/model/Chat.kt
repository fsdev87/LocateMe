package com.mustafafaraz.locateme.data.model

import com.google.gson.annotations.SerializedName

data class Chat(
    val id: Int,
    @SerializedName("other_user_id")
    val otherUserId: Int,
    @SerializedName("other_user_name")
    val otherUserName: String,
    @SerializedName("other_user_email")
    val otherUserEmail: String,
    @SerializedName("user_profile_pic")
    private val _userProfilePic: String?,
    @SerializedName("last_message")
    val lastMessage: String?,
    @SerializedName("last_message_type")
    val lastMessageType: String?,
    @SerializedName("last_message_time")
    val lastMessageTime: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("last_message_at")
    val lastMessageAt: String?
) {
    val userProfilePic: String?
        get() = _userProfilePic?.replace(":5000", "")
}

data class CreateChatRequest(
    val otherUserId: Int
)