# FCM Quick Reference Card

## ✅ What's Already Done (Backend)

Your backend is **100% ready** for push notifications!

- ✅ FCM SDK initialized (`utils/fcm.js`)
- ✅ Sends notifications automatically when messages are sent
- ✅ Includes sender name, message text, and profile picture
- ✅ Passes chat data for opening correct chat
- ✅ API endpoint to update FCM token: `PUT /api/auth/fcm-token`

**You don't need to change anything on the backend!**

---

## 🎯 What You Need to Do (3 Main Steps)

### 1️⃣ FIREBASE CONSOLE (5 minutes)
1. Go to https://console.firebase.google.com/
2. Create/select your project
3. Add Android app with your package name
4. Download `google-services.json`
5. Place in `app/google-services.json`

### 2️⃣ ANDROID CODE (30 minutes)
1. Add dependencies to `build.gradle`
2. Create `MyFirebaseMessagingService.kt`
3. Update `AndroidManifest.xml`
4. Get FCM token after login and send to backend
5. Suppress notifications in `ChatActivity` when user is in chat

### 3️⃣ TEST (10 minutes)
1. Login on 2 devices
2. Send message from device A
3. Device B receives notification
4. Click notification → opens chat
5. Open chat → send message → no notification shown

---

## 📝 Code Snippets You Need

### After Login - Get and Send FCM Token
```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    val fcmToken = task.result
    // Send to backend: PUT /api/auth/fcm-token
    retrofitService.updateFcmToken(UpdateFcmTokenRequest(fcmToken))
}
```

### In ChatActivity - Suppress Notifications
```kotlin
override fun onResume() {
    super.onResume()
    MyFirebaseMessagingService.currentChatId = chatId
}

override fun onPause() {
    super.onPause()
    MyFirebaseMessagingService.currentChatId = null
}
```

### Notification Service - Show Notification
```kotlin
class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        var currentChatId: Int? = null // Set by ChatActivity
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        val chatId = message.data["chatId"]?.toIntOrNull() ?: -1
        
        // Don't show if user is in this chat
        if (chatId == currentChatId) return
        
        // Show notification
        showNotification(...)
    }
}
```

---

## 🔍 How Notifications Work

```
User A sends message
    ↓
Backend saves message
    ↓
Backend gets User B's FCM token from database
    ↓
Backend sends notification via Firebase
    ↓
Firebase delivers to User B's phone
    ↓
MyFirebaseMessagingService receives it
    ↓
If User B is in that chat → DO NOTHING
If User B is NOT in that chat → SHOW NOTIFICATION
    ↓
User B clicks notification → ChatActivity opens
```

---

## 📚 Full Guide Location

See complete step-by-step guide: `FCM_PUSH_NOTIFICATIONS_GUIDE.md`

Includes:
- Detailed Firebase setup
- Complete Android code
- Troubleshooting guide
- Testing checklist
- Common problems and solutions

---

## 🚨 Important Notes

1. **Backend is ready** - No changes needed!
2. **FCM token sent after login** - Very important!
3. **Suppress in ChatActivity** - Prevents duplicate notifications
4. **Android 13+ needs permission** - Request POST_NOTIFICATIONS
5. **Test on real device** - Emulator might not work perfectly

---

## 🎉 Summary

**Backend:** ✅ Done
**Firebase Console:** 👉 You do this (5 min)
**Android Code:** 👉 You do this (30 min)
**Testing:** 👉 You do this (10 min)

**Total time:** ~45 minutes to get notifications working!
