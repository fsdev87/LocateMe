package com.mustafafaraz.locateme.data.model

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    val id: Int,
    @SerializedName("chat_id")
    val chatId: Int,
    @SerializedName("sender_id")
    val senderId: Int,
    @SerializedName("receiver_id")
    val receiverId: Int,
    val type: String, // TEXT or IMAGE
    val content: String?,
    @SerializedName("media_url")
    private val _mediaUrl: String?,
    @SerializedName("is_read")
    val isRead: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("sender_name")
    val senderName: String?,
    @SerializedName("sender_profile_pic")
    private val _senderProfilePic: String?
) {
    val mediaUrl: String?
        get() = _mediaUrl?.replace(":5000", "")

    val senderProfilePic: String?
        get() = _senderProfilePic?.replace(":5000", "")
}

data class SendMessageRequest(
    val chatId: Int,
    val type: String, // TEXT or IMAGE
    val content: String? = null,
    val messageImage: String? = null // base64 encoded image
)