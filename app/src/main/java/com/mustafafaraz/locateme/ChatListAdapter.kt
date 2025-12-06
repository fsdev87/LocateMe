package com.mustafafaraz.locateme

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.data.model.Chat

class ChatListAdapter(
    private val context: Context,
    private val chats: List<Chat>
) : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    inner class ChatListViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {

        private val userAvatar: ImageView = itemView.findViewById(R.id.user_avatar)
        private val userName: TextView = itemView.findViewById(R.id.user_name)
        private val lastMessage: TextView = itemView.findViewById(R.id.last_message)
        private val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        private val unreadBadge: TextView = itemView.findViewById(R.id.unread_badge)
        private val chatContainer: LinearLayout = itemView.findViewById(R.id.chat_container)

        fun bind(chat: Chat) {
            userAvatar.setImageResource(chat.userAvatar)
            userName.text = chat.userName
            lastMessage.text = chat.lastMessage
            timestamp.text = chat.timestamp

            // Show unread badge if there are unread messages
            if (chat.unreadCount > 0) {
                unreadBadge.visibility = android.view.View.VISIBLE
                unreadBadge.text = chat.unreadCount.toString()
            } else {
                unreadBadge.visibility = android.view.View.GONE
            }

            // Click listener to open ChatScreen
            chatContainer.setOnClickListener {
                val intent = Intent(context, ChatScreen::class.java).apply {
                    putExtra("user_name", chat.userName)
                    putExtra("user_email", chat.userEmail)
                    putExtra("chat_id", chat.id)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatListViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.chat_list_row, parent, false)
        return ChatListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    override fun getItemCount(): Int = chats.size
}

