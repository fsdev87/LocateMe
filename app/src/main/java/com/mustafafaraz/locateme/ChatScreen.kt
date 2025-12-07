package com.mustafafaraz.locateme

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.adapter.ChatMessagesAdapter

class ChatScreen : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var adapter: ChatMessagesAdapter
    private lateinit var messages: MutableList<ChatMessage>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_screen)

        backButton = findViewById(R.id.back_button)
        messagesRecyclerView = findViewById(R.id.messages_recyclerview)

        backButton.setOnClickListener {
            finish()
        }

        // Initialize sample messages
        messages = mutableListOf(
            ChatMessage("Hi, did you find my backpack?", "received", "10:30 AM"),
            ChatMessage("Yes, I found it near the library!", "sent", "10:32 AM"),
            ChatMessage("That's great! When can we meet?", "received", "10:35 AM"),
            ChatMessage("How about at 2 PM near the cafeteria?", "sent", "10:37 AM"),
            ChatMessage("Perfect! See you then", "received", "10:40 AM")
        )

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatMessagesAdapter(this, messages)
        messagesRecyclerView.adapter = adapter
    }
}

