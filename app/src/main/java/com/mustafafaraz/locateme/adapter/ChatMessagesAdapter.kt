package com.mustafafaraz.locateme.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mustafafaraz.locateme.data.model.ChatMessage
import com.mustafafaraz.locateme.R
import com.mustafafaraz.locateme.utils.TimeFormatter

class ChatMessagesAdapter(
    private val context: Context,
    private val messages: MutableList<ChatMessage>,
    private val currentUserId: Int,
    private val onMessageLongClick: (ChatMessage) -> Unit
) : RecyclerView.Adapter<ChatMessagesAdapter.ChatMessageViewHolder>() {

    inner class ChatMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val messageImage: ImageView = itemView.findViewById(R.id.message_image)
        private val messageTimestamp: TextView = itemView.findViewById(R.id.message_timestamp)
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.message_container)
        private val rowContainer: LinearLayout = itemView.findViewById(R.id.root_container)

        fun bind(message: ChatMessage) {
            // Determine if message is sent or received
            val isSent = message.senderId == currentUserId

            // Format timestamp
            messageTimestamp.text = TimeFormatter.formatTime(message.createdAt)

            // Handle message type (TEXT or IMAGE)
            if (message.type == "TEXT") {
                messageText.visibility = View.VISIBLE
                messageImage.visibility = View.GONE
                messageText.text = message.content
            } else if (message.type == "IMAGE") {
                messageText.visibility = View.GONE
                messageImage.visibility = View.VISIBLE

                // Load image
                Glide.with(context)
                    .load(message.mediaUrl)
                    .placeholder(R.drawable.item_placeholder)
                    .error(R.drawable.item_placeholder)
                    .centerCrop()
                    .into(messageImage)
            }

            // Style based on sent/received
            if (isSent) {
                rowContainer.gravity = Gravity.END
                messageContainer.setBackgroundResource(R.drawable.chat_sent_background)
                messageText.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                messageTimestamp.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            } else {
                rowContainer.gravity = Gravity.START
                messageContainer.setBackgroundResource(R.drawable.chat_received_background)
                messageText.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                messageTimestamp.setTextColor(ContextCompat.getColor(context, R.color.text_color))
            }

            // Long click listener for deletion (only for sent messages)
            if (isSent) {
                messageContainer.setOnLongClickListener {
                    onMessageLongClick(message)
                    true
                }
            } else {
                messageContainer.setOnLongClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.chat_message_row, parent, false)
        return ChatMessageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun removeMessage(messageId: Int) {
        val position = messages.indexOfFirst { it.id == messageId }
        if (position != -1) {
            messages.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }
}