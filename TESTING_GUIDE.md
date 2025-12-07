# 🧪 LocateMe - Complete Testing Guide

## 📋 Pre-Testing Setup

### ✅ Before You Start Testing:
1. **Build the app successfully**
   ```bash
   gradlew.bat clean build
   ```

2. **Install on at least 2 devices/emulators** (for testing chat & notifications)
   - Device A: Your primary test device
   - Device B: Secondary device (for receiving messages/notifications)

3. **Ensure Backend is Running**
   - Backend URL: Check `RetrofitClient.kt` for the base URL
   - Should be deployed on Render (already done)

4. **Check Logcat** - Filter by these tags:
   - `FCMService` - Push notification logs
   - `SyncWorker` - Background sync logs
   - `ChatScreen` - Chat functionality
   - `MessageRepository` - Offline messaging
   - `ItemRepository` - Item caching

---

## 🔥 CRITICAL FEATURES TO TEST

### **1. Authentication & Logout (FCM Token Management)** ⭐

#### Test 1.1: User Registration
```
Steps:
1. Open app
2. Click "Sign Up"
3. Fill in all fields:
   - Full Name: Test User
   - Email: test@student.fast.edu.pk
   - Student ID: 23I-1234
   - Batch: 2023
   - Department: Computer Science
   - Section: A
   - Password: test123
4. Click "Create Account"

Expected Results:
✅ User registered successfully
✅ Toast: "Account created successfully! Welcome Test User"
✅ Redirected to Home screen
✅ Check Logcat: "✅ FCM token sent to backend"
```

#### Test 1.2: User Login
```
Steps:
1. Logout (if logged in)
2. Click "Login"
3. Enter credentials
4. Click "Login"

Expected Results:
✅ Login successful
✅ Check Logcat: "FCM Token: ..."
✅ Check Logcat: "✅ FCM token sent to backend"
```

#### Test 1.3: Logout & FCM Token Cleanup ⭐⭐⭐
```
Steps:
1. Login on Device A
2. Go to Profile → Sign Out
3. Check Logcat on Device A
4. On Device B: Send message to Device A's user

Expected Results:
✅ Device A Logcat shows: "✅ FCM token cleared from backend"
✅ Device A does NOT receive push notification
✅ After logout, no notifications should come
```

---

### **2. Push Notifications (FCM)** ⭐⭐⭐

#### Test 2.1: Basic Push Notification
```
Setup:
- Device A: Login as User 1
- Device B: Login as User 2

Steps:
1. Device B: Post an item (Lost Phone)
2. Device A: Click on the item → Click "Message" button
3. Device A: Send message "Is this still available?"
4. Device B: Should be on Home screen (NOT in chat)

Expected Results:
✅ Device B receives push notification
✅ Notification shows:
   - Title: "User 1's Name"
   - Body: "Is this still available?"
   - Large Icon: User 1's profile picture (circular)
   - Small Icon: Bell icon
✅ Notification appears in status bar
✅ Check Logcat on Device B: "Message received from: ..."
```

#### Test 2.2: Notification Click Action
```
Steps:
1. Receive notification (from Test 2.1)
2. Click on notification

Expected Results:
✅ App opens
✅ ChatScreen opens with correct chat
✅ Message is visible in chat
✅ Notification disappears from status bar
```

#### Test 2.3: Notification Suppression (In Chat) ⭐⭐⭐
```
Steps:
1. Device B: Open chat with User 1
2. Device A: Send message "Hello there"
3. Device B: STAY IN the chat screen

Expected Results:
✅ Message appears in chat immediately (3-second polling)
✅ NO push notification shown
✅ Check Logcat on Device B: "User is in this chat, suppressing notification"
```

#### Test 2.4: Notification Resume (Leave Chat)
```
Steps:
1. Device B: Press back button (exit chat)
2. Device B: Go to Home screen
3. Device A: Send another message "Are you there?"

Expected Results:
✅ Device B receives push notification again
✅ Notification works normally
```

