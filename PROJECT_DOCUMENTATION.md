# LocateMe - Lost & Found App

## 📱 Project Overview
A comprehensive Android lost & found application for university students built with Kotlin, featuring real-time chat, offline-first architecture, and push notifications.

## 🏗️ Architecture

### **Technology Stack**
- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)
- **Backend:** Node.js + Express + MySQL (hosted on Render & Railway)
- **Local Database:** Room (SQLite wrapper)
- **Networking:** Retrofit + OkHttp
- **Image Loading:** Glide
- **Async Operations:** Coroutines + Flow
- **Background Jobs:** WorkManager
- **Push Notifications:** Firebase Cloud Messaging (FCM)
- **Local Storage:** DataStore (for auth tokens)

### **Key Features Implemented**

#### ✅ **1. Authentication & User Management**
- JWT-based authentication
- User registration with student details
- Profile management with image upload
- Password change functionality
- Account deletion (soft delete for items, hard delete for account)
- Logout with FCM token cleanup

#### ✅ **2. Items Management**
- Post lost/found items with images (up to 5 per item)
- Filter by type (LOST/FOUND), category, status
- Search functionality
- Item status management (ACTIVE, RESOLVED, EXPIRED)
- My Items screen
- Save/bookmark items
- Soft delete for items

#### ✅ **3. Real-Time Chat System**
- One-on-one messaging
- Text + image messages
- Message deletion (within 5 minutes)
- Read receipts
- Auto-refresh every 3 seconds
- Chat from item details
- **Offline-first messaging with background sync**

#### ✅ **4. Push Notifications (FCM)**
- Notification on new messages
- Shows sender's profile picture, name, and message
- Click to open specific chat
- Auto-suppression when user is in chat (like WhatsApp)
- FCM token management (send on login, clear on logout)

#### ✅ **5. Offline-First Architecture**
- **Room Database** with 4 tables:
  - `items` - Cache all items locally
  - `user_profile` - Cache user data
  - `messages` - Chat message cache
  - `sync_queue` - Pending operations queue
- **Cache-first strategy:** Show data instantly from local DB
- **Background sync:** WorkManager syncs when online
- **Network detection:** Auto-sync when internet returns
- **Offline messaging:** Send messages offline, auto-sync later

#### ✅ **6. Background Synchronization**
- WorkManager for background jobs
- Exponential backoff retry (max 3 attempts)
- NetworkChangeReceiver for auto-sync
- Sync queue for failed operations
- Works even when app is closed

---

## 📁 Project Structure

```
app/src/main/java/com/mustafafaraz/locateme/
├── adapter/                    # RecyclerView adapters
│   ├── ChatMessagesAdapter
│   ├── ItemsAdapter
│   └── ...
├── data/
│   ├── api/                    # Retrofit API service
│   │   ├── ApiService.kt
│   │   └── RetrofitClient.kt
│   ├── local/                  # Room Database
│   │   ├── AppDatabase.kt
│   │   ├── dao/               # Data Access Objects
│   │   │   ├── ItemDao.kt
│   │   │   ├── MessageDao.kt
│   │   │   ├── SyncQueueDao.kt
│   │   │   └── UserProfileDao.kt
│   │   └── entity/            # Database entities
│   │       ├── ItemEntity.kt
│   │       ├── MessageEntity.kt
│   │       ├── SyncQueueEntity.kt
│   │       └── UserProfileEntity.kt
│   ├── model/                  # API data models
│   │   ├── AuthResponse.kt
│   │   ├── ChatMessage.kt
│   │   ├── Item.kt
│   │   ├── User.kt
│   │   └── ...
│   └── repository/            # Repository pattern
│       ├── ItemRepository.kt
│       └── MessageRepository.kt
├── services/                   # Background services
│   └── MyFirebaseMessagingService.kt
├── receivers/                  # Broadcast receivers
│   └── NetworkChangeReceiver.kt
├── workers/                    # WorkManager workers
│   └── SyncWorker.kt
├── utils/                      # Utilities
│   ├── TokenManager.kt
│   └── NetworkUtils.kt
└── [Activities]               # UI screens
    ├── LoginSignup.kt
    ├── Home.kt
    ├── ChatScreen.kt
    ├── Profile.kt
    ├── Settings.kt
    └── ...
```

