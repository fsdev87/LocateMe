package com.mustafafaraz.locateme

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.adapter.ChatMessagesAdapter
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.model.ChatMessage
import com.mustafafaraz.locateme.data.repository.MessageRepository
import com.mustafafaraz.locateme.utils.TokenManager
import com.mustafafaraz.locateme.utils.NetworkUtils
import com.mustafafaraz.locateme.services.MyFirebaseMessagingService
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ChatScreen : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var messageRepository: MessageRepository
    private lateinit var backButton: ImageView
    private lateinit var userName: TextView
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView
    private lateinit var attachImageButton: ImageView
    private lateinit var adapter: ChatMessagesAdapter

    private var chatId: Int = -1
    private var otherUserId: Int = -1
    private var currentUserId: Int = -1
    private val messages = mutableListOf<ChatMessage>()

    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval = 3000L
    private var isRefreshing = false

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_screen)

        tokenManager = TokenManager(this)
        messageRepository = MessageRepository(this)

        // Get current user ID
        lifecycleScope.launch {
            val userIdString = tokenManager.getUserId()
            currentUserId = userIdString?.toIntOrNull() ?: -1
        }

        initializeViews()

        // Get chat info from intent
        chatId = intent.getIntExtra("chat_id", -1)
        val itemId = intent.getIntExtra("item_id", -1)

        if (chatId != -1) {
            // Existing chat - load messages
            loadChatDetails()
            loadMessagesFromCache()
            syncMessages()
        } else if (itemId != -1) {
            // New chat from item
            createChatFromItem(itemId)
        } else {
            Toast.makeText(this, "Invalid chat", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupListeners()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        userName = findViewById(R.id.user_name)
        messagesRecyclerView = findViewById(R.id.messages_recyclerview)
        messageInput = findViewById(R.id.message_input)
        sendButton = findViewById(R.id.send_button)
        attachImageButton = findViewById(R.id.attach_image_button)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = ChatMessagesAdapter(this, messages, currentUserId) { message ->
            handleMessageLongClick(message)
        }
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

        sendButton.setOnClickListener {
            sendTextMessage()
        }

        attachImageButton.setOnClickListener {
            pickImage()
        }
    }

    private fun createChatFromItem(itemId: Int) {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@ChatScreen, "Please login", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val authHeader = "Bearer $token"
                val response = RetrofitClient.apiService.createChatFromItem(authHeader, itemId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val chat = response.body()?.data
                    chat?.let {
                        chatId = it.id
                        otherUserId = it.otherUserId
                        userName.text = it.otherUserName
                        loadMessages()
                        startAutoRefresh()
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to create chat"
                    Toast.makeText(this@ChatScreen, errorMsg, Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error creating chat", e)
                Toast.makeText(this@ChatScreen, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun loadChatDetails() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) return@launch

                val authHeader = "Bearer $token"
                val response = RetrofitClient.apiService.getChatById(authHeader, chatId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val chat = response.body()?.data
                    chat?.let {
                        otherUserId = it.otherUserId
                        userName.text = it.otherUserName
                        startAutoRefresh()
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error loading chat details", e)
            }
        }
    }

    private fun loadMessagesFromCache() {
        lifecycleScope.launch {
            // Subscribe to message Flow - auto-updates when cache changes
            messageRepository.getMessagesByChatId(chatId).collect { cachedMessages ->
                adapter.updateMessages(cachedMessages)
                if (cachedMessages.isNotEmpty()) {
                    scrollToBottom()
                }
            }
        }
    }

    private fun syncMessages() {
        lifecycleScope.launch {
            val result = messageRepository.syncMessages(chatId)
            result.onFailure { error ->
                if (error.message?.contains("No internet") == true) {
                    // Offline mode - showing cached messages
                    Log.d("ChatScreen", "Offline mode - showing cached messages")
                }
            }
        }
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) return@launch

                val authHeader = "Bearer $token"
                val response = RetrofitClient.apiService.getChatMessages(authHeader, chatId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val newMessages = response.body()?.data ?: emptyList()

                    if (newMessages.isNotEmpty()) {
                        adapter.updateMessages(newMessages)
                        scrollToBottom()

                        // Mark messages as read
                        markMessagesAsRead()
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error loading messages", e)
            }
        }
    }

    private fun sendTextMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isEmpty()) return

        lifecycleScope.launch {
            val result = messageRepository.sendMessage(
                chatId = chatId,
                type = "TEXT",
                content = messageText,
                currentUserId = currentUserId
            )

            result.onSuccess {
                // Message already showing in UI from Flow
                messageInput.text.clear()
                scrollToBottom()

                // Show offline indicator if needed
                if (!NetworkUtils.isOnline(this@ChatScreen)) {
                    Toast.makeText(
                        this@ChatScreen,
                        "Message queued. Will send when online.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.onFailure { error ->
                Toast.makeText(
                    this@ChatScreen,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            imageUri?.let {
                sendImageMessage(it)
            }
        }
    }

    private fun sendImageMessage(imageUri: Uri) {
        lifecycleScope.launch {
            try {
                // Convert image to base64
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                val resizedBitmap = resizeBitmap(bitmap, 1024, 1024)
                val base64Image = bitmapToBase64(resizedBitmap)

                val result = messageRepository.sendMessage(
                    chatId = chatId,
                    type = "IMAGE",
                    messageImage = base64Image,
                    currentUserId = currentUserId
                )

                result.onSuccess {
                    scrollToBottom()

                    if (!NetworkUtils.isOnline(this@ChatScreen)) {
                        Toast.makeText(
                            this@ChatScreen,
                            "Image queued. Will send when online.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.onFailure { error ->
                    Toast.makeText(
                        this@ChatScreen,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error sending image", e)
                Toast.makeText(this@ChatScreen, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleMessageLongClick(message: ChatMessage) {
        // Check if message is within 5 minutes
        val messageTime = parseTimestamp(message.createdAt)
        val currentTime = System.currentTimeMillis()
        val timeDiff = (currentTime - messageTime) / 1000 / 60 // minutes

        if (timeDiff > 5) {
            Toast.makeText(this, "Messages can only be deleted within 5 minutes", Toast.LENGTH_SHORT).show()
            return
        }

        // Show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMessage(message)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMessage(message: ChatMessage) {
        lifecycleScope.launch {
            try {
                val result = messageRepository.deleteMessage(message.id)

                result.onSuccess {
                    // Message will be automatically removed from UI via Flow
                    Toast.makeText(this@ChatScreen, "Message deleted", Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Toast.makeText(this@ChatScreen, error.message ?: "Failed to delete message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error deleting message", e)
                Toast.makeText(this@ChatScreen, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun markMessagesAsRead() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) return@launch

                val authHeader = "Bearer $token"
                RetrofitClient.apiService.markMessagesAsRead(authHeader, chatId)
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error marking messages as read", e)
            }
        }
    }

    private fun startAutoRefresh() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isRefreshing) {
                    isRefreshing = true
                    refreshMessages()
                }
                handler.postDelayed(this, refreshInterval)
            }
        }, refreshInterval)
    }

    private fun refreshMessages() {
        lifecycleScope.launch {
            try {
                messageRepository.syncMessages(chatId)
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error refreshing messages", e)
            } finally {
                isRefreshing = false
            }
        }
    }

    private fun scrollToBottom() {
        if (messages.isNotEmpty()) {
            messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
        }
    }

    private fun isAtBottom(): Boolean {
        val layoutManager = messagesRecyclerView.layoutManager as LinearLayoutManager
        val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
        return lastVisiblePosition >= messages.size - 1
    }

    private fun parseTimestamp(timestamp: String): Long {
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            format.parse(timestamp)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    override fun onResume() {
        super.onResume()
        // Suppress notifications for this chat when user is in the chat screen
        MyFirebaseMessagingService.currentChatId = chatId
    }

    override fun onPause() {
        super.onPause()
        // Re-enable notifications when user leaves the chat screen
        MyFirebaseMessagingService.currentChatId = null
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        // Clear the current chat ID
        MyFirebaseMessagingService.currentChatId = null
    }
}
