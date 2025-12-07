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
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.model.Chat
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.launch

class ChatList : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var titleText: TextView
    private lateinit var chatsRecyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ChatListAdapter
    private lateinit var tokenManager: TokenManager
    private val chatsList = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        tokenManager = TokenManager(this)

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

                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@ChatList, "Please login", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val authHeader = "Bearer $token"
                val response = RetrofitClient.apiService.getUserChats(authHeader)

                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val chats = response.body()?.data ?: emptyList()

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
                } else {
                    Toast.makeText(this@ChatList, "Failed to load chats", Toast.LENGTH_SHORT).show()
                    emptyState.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("ChatList", "Error loading chats", e)
                progressBar.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
                Toast.makeText(this@ChatList, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openChat(chat: Chat) {
        val intent = Intent(this, ChatScreen::class.java).apply {
            putExtra("chat_id", chat.id)
        }
        startActivity(intent)
    }
}
