package com.mustafafaraz.locateme.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mustafafaraz.locateme.ChatScreen
import com.mustafafaraz.locateme.R
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.model.UpdateFcmTokenRequest
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID = "chat_messages"
        const val CHANNEL_NAME = "Chat Messages"

        // Track current chat to suppress notifications
        var currentChatId: Int? = null
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")

        // Send token to backend
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")
        Log.d(TAG, "Message data: ${message.data}")

        // Extract data
        val title = message.notification?.title ?: message.data["title"] ?: "New Message"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val imageUrl = message.data["image"]
        val chatId = message.data["chatId"]?.toIntOrNull() ?: -1
        val senderId = message.data["senderId"]?.toIntOrNull() ?: -1

        Log.d(TAG, "Chat ID: $chatId, Current Chat: $currentChatId")

        // DON'T show notification if user is already in this chat
        if (chatId == currentChatId) {
            Log.d(TAG, "User is in this chat, suppressing notification")
            return
        }

        // Show notification
        showNotification(title, body, imageUrl, chatId, senderId)
    }

    private fun showNotification(
        title: String,
        body: String,
        imageUrl: String?,
        chatId: Int,
        senderId: Int
    ) {
        createNotificationChannel()

        // Create intent to open chat when notification is clicked
        val intent = Intent(this, ChatScreen::class.java).apply {
            putExtra("chat_id", chatId)
            putExtra("other_user_id", senderId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            chatId, // Unique request code per chat
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        // Load and set large icon (profile picture) asynchronously
        if (!imageUrl.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bitmap = Glide.with(applicationContext)
                        .asBitmap()
                        .load(imageUrl)
                        .circleCrop()
                        .submit()
                        .get()

                    notificationBuilder.setLargeIcon(bitmap)

                    // Show notification on main thread
                    CoroutineScope(Dispatchers.Main).launch {
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(chatId, notificationBuilder.build())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading notification image", e)
                    showNotificationWithoutImage(chatId, notificationBuilder)
                }
            }
        } else {
            showNotificationWithoutImage(chatId, notificationBuilder)
        }
    }

    private fun showNotificationWithoutImage(chatId: Int, builder: NotificationCompat.Builder) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(chatId, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new chat messages"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tokenManager = TokenManager(applicationContext)
                val authToken = tokenManager.getToken()

                if (!authToken.isNullOrEmpty()) {
                    val authHeader = "Bearer $authToken"
                    val request = UpdateFcmTokenRequest(token)
                    val response = RetrofitClient.apiService.updateFcmToken(authHeader, request)

                    if (response.isSuccessful) {
                        Log.d(TAG, "✅ FCM token sent to server successfully")
                    } else {
                        Log.e(TAG, "❌ Failed to send FCM token: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending FCM token to server", e)
            }
        }
    }
}