---

## 🔄 Data Flow

### **Offline-First Message Flow**
```
User sends message (offline)
    ↓
1. Save to local Room DB (instant UI update)
2. Add to sync_queue table
3. Schedule SyncWorker
4. Show "Message queued" toast
    ↓
When internet returns:
    ↓
5. NetworkChangeReceiver detects connectivity
6. Triggers SyncWorker
7. SyncWorker processes sync_queue
8. Sends message to backend API
9. Updates local message with server ID
10. Other user receives notification
```

### **Cache-First Item Flow**
```
User opens Home screen
    ↓
1. Load from Room DB → Show instantly
2. Check internet connection
3. IF ONLINE: Fetch from API → Update cache → Refresh UI
4. IF OFFLINE: Continue showing cached data
```

---

## 🗄️ Database Schema

### **Room Database Tables**

#### **1. items**
```kotlin
- id (Primary Key)
- userId, title, description
- category, location, type, status
- imageUrls (JSON string)
- userName, userEmail, userProfilePic
- isSaved (Boolean)
- createdAt, updatedAt
- lastSyncedAt (for cache cleanup)
```

#### **2. user_profile**
```kotlin
- id (Primary Key)
- fullName, email, studentId
- batch, department, section
- profilePic
- totalItems, resolvedItems, successRate
- lastSyncedAt
```

#### **3. messages**
```kotlin
- id (Primary Key)
- chatId, senderId, receiverId
- type (TEXT/IMAGE)
- content, mediaUrl
- isRead, senderName, senderProfilePic
- createdAt
- localId (for offline messages)
- syncStatus (PENDING/SYNCING/SYNCED/FAILED)
- isSentByMe
- lastSyncedAt
```

#### **4. sync_queue**
```kotlin
- id (Auto-increment Primary Key)
- operationType (SEND_MESSAGE, CREATE_ITEM, etc.)
- entityType (MESSAGE, ITEM, etc.)
- payload (JSON)
- status (PENDING/IN_PROGRESS/COMPLETED/FAILED)
- retryCount, maxRetries
- createdAt, lastAttemptAt
- errorMessage
```

---

## 🔌 API Integration

### **Base URL**
```
https://your-backend.onrender.com/api
```

### **Key Endpoints**
- `POST /auth/signup` - User registration
- `POST /auth/login` - User login
- `POST /auth/logout` - Logout & clear FCM token
- `PUT /auth/fcm-token` - Update FCM token
- `GET /users/profile` - Get user profile
- `PUT /users/profile` - Update profile
- `GET /items` - Get all items (excludes own items)
- `POST /items` - Create item
- `GET /items/my-items` - Get user's items
- `POST /items/save` - Save/bookmark item
- `GET /items/saved` - Get saved items
- `GET /chats` - Get user's chats
- `POST /chats/from-item/:itemId` - Create chat from item
- `GET /messages/chat/:chatId` - Get chat messages
- `POST /messages` - Send message
- `DELETE /messages/:id` - Delete message

---

## 🔐 Security

- **JWT Authentication:** All API requests require bearer token
- **Token Storage:** Encrypted DataStore for secure local storage
- **Password Hashing:** bcrypt on backend (never sent plain)
- **FCM Token Cleanup:** Cleared on logout to prevent unauthorized notifications
- **Input Validation:** Both client-side and server-side

---

## 📦 Dependencies

```kotlin
// Core Android
implementation("androidx.core:core-ktx:...")
implementation("androidx.appcompat:appcompat:...")
implementation("com.google.android.material:material:...")

// Lifecycle & ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

// Retrofit (Networking)
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// WorkManager (Background Jobs)
implementation("androidx.work:work-runtime-ktx:2.9.0")

// Glide (Image Loading)
implementation("com.github.bumptech.glide:glide:4.16.0")

// Firebase (Push Notifications)
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-messaging-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")

// DataStore (Secure Storage)
implementation("androidx.datastore:datastore-preferences:1.0.0")

// UI Components
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("de.hdodenhof:circleimageview:3.1.0")
implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
```

