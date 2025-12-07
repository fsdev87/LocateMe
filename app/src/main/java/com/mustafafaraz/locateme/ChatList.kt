package com.mustafafaraz.locateme

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.adapter.ChatListAdapter
import com.mustafafaraz.locateme.data.model.Chat

class ChatList : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var titleText: TextView
    private lateinit var chatsRecyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private lateinit var chatsList: MutableList<Chat>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        backButton = findViewById(R.id.back_button)
        titleText = findViewById(R.id.title_text)
        chatsRecyclerView = findViewById(R.id.chats_recyclerview)

        backButton.setOnClickListener {
            finish()
        }

        // Initialize sample chats
        chatsList = mutableListOf(
            Chat(
                id = "1",
                userName = "John Doe",
                userEmail = "john@example.com",
                lastMessage = "Did you find my backpack?",
                timestamp = "10:30 AM",
                unreadCount = 2,
                userAvatar = R.drawable.ic_person
            ),
            Chat(
                id = "2",
                userName = "Sarah Johnson",
                userEmail = "sarah@example.com",
                lastMessage = "Thanks for finding my keys!",
                timestamp = "Yesterday",
                unreadCount = 0,
                userAvatar = R.drawable.ic_person
            ),
            Chat(
                id = "3",
                userName = "Mike Chen",
                userEmail = "mike@example.com",
                lastMessage = "When can we meet?",
                timestamp = "2:45 PM",
                unreadCount = 1,
                userAvatar = R.drawable.ic_person
            ),
            Chat(
                id = "4",
                userName = "Emma Wilson",
                userEmail = "emma@example.com",
                lastMessage = "Great! See you tomorrow",
                timestamp = "Yesterday",
                unreadCount = 0,
                userAvatar = R.drawable.ic_person
            ),
            Chat(
                id = "5",
                userName = "Alex Thompson",
                userEmail = "alex@example.com",
                lastMessage = "I found something near the library",
                timestamp = "3 days ago",
                unreadCount = 0,
                userAvatar = R.drawable.ic_person
            )
        )

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        chatsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatListAdapter(this, chatsList)
        chatsRecyclerView.adapter = adapter
    }
}

