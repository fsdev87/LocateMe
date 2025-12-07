package com.mustafafaraz.locateme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.adapter.ChatListAdapter
import com.mustafafaraz.locateme.data.model.Chat
import com.mustafafaraz.locateme.data.repository.ChatRepository
import com.mustafafaraz.locateme.utils.TokenManager
import com.mustafafaraz.locateme.utils.NetworkUtils
import kotlinx.coroutines.launch

class ChatList : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var titleText: TextView
    private lateinit var chatsRecyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ChatListAdapter
    private lateinit var tokenManager: TokenManager
    private lateinit var chatRepository: ChatRepository
    private val chatsList = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        tokenManager = TokenManager(this)
        chatRepository = ChatRepository(this)

        initializeViews()
        setupRecyclerView()
        loadChats()
    }

    override fun onResume() {
        super.onResume()
        // Reload chats when returning from chat screen
        loadChats()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        titleText = findViewById(R.id.title_text)
        chatsRecyclerView = findViewById(R.id.chats_recyclerview)
        emptyState = findViewById(R.id.empty_state)
        progressBar = findViewById(R.id.progress_bar)

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter(this, chatsList) { chat ->
            openChat(chat)
        }
        chatsRecyclerView.layoutManager = LinearLayoutManager(this)
        chatsRecyclerView.adapter = adapter
    }

    private fun loadChats() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                emptyState.visibility = View.GONE
                chatsRecyclerView.visibility = View.GONE

                // Check if online
                if (NetworkUtils.isOnline(this@ChatList)) {
                    // ONLINE: Fetch from API first
                    val result = chatRepository.syncChats()

                    result.onSuccess { chats ->
                        progressBar.visibility = View.GONE

                        if (chats.isEmpty()) {
                            emptyState.visibility = View.VISIBLE
                            chatsRecyclerView.visibility = View.GONE
                        } else {
                            emptyState.visibility = View.GONE
                            chatsRecyclerView.visibility = View.VISIBLE
                            chatsList.clear()
                            chatsList.addAll(chats)
                            adapter.notifyDataSetChanged()
                        }
                        Log.d("ChatList", "✅ Loaded ${chats.size} chats from server")
                    }.onFailure { error ->
                        // API failed, load from cache
                        Log.e("ChatList", "API failed, loading from cache: ${error.message}")
                        loadChatsFromCache()
                    }
                } else {
                    // OFFLINE: Load from cache
                    Log.d("ChatList", "📴 Offline mode - loading from cache")
                    loadChatsFromCache()
                }
            } catch (e: Exception) {
                Log.e("ChatList", "Error loading chats", e)
                progressBar.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
                Toast.makeText(this@ChatList, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun loadChatsFromCache() {
        val cachedChats = chatRepository.getAllChatsOnce()

        progressBar.visibility = View.GONE

        if (cachedChats.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            chatsRecyclerView.visibility = View.GONE
            if (!NetworkUtils.isOnline(this@ChatList)) {
                Toast.makeText(this@ChatList, "No cached chats. Connect to internet to load.", Toast.LENGTH_SHORT).show()
            }
        } else {
            emptyState.visibility = View.GONE
            chatsRecyclerView.visibility = View.VISIBLE
            chatsList.clear()
            chatsList.addAll(cachedChats)
            adapter.notifyDataSetChanged()

            if (!NetworkUtils.isOnline(this@ChatList)) {
                Toast.makeText(this@ChatList, "Offline mode - showing cached chats", Toast.LENGTH_SHORT).show()
            }
        }
        Log.d("ChatList", "Loaded ${cachedChats.size} chats from cache")
    }

    private fun openChat(chat: Chat) {
        val intent = Intent(this, ChatScreen::class.java).apply {
            putExtra("chat_id", chat.id)
        }
        startActivity(intent)
    }
}
