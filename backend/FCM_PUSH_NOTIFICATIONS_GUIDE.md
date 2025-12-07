# FCM Push Notifications - Complete Setup Guide

## 🎯 Overview

Your backend is **ALREADY FULLY SET UP** for FCM! When a user sends a message, the backend automatically:
- ✅ Gets receiver's FCM token from database
- ✅ Sends push notification with sender's name, message, and profile pic
- ✅ Includes chat data for navigation
- ✅ Stores notification in database

**What you need to do:** Set up Firebase project and implement Android FCM handling.

---

## 📋 Step-by-Step Setup Guide

### STEP 1: Firebase Console Setup

1. **Go to Firebase Console**
   - Visit: https://console.firebase.google.com/
   - Sign in with your Google account

2. **Create/Select Project**
   - If you already have a Firebase project for this app → Use it
   - If not → Click "Add project" → Name it "LocateMe" → Follow wizard

3. **Add Android App**
   - In project dashboard → Click Android icon
   - **Package name:** Must match your Android app package (e.g., `com.yourname.locateme`)
   - **App nickname:** "LocateMe Android" (optional)
   - **Debug signing certificate SHA-1:** Optional for now
   - Click "Register app"

4. **Download google-services.json**
   - Firebase will provide `google-services.json` file
   - **IMPORTANT:** Download this file
   - Place it in your Android project: `app/google-services.json`
   - This file contains your Firebase configuration

5. **Get Service Account Key (Backend Already Has This)**
   - Go to Project Settings → Service Accounts
   - Click "Generate new private key"
   - Download the JSON file
   - **You already have this:** `locateme-205af-firebase-adminsdk-fbsvc-89013a7086.json`
   - This is already in your backend folder ✅

6. **Verify Backend Environment Variables**
   Check your `.env` file on Render has these (extracted from the service account JSON):
   ```
   FIREBASE_PROJECT_ID=locateme-205af
   FIREBASE_CLIENT_EMAIL=firebase-adminsdk-fbsvc@locateme-205af.iam.gserviceaccount.com
   FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"
   ```
   ✅ This is already set up based on your fcm.js file!

---

### STEP 2: Android Project Setup

#### 2.1 Add Google Services Plugin

**In `build.gradle` (Project level):**
```gradle
buildscript {
    dependencies {
        // Add this line
        classpath 'com.google.gms:google-services:4.4.0'
    }
}
```

**In `build.gradle` (App level) - at the BOTTOM:**
```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

android {
    // ... your existing config
}

dependencies {
    // Add Firebase dependencies
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-messaging-ktx'
    implementation 'com.google.firebase:firebase-analytics-ktx'
    
    // ... your existing dependencies
}

// Add this at the very bottom
apply plugin: 'com.google.gms.google-services'
```

#### 2.2 Add Permissions to AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Add these permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
    
    <application>
        <!-- Add FCM Service -->
        <service
            android:name=".services.MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

        <!-- Add notification channel (for Android 8+) -->
        <meta-data
            android:name="com.google.firebase.messaging.default_notification_channel_id"
            android:value="chat_messages" />
            
        <!-- Your activities... -->
    </application>
</manifest>
```

---

### STEP 3: Create Firebase Messaging Service

**Create file:** `app/src/main/java/com/yourpackage/services/MyFirebaseMessagingService.kt`

```kotlin
package com.yourpackage.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yourpackage.R
import com.yourpackage.activities.ChatActivity
import com.yourpackage.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

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
        
        // Save token locally
        PreferencesManager.saveFcmToken(this, token)
        
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
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("CHAT_ID", chatId)
            putExtra("OTHER_USER_ID", senderId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            chatId, // Unique request code per chat
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Make sure you have this icon
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
        // Call your API to update FCM token
        // This should be done when user logs in as well
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use your Retrofit service
                // retrofitService.updateFcmToken(token).execute()
                Log.d(TAG, "Token sent to server: $token")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send token to server", e)
            }
        }
    }
}
```

---

### STEP 4: Create PreferencesManager Helper

**Create file:** `app/src/main/java/com/yourpackage/utils/PreferencesManager.kt`

```kotlin
package com.yourpackage.utils

import android.content.Context

object PreferencesManager {
    private const val PREF_NAME = "LocateMePrefs"
    private const val KEY_FCM_TOKEN = "fcm_token"
    private const val KEY_USER_ID = "user_id"
    
    fun saveFcmToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }
    
    fun getFcmToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_FCM_TOKEN, null)
    }
    
    fun saveUserId(context: Context, userId: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }
    
    fun getUserId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_USER_ID, -1)
    }
}
```

---

### STEP 5: Update ChatActivity to Suppress Notifications

**In your ChatActivity.kt:**

```kotlin
class ChatActivity : AppCompatActivity() {
    
    private var chatId: Int = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        chatId = intent.getIntExtra("CHAT_ID", -1)
        
        // Rest of your code...
    }
    
    override fun onResume() {
        super.onResume()
        // Tell FCM service we're in this chat - suppress notifications
        MyFirebaseMessagingService.currentChatId = chatId
    }
    
    override fun onPause() {
        super.onPause()
        // We left the chat - allow notifications again
        MyFirebaseMessagingService.currentChatId = null
    }
}
```

---

### STEP 6: Request FCM Token and Send to Backend

**In your MainActivity or LoginActivity (after successful login):**

```kotlin
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // ... your login code
    }
    
    private fun onLoginSuccess(userId: Int, token: String) {
        // Save user session
        PreferencesManager.saveUserId(this, userId)
        
        // Get FCM token and send to backend
        getFcmTokenAndSendToServer()
        
        // Navigate to home
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    private fun getFcmTokenAndSendToServer() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            
            // Get FCM token
            val fcmToken = task.result
            Log.d("FCM", "FCM Token: $fcmToken")
            
            // Save locally
            PreferencesManager.saveFcmToken(this, fcmToken)
            
            // Send to backend
            sendFcmTokenToBackend(fcmToken)
        }
    }
    
    private fun sendFcmTokenToBackend(fcmToken: String) {
        val request = UpdateFcmTokenRequest(fcmToken)
        
        retrofitService.updateFcmToken(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    Log.d("FCM", "FCM token sent to backend successfully")
                } else {
                    Log.e("FCM", "Failed to send FCM token: ${response.code()}")
                }
            }
            
            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Log.e("FCM", "Error sending FCM token", t)
            }
        })
    }
}

