package com.mustafafaraz.locateme.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.ChatMessage
import com.mustafafaraz.locateme.R

class ChatMessagesAdapter(
    private val context: Context,
    private val messages: List<ChatMessage>
) : RecyclerView.Adapter<ChatMessagesAdapter.ChatMessageViewHolder>() {

    inner class ChatMessageViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val messageTimestamp: TextView = itemView.findViewById(R.id.message_timestamp)
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.message_container)

        private val rowContainer: LinearLayout = itemView.findViewById(R.id.root_container)
        fun bind(message: ChatMessage) {
            messageText.text = message.messageText
            messageTimestamp.text = message.timestamp


            // Style based on message type (sent or received)
            if (message.type == "sent") {
                rowContainer.gravity = Gravity.END

                // Sent messages - blue background, right aligned
                messageContainer.setBackgroundResource(R.drawable.chat_sent_background)
                messageText.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                messageTimestamp.setTextColor(ContextCompat.getColor(context, android.R.color.white))

                val layoutParams = messageContainer.layoutParams as LinearLayout.LayoutParams
                /*layoutParams.marginStart = 80
                layoutParams.marginEnd = 12
                messageContainer.layoutParams = layoutParams*/
            } else {
                rowContainer.gravity = Gravity.START

                // Received messages - gray background, left aligned
                messageContainer.setBackgroundResource(R.drawable.chat_received_background)
                messageText.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                messageTimestamp.setTextColor(ContextCompat.getColor(context, R.color.text_color))

                /*val layoutParams = messageContainer.layoutParams as LinearLayout.LayoutParams
                layoutParams.marginStart = 12
                layoutParams.marginEnd = 80
                messageContainer.layoutParams = layoutParams*/
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatMessageViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.chat_message_row, parent, false)
        return ChatMessageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size
}