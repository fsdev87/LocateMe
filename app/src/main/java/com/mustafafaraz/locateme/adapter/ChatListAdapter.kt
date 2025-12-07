package com.mustafafaraz.locateme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mustafafaraz.locateme.R
import com.mustafafaraz.locateme.data.model.Chat
import com.mustafafaraz.locateme.utils.TimeFormatter

class ChatListAdapter(
    private val context: Context,
    private val chats: List<Chat>,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    inner class ChatListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val userAvatar: ImageView = itemView.findViewById(R.id.user_avatar)
        private val userName: TextView = itemView.findViewById(R.id.user_name)
        private val lastMessage: TextView = itemView.findViewById(R.id.last_message)
        private val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        private val unreadBadge: TextView = itemView.findViewById(R.id.unread_badge)
        private val chatContainer: LinearLayout = itemView.findViewById(R.id.chat_container)

        fun bind(chat: Chat) {
            // Load user profile picture or default avatar
            if (!chat.userProfilePic.isNullOrEmpty()) {
                Glide.with(context)
                    .load(chat.userProfilePic)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(userAvatar)
            } else {
                userAvatar.setImageResource(R.drawable.ic_person)
            }

            userName.text = chat.otherUserName

            // Display last message or default text
            lastMessage.text = when {
                chat.lastMessage != null -> {
                    if (chat.lastMessageType == "IMAGE") "📷 Image" else chat.lastMessage
                }
                else -> "Start chatting..."
            }

            // Format timestamp
            timestamp.text = if (chat.lastMessageTime != null) {
                TimeFormatter.formatTimeAgo(chat.lastMessageTime)
            } else {
                TimeFormatter.formatTimeAgo(chat.createdAt)
            }

            // Hide unread badge (we can implement unread count later if needed)
            unreadBadge.visibility = View.GONE

            // Click listener
            chatContainer.setOnClickListener {
                onChatClick(chat)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.chat_list_row, parent, false)
        return ChatListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    override fun getItemCount(): Int = chats.size
}