data class UpdateFcmTokenRequest(val fcmToken: String)
```

---

### STEP 7: Add Notification Icon

**Create notification icon:**
1. Right-click `res` folder → New → Image Asset
2. Select "Notification Icons"
3. Configure your icon (should be white on transparent)
4. Name it `ic_notification`
5. Click Finish

Or use your app icon temporarily:
- Rename your launcher icon to `ic_notification` in drawable folders

---

### STEP 8: Request Notification Permission (Android 13+)

**In MainActivity or first screen:**

```kotlin
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permissions", "Notification permission granted")
        } else {
            Log.d("Permissions", "Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
```

---

### STEP 9: Update Retrofit Service Interface

**Add FCM token endpoint to your ApiService:**

```kotlin
@PUT("auth/fcm-token")
fun updateFcmToken(@Body request: UpdateFcmTokenRequest): Call<GenericResponse>
```

---

## 🎯 How It Works (Flow Diagram)

```
USER A sends message
         ↓
Backend receives message
         ↓
Backend saves message to database
         ↓
Backend gets USER B's FCM token from database
         ↓
Backend sends notification via FCM
         ↓
FCM delivers to USER B's device
         ↓
MyFirebaseMessagingService receives notification
         ↓
Check: Is USER B in this chat?
    ├─ YES → Suppress notification (user already sees message)
    └─ NO  → Show notification with profile pic and message
         ↓
USER B clicks notification
         ↓
ChatActivity opens with correct chat
```

---

## ✅ Testing Checklist

### Test 1: FCM Token Registration
- [ ] Login to app
- [ ] Check backend logs: FCM token should be saved to database
- [ ] Verify in database: User record has fcm_token column populated

### Test 2: Notification When App is Closed
- [ ] User A and User B both logged in
- [ ] User B closes app completely
- [ ] User A sends message to User B
- [ ] User B should receive notification
- [ ] Notification should show: Profile pic, name, message
- [ ] Click notification → Chat opens

### Test 3: Notification When App is in Background
- [ ] User B has app in background (on home screen)
- [ ] User A sends message
- [ ] User B receives notification
- [ ] Click → Opens chat

### Test 4: NO Notification When Already in Chat
- [ ] User B is in chat with User A (ChatActivity open)
- [ ] User A sends message
- [ ] User B should NOT receive notification
- [ ] Message appears in chat immediately (via polling)

### Test 5: Image Messages
- [ ] User A sends image
- [ ] User B receives notification: "📷 Sent an image"

---

## 🐛 Troubleshooting

### Problem: Not receiving notifications

**Check:**
1. **Firebase Console:** Is Android app added with correct package name?
2. **google-services.json:** Is it in the correct location (`app/google-services.json`)?
3. **FCM Token:** Is it being sent to backend after login?
4. **Database:** Does user have fcm_token in database?
5. **Backend logs:** Any errors when sending notification?
6. **Android Manifest:** Are permissions added?
7. **Notification Permission:** Is it granted (Android 13+)?

### Problem: Notification shows but no image

**Check:**
1. Profile picture URL is accessible
2. Glide dependency is added
3. Internet permission is granted
4. Check logs for image loading errors

### Problem: Clicking notification doesn't open chat

**Check:**
1. PendingIntent flags are correct
2. Chat ID and sender ID are passed correctly
3. ChatActivity handles intents from notification

### Problem: Notifications show even when in chat

**Check:**
1. `MyFirebaseMessagingService.currentChatId` is being set in `onResume()`
2. Being cleared in `onPause()`
3. Chat IDs match exactly

---

## 🚀 Quick Start Summary

### Backend (Already Done ✅)
- FCM already integrated
- Sends notifications when messages are sent
- Includes sender name, message, profile pic
- Includes chat data for navigation

### Firebase Console (Do This First)
1. Create/select project
2. Add Android app
3. Download google-services.json
4. Place in `app/` folder

### Android (Your Work)
1. Add Firebase dependencies to gradle
2. Place google-services.json
3. Create MyFirebaseMessagingService.kt
4. Update AndroidManifest.xml
5. Request notification permission
6. Get FCM token after login
7. Send token to backend
8. Update ChatActivity to suppress notifications

### Total Time Needed
- Firebase setup: 10 minutes
- Android code: 30-45 minutes
- Testing: 15 minutes

---

## 📱 Backend API Endpoints (Already Working)

### Update FCM Token
```
PUT /api/auth/fcm-token
Authorization: Bearer <token>
Body: { "fcmToken": "device_token_here" }
```

### Send Message (Auto-triggers notification)
```
POST /api/messages
Body: { "chatId": 1, "type": "TEXT", "content": "Hello" }
```

Backend automatically:
- Gets receiver's FCM token
- Sends push notification
- Saves notification to database

---

## 🎉 You're All Set!

Your backend is ready. Just complete the Android setup and test! The notifications will work exactly like WhatsApp - showing when user is outside the chat, hiding when they're inside.