---

## 🚀 Setup Instructions

### **1. Clone Repository**
```bash
git clone <repository-url>
cd LocateMe
```

### **2. Firebase Setup**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create/select project
3. Add Android app with package: `com.mustafafaraz.locateme`
4. Download `google-services.json`
5. Place in `app/google-services.json`

### **3. Backend Setup**
1. Backend is already deployed on Render
2. Database hosted on Railway
3. Environment variables configured on Render:
   - `FIREBASE_PROJECT_ID`
   - `FIREBASE_CLIENT_EMAIL`
   - `FIREBASE_PRIVATE_KEY`

### **4. Build Project**
```bash
# Sync Gradle
gradlew.bat --refresh-dependencies

# Clean build
gradlew.bat clean

# Build APK
gradlew.bat assembleDebug
```

---

## 🧪 Testing Checklist

### **Offline Messaging**
- [ ] Send message offline → appears instantly
- [ ] Turn on internet → message syncs automatically
- [ ] Close app + send offline → reopen → message synced
- [ ] Multiple offline messages → all sync in order

### **Push Notifications**
- [ ] Receive notification when message arrives
- [ ] Notification shows sender's profile pic + name
- [ ] Click notification → opens correct chat
- [ ] No notification when already in that chat
- [ ] Logout → no notifications received

### **Offline Data**
- [ ] Open app offline → items load from cache
- [ ] Browse items offline
- [ ] Search works offline (from cache)
- [ ] Profile loads from cache

---

## 📝 Important Notes

### **Known Limitations**
1. **3-Second Polling:** Chat uses polling instead of WebSocket (simple but effective)
2. **Image Upload Size:** Max 5 images per item, base64 limited to 50MB
3. **Message Deletion:** Only within 5 minutes of sending
4. **Cache Expiry:** Old items deleted after 7 days

### **Future Enhancements**
- [ ] WebSocket for real-time messages (replace polling)
- [ ] Push notifications for item matches
- [ ] Notification history screen
- [ ] Advanced search filters
- [ ] Item expiration reminders
- [ ] Analytics dashboard

---

## 🐛 Troubleshooting

### **Build Errors**
```bash
# Clean and rebuild
gradlew.bat clean build --refresh-dependencies
```

### **FCM Not Working**
1. Check `google-services.json` is in `app/` folder
2. Verify package name matches Firebase console
3. Check backend FCM credentials in Render environment variables

### **Offline Sync Not Working**
1. Check `ACCESS_NETWORK_STATE` permission granted
2. Verify WorkManager constraints (requires CONNECTED network)
3. Check Logcat for SyncWorker logs

### **Room Database Errors**
```bash
# If schema changes, increment version in AppDatabase
# Or use fallbackToDestructiveMigration() (current)
```

---

## 📚 Additional Documentation

- `FCM_SETUP_INSTRUCTIONS.md` - FCM implementation guide
- `FCM_QUICK_REFERENCE.md` - Quick FCM reference
- `OFFLINE_STORAGE_IMPLEMENTATION.md` - Offline storage details
- `OFFLINE_SYNC_GUIDE.md` - Background sync architecture
- `backend/api_docs.md` - Complete API documentation
- `backend/schema.sql` - Database schema

---

## 👨‍💻 Development

### **Code Style**
- Kotlin conventions
- MVVM architecture
- Repository pattern for data layer
- Coroutines for async operations
- Flow for reactive data streams

### **Git Workflow**
```bash
git checkout -b feature/new-feature
# Make changes
git add .
git commit -m "Add new feature"
git push origin feature/new-feature
# Create pull request
```

---

## 📄 License
[Your License Here]

## 👥 Contributors
- Mustafa Faraz

---

**Last Updated:** December 7, 2025
**Version:** 1.0.0
**Status:** Production Ready ✅