#### Test 2.5: Profile Picture in Notification
```
Steps:
1. Device A: Update profile picture
2. Device A: Send message to Device B
3. Device B: Check notification

Expected Results:
✅ Notification shows Device A's NEW profile picture
✅ Profile picture is circular
✅ Profile picture loads from URL correctly
```

---

### **3. Offline Messaging & Background Sync** ⭐⭐⭐

#### Test 3.1: Send Message Offline
```
Steps:
1. Device A: Open chat with User 2
2. Device A: Turn OFF WiFi and Mobile Data
3. Device A: Type message "Testing offline"
4. Device A: Click Send

Expected Results:
✅ Message appears in chat immediately
✅ Toast: "Message queued. Will send when online."
✅ Message shows in UI with local timestamp
✅ Check Logcat: "💾 Message saved locally with ID: local_..."
✅ Check Logcat: "📤 Message queued for sync"
```

#### Test 3.2: Auto-Sync When Internet Returns
```
Steps:
1. Continue from Test 3.1
2. Wait 5 seconds
3. Device A: Turn ON WiFi
4. Wait 10-15 seconds

Expected Results:
✅ Check Logcat: "🌐 Internet connection detected, triggering sync..."
✅ Check Logcat: "🔄 SyncWorker started"
✅ Check Logcat: "✅ Message synced successfully: [server_id]"
✅ Message indicator changes (if you have UI indicator)
✅ Device B receives the message
✅ Device B receives push notification
```

#### Test 3.3: Multiple Offline Messages
```
Steps:
1. Device A: Turn OFF internet
2. Device A: Send 5 messages in chat
3. Device A: Turn ON internet
4. Wait 15 seconds

Expected Results:
✅ All 5 messages appear in UI immediately (offline)
✅ Toast shown for each: "Message queued..."
✅ When online: All 5 sync in order
✅ Check Logcat: "📦 Found 5 pending operations"
✅ Check Logcat: "✅ Sync completed: 5 success, 0 failed"
✅ Device B receives all 5 messages
```

#### Test 3.4: Background Sync (App Closed) ⭐⭐⭐
```
Steps:
1. Device A: Turn OFF internet
2. Device A: Send message "Background test"
3. Device A: Close app completely (swipe away from recents)
4. Device A: Turn ON internet
5. Wait 20 seconds
6. Device A: Open app again

Expected Results:
✅ Message has synced (even though app was closed)
✅ Check Logcat after reopening: Message should have SYNCED status
✅ Device B received the message while Device A was closed
```

#### Test 3.5: Offline Image Message
```
Steps:
1. Device A: Turn OFF internet
2. Device A: In chat, click attach image
3. Device A: Select an image
4. Device A: Image converts to base64

Expected Results:
✅ Image appears in chat (from local base64)
✅ Toast: "Image queued. Will send when online."
✅ When internet ON: Image syncs to server
✅ Device B receives image
```

---

### **4. Offline Data Storage & Cache** ⭐⭐

#### Test 4.1: Items Cache
```
Steps:
1. Device A: Login, browse home feed (load items)
2. Device A: Turn OFF internet
3. Device A: Kill app, reopen
4. Device A: Check home feed

Expected Results:
✅ Items load INSTANTLY from cache
✅ No loading spinner
✅ All previously loaded items visible
✅ Search works on cached items
✅ Filters work on cached items
```

#### Test 4.2: Profile Cache
```
Steps:
1. Device A: Open Profile screen (while online)
2. Device A: Turn OFF internet
3. Device A: Kill app, reopen
4. Device A: Go to Profile

Expected Results:
✅ Profile loads from cache
✅ Profile picture shows
✅ Stats show (items posted, resolved, success rate)
✅ All profile data visible
```

#### Test 4.3: Messages Cache
```
Steps:
1. Device A: Open chat (while online, load messages)
2. Device A: Turn OFF internet
3. Device A: Kill app, reopen
4. Device A: Open same chat

Expected Results:
✅ Previous messages load instantly from cache
✅ Can scroll through message history
✅ Images in messages load from cache
```

#### Test 4.4: Sync Fresh Data When Online
```
Steps:
1. Device A: Online, browse home (items cached)
2. Device B: Post new item "New Lost Wallet"
3. Device A: Pull to refresh (or wait for auto-refresh)

Expected Results:
✅ Device A sees new item from Device B
✅ Cache updates in background
✅ New item appears in feed
```

---

### **5. Chat Functionality** ⭐

#### Test 5.1: Text Messages
```
Steps:
1. Device A: Send text message
2. Device B: Receive and reply

Expected Results:
✅ Messages appear immediately on sender side
✅ Messages appear within 3 seconds on receiver side
✅ Messages in correct order
✅ Sender on right, receiver on left
```

#### Test 5.2: Image Messages
```
Steps:
1. Device A: Click attach image
2. Select image from gallery
3. Image sends

Expected Results:
✅ Image appears in chat
✅ Device B receives image
✅ Image clickable/viewable
✅ Image loads from URL correctly
```

#### Test 5.3: Message Deletion (Within 5 Minutes)
```
Steps:
1. Device A: Send message
2. Device A: Immediately long-press message
3. Click "Delete"

Expected Results:
✅ Confirmation dialog appears
✅ After delete: Message removed from chat
✅ Device B's chat also updates (on next refresh)
```

#### Test 5.4: Message Deletion (After 5 Minutes)
```
Steps:
1. Device A: Send message
2. Wait 6 minutes
3. Long-press message

Expected Results:
✅ Toast: "Messages can only be deleted within 5 minutes"
✅ Message NOT deleted
```

#### Test 5.5: Read Receipts
```
Steps:
1. Device A: Send message
2. Device B: Open chat (view message)

Expected Results:
✅ Message marked as read in database
✅ (If you have UI indicator, it should update)
```

---

### **6. Items Management** ⭐

#### Test 6.1: Post Lost Item
```
Steps:
1. Click "Report" from bottom nav
2. Select "Lost"
3. Fill details:
   - Title: Lost iPhone 13
   - Description: Black iPhone, lost in library
   - Category: ELECTRONICS
   - Location: Main Library
4. Add 2-3 images
5. Click Submit

Expected Results:
✅ Item created successfully
✅ Appears in "My Items"
✅ Visible to other users in home feed
```

#### Test 6.2: Save/Bookmark Item
```
Steps:
1. Device A: Browse home feed
2. Click bookmark icon on an item

Expected Results:
✅ Bookmark icon fills (changes from outline to filled)
✅ Item appears in "Saved Items" screen
✅ Toast: "Item saved successfully"
```

#### Test 6.3: Unsave Item
```
Steps:
1. Click bookmark icon again (on saved item)

Expected Results:
✅ Bookmark icon becomes outline
✅ Item removed from "Saved Items"
✅ Toast: "Item unsaved successfully"
```

#### Test 6.4: Filter Items
```
Steps:
1. Home screen → Use filters
2. Select "LOST" only
3. Select "ELECTRONICS" category

Expected Results:
✅ Only LOST items shown
✅ Only ELECTRONICS items shown
✅ Filters work together
```

#### Test 6.5: Search Items
```
Steps:
1. Home screen → Click search
2. Type "iPhone"

Expected Results:
✅ Only items with "iPhone" in title/description shown
✅ Search works on title AND description AND location
```

---

### **7. Network State Handling** ⭐

#### Test 7.1: No Internet on App Launch
```
Steps:
1. Turn OFF internet
2. Launch app
3. Browse screens

Expected Results:
✅ App doesn't crash
✅ Shows cached data
✅ UI indicates offline mode (optional)
✅ Actions queue for sync
```

#### Test 7.2: Internet Lost During Usage
```
Steps:
1. Using app (online)
2. Turn OFF internet mid-session
3. Try actions (send message, save item)

Expected Results:
✅ App continues working
✅ Offline actions queue
✅ Toast notifications inform user
```

#### Test 7.3: Internet Restored
```
Steps:
1. Continue from Test 7.2
2. Turn ON internet

Expected Results:
✅ Auto-sync triggered
✅ Queued actions execute
✅ No user intervention needed
```

---

## 🎯 PRIORITY TEST SCENARIOS

### **TOP 3 Must-Test Features:**

#### 🥇 **#1: Offline Messaging + Background Sync**
```
Why: This is your most advanced feature
Test: Send message offline → Close app → Turn on internet → Message syncs
Expected: Works like WhatsApp
```

#### 🥈 **#2: FCM Notifications + Suppression**
```
Why: Shows intelligent notification handling
Test: Get notification → Click → Opens chat
Test: In chat → Send message → No notification
Expected: Smart like WhatsApp/Messenger
```

#### 🥉 **#3: Logout FCM Cleanup**
```
Why: Security & proper token management
Test: Logout → Other user sends message → No notification
Expected: Zero notifications after logout
```

---

## 📊 Expected Logcat Output

### **Successful FCM Token Registration:**
```
LoginSignup: FCM Token: eXaMpLe_ToKeN_HeRe
LoginSignup: ✅ FCM token sent to backend
```

### **Successful Logout:**
```
Profile: ✅ FCM token cleared from backend
```

### **Push Notification Received:**
```
FCMService: Message received from: ...
FCMService: Message data: {chatId=123, senderId=456, ...}
FCMService: Chat ID: 123, Current Chat: null
```

### **Notification Suppressed (In Chat):**
```
FCMService: Chat ID: 123, Current Chat: 123
FCMService: User is in this chat, suppressing notification
```

### **Offline Message Queued:**
```
MessageRepository: 💾 Message saved locally with ID: local_abc123
MessageRepository: 📤 Message queued for sync
MessageRepository: 🔄 Background sync scheduled
```

### **Background Sync Running:**
```
NetworkChangeReceiver: 🌐 Internet connection detected, triggering sync...
SyncWorker: 🔄 SyncWorker started
SyncWorker: 📦 Found 3 pending operations
SyncWorker: ✅ Message synced successfully: 789
SyncWorker: ✅ Sync completed: 3 success, 0 failed
```

---

## ❌ Common Issues & Solutions

### **Issue: No Notifications Received**
**Check:**
- [ ] `google-services.json` in `app/` folder
- [ ] Package name matches Firebase console
- [ ] POST_NOTIFICATIONS permission granted (Android 13+)
- [ ] FCM token sent to backend (check Logcat)
- [ ] Backend has valid FCM credentials

### **Issue: Offline Sync Not Working**
**Check:**
- [ ] ACCESS_NETWORK_STATE permission granted
- [ ] WorkManager constraints (requires internet)
- [ ] Check Logcat for SyncWorker errors
- [ ] Verify sync_queue table has pending items

### **Issue: App Crashes on Offline**
**Check:**
- [ ] NetworkUtils.isOnline() called before API calls
- [ ] Repository handles offline gracefully
- [ ] UI shows cached data when offline

---

## ✅ Final Checklist

Before submitting/demo:
- [ ] All 3 priority features tested and working
- [ ] Push notifications work correctly
- [ ] Offline messaging syncs in background
- [ ] Logout clears FCM token
- [ ] No crashes when offline
- [ ] Chat works with text and images
- [ ] Items can be posted, saved, searched
- [ ] Profile loads and updates correctly
- [ ] Background sync works when app is closed

---

## 🎬 Demo Script (5 Minutes)

**Perfect order to demonstrate features:**

1. **Login** (30s) - Show FCM token registration in Logcat
2. **Browse Items** (30s) - Show home feed, search, filters
3. **Post Item** (30s) - Create lost item with images
4. **Chat** (60s) - Message someone about an item
5. **Push Notification** (60s) - Receive notification from other device
6. **Notification Suppression** (30s) - Show no notification when in chat
7. **Offline Messaging** (90s) - Send offline → Turn on internet → Auto-sync
8. **Logout** (30s) - Show FCM cleanup in Logcat

**Total: ~5 minutes of impressive features!**

---

**Good luck with testing! 🚀**

