# LocateMe API Documentation

Base URL: `https://your-app.onrender.com/api`

## Authentication

All protected routes require JWT token in header:

```
Authorization: Bearer <your_jwt_token>
```

---

## 🔐 Authentication Routes

### POST `/auth/signup`

Register a new user

**Body:**

```json
{
  "fullName": "John Doe",
  "email": "john@student.fast.edu.pk",
  "password": "password123",
  "studentId": "23I-0631",
  "batch": "2023",
  "department": "Computer Science",
  "section": "A"
}
```

**Response:**

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "full_name": "John Doe",
      "email": "john@student.fast.edu.pk",
      ...
    }
  }
}
```

### POST `/auth/login`

Login user

**Body:**

```json
{
  "email": "john@student.fast.edu.pk",
  "password": "password123"
}
```

**Response:** Same as signup

### PUT `/auth/fcm-token` 🔒

Update FCM token for push notifications

**Headers:** `Authorization: Bearer <token>`

**Body:**

```json
{
  "fcmToken": "your-fcm-device-token"
}
```

---

## 👤 User Routes

### GET `/users/profile` 🔒

Get current user profile with statistics

**Response:**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "full_name": "John Doe",
    "email": "john@student.fast.edu.pk",
    "student_id": "23I-0631",
    "batch": "2023",
    "department": "Computer Science",
    "section": "A",
    "profile_pic": "http://server.com/uploads/profiles/pic.jpg",
    "stats": {
      "total_items": 15,
      "resolved_items": 12,
      "success_rate": 80.0
    },
    "created_at": "2025-01-01T00:00:00.000Z",
    "updated_at": "2025-12-07T10:00:00.000Z"
  }
}
```

**Statistics explained:**

- `total_items`: Total number of items posted by the user
- `resolved_items`: Number of items marked as RESOLVED
- `success_rate`: Percentage of resolved items (resolved/total \* 100)

### PUT `/users/profile` 🔒

Update user profile (with optional profile picture)

**Body:** (application/json)

```json
{
  "fullName": "John Updated",
  "studentId": "2023-CS-001",
  "batch": "2024",
  "department": "Computer Science",
  "section": "A",
  "profilePic": "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
}
```

**Important Notes:**

- All fields are optional - only include fields you want to update
- `profilePic` can be:
  - **New image**: Base64 encoded string (with or without data URI prefix) - will upload new image
  - **Existing image**: Path starting with `uploads/` (e.g., `"uploads/profiles/pic.jpg"`) - keeps existing image unchanged
  - **Omit**: Don't include `profilePic` in request - keeps current profile picture
- Only the authenticated user can update their own profile

**Response:**

```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": 1,
    "full_name": "John Updated",
    "profile_pic": "https://server.com/uploads/profiles/pic.jpg",
    "stats": { ... }
  }
}
```

### PUT `/users/change-password` 🔒

Change password (requires current password for security)

**Body:**

```json
{
  "currentPassword": "old123",
  "newPassword": "new456"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

**Note:** The Android app should validate that "new password" and "confirm new password" match on the frontend before sending the request.

### DELETE `/users/account` 🔒

Delete account (soft delete - requires password confirmation)

**Body:**

```json
{
  "password": "userPassword123"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Account deleted successfully"
}
```

**Important:**

- **Permanently deletes ALL user-related data:**
  - **User account** - Permanently deleted (cannot be recovered)
  - **All items posted by the user** - Soft deleted (set deleted_at, can be recovered by admin)
  - **All saved items by the user** - Permanently deleted
  - **All chats where user is a participant** - Permanently deleted
  - **All messages sent or received by the user** - Permanently deleted
  - **All notifications for the user** - Permanently deleted
- User cannot login after account deletion
- After successful deletion, Android app should clear session/token and redirect to login
- **Warning:** This action is irreversible for the user account itself

### GET `/users/:id` 🔒

Get user by ID (to view other users' profiles)

---

## 📦 Item Routes

### GET `/items` 🔒

Get all items (home feed - excludes current user's items)

**Behavior:** Returns items posted by OTHER users only. Current user's items are excluded from the home feed.

**Query Parameters:**

- `type` - Filter by LOST or FOUND
- `category` - Filter by ELECTRONICS, BAGS, KEYS, CLOTHING, OTHER
- `status` - Filter by ACTIVE, RESOLVED, EXPIRED
- `search` - Search in title, description, location
- `limit` - Default: 50
- `offset` - Default: 0

**Example:** `/items?type=LOST&category=ELECTRONICS&search=phone`

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "title": "Lost Backpack",
      "description": "Blue backpack with laptop inside",
      "category": "BAGS",
      "location": "Library 2nd Floor",
      "type": "LOST",
      "status": "ACTIVE",
      "image_urls": ["https://server.com/uploads/items/backpack.jpg"],
      "user_id": 7,
      "user_name": "Alice Johnson",
      "user_email": "alice@example.com",
      "user_profile_pic": "https://server.com/uploads/profiles/alice.jpg",
      "date_reported": "2025-12-09T14:30:00.000Z",
      "expires_at": null,
      "is_saved": false,
      "created_at": "2025-12-09T14:30:00.000Z",
      "updated_at": "2025-12-09T14:30:00.000Z"
    }
  ],
  "total": 42,
  "limit": 50,
  "offset": 0
}
```

**Note:**

- Current user's own items are excluded from this feed
- `is_saved` indicates whether the current user has saved this item

### POST `/items` 🔒

Create new item

**Body:** (application/json)

```json
{
  "title": "Lost iPhone 13",
  "description": "Black iPhone 13, lost near library",
  "category": "ELECTRONICS",
  "location": "Library Building",
  "type": "LOST",
  "expiresAt": "2025-12-31T23:59:59Z",
  "itemImages": [
    "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "data:image/png;base64,iVBORw0KGgoAAAANS..."
  ]
}
```

**Note:** `itemImages` is optional, max 5 images, each should be a base64 encoded image string

### GET `/items/:id` 🔒

Get item by ID with detailed user information

**Response:**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Lost iPhone 13",
    "description": "Black iPhone 13",
    "category": "ELECTRONICS",
    "type": "LOST",
    "status": "ACTIVE",
    "location": "Library",
    "image_urls": ["https://server.com/uploads/items/image1.jpg"],
    "user_name": "John Doe",
    "user_email": "john@example.com",
    "user_student_id": "2021-CS-001",
    "user_batch": "2021",
    "user_department": "Computer Science",
    "user_section": "A",
    "user_profile_pic": "https://server.com/uploads/profiles/pic.jpg",
    "is_saved": true,
    "created_at": "2025-12-07T10:00:00.000Z"
  }
}
```

**Note:** `is_saved` indicates whether the current user has saved this item.

````

### GET `/items/my-items` 🔒

Get current user's items

**Query Parameters:**

- `type` - Filter by LOST or FOUND
- `status` - Filter by ACTIVE, RESOLVED, EXPIRED

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Lost iPhone 13",
      "description": "Black iPhone 13, lost near library",
      "category": "ELECTRONICS",
      "location": "Library Building",
      "type": "LOST",
      "status": "ACTIVE",
      "image_urls": [
        "https://server.com/uploads/items/image1.jpg",
        "https://server.com/uploads/items/image2.jpg"
      ],
      "user_id": 1,
      "user_name": "John Doe",
      "user_email": "john@example.com",
      "user_profile_pic": "https://server.com/uploads/profiles/pic.jpg",
      "date_reported": "2025-12-07T08:00:00.000Z",
      "expires_at": null,
      "created_at": "2025-12-07T08:00:00.000Z",
      "updated_at": "2025-12-07T08:00:00.000Z"
    }
  ]
}
````

````

### PUT `/items/:id` 🔒

Update item (only owner can update)

**Body:** (application/json) - All fields optional

```json
{
  "title": "Updated Title",
  "description": "Updated description",
  "category": "ELECTRONICS",
  "location": "Updated location",
  "type": "LOST",
  "status": "RESOLVED",
  "expiresAt": "2025-12-31T23:59:59Z",
  "itemImages": [
    "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "data:image/png;base64,iVBORw0KGgoAAAANS..."
  ]
}
````

**Important Notes:**

- All fields are optional - only include fields you want to update
- `itemImages` behavior:
  - **Array with new base64 images**: Replaces all existing images with new ones
  - **Array with existing paths** (e.g., `["uploads/items/img1.jpg", "uploads/items/img2.jpg"]`): Keeps those specific images
  - **Mixed array**: Can mix existing paths (starting with `uploads/`) and new base64 images - existing paths are kept, base64 strings are converted
  - **Empty array** `[]`: Removes all images
  - **Omit field**: Don't include `itemImages` in request - keeps all existing images unchanged
- Max 5 images allowed
- Only the item owner can update their item
- Returns updated item with full image URLs

**Response:**

```json
{
  "success": true,
  "message": "Item updated successfully",
  "data": {
    "id": 1,
    "title": "Updated Title",
    "image_urls": ["https://server.com/uploads/items/newimage.jpg"],
    ...
  }
}
```

### DELETE `/items/:id` 🔒

Delete item (soft delete - only owner can delete)

**Response:**

```json
{
  "success": true,
  "message": "Item deleted successfully"
}
```

### POST `/items/save` 🔒

Save an item to your saved items list

**Note:** You cannot save your own items, only items posted by other users.

**Body:**

```json
{
  "itemId": 123
}
```

**Response:**

```json
{
  "success": true,
  "message": "Item saved successfully"
}
```

**Errors:**

- 400: Item already saved OR attempting to save your own item
- 404: Item not found

### DELETE `/items/save/:itemId` 🔒

Remove an item from your saved items list

**Response:**

```json
{
  "success": true,
  "message": "Item unsaved successfully"
}
```

### GET `/items/saved` 🔒

Get all items you have saved

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "title": "Found Wallet",
      "description": "Brown leather wallet found in cafeteria",
      "category": "WALLETS",
      "location": "Main Cafeteria",
      "type": "FOUND",
      "status": "ACTIVE",
      "image_urls": ["https://server.com/uploads/items/wallet.jpg"],
      "user_id": 3,
      "user_name": "Jane Smith",
      "user_email": "jane@example.com",
      "user_profile_pic": "https://server.com/uploads/profiles/jane.jpg",
      "date_reported": "2025-12-08T10:00:00.000Z",
      "expires_at": null,
      "is_saved": true,
      "created_at": "2025-12-08T10:00:00.000Z",
      "updated_at": "2025-12-08T10:00:00.000Z"
    }
  ]
}
```

**Note:** All items in this list will have `is_saved: true` by definition.

---

## 💬 Chat Routes

### GET `/chats` 🔒

Get all user's chats with last message preview

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "user1_id": 1,
      "user2_id": 2,
      "other_user_id": 2,
      "other_user_name": "Jane Doe",
      "other_user_email": "jane@example.com",
      "user_profile_pic": "https://server.com/uploads/profiles/jane.jpg",
      "last_message": "Hey, is this still available?",
      "last_message_type": "TEXT",
      "last_message_time": "2025-12-07T10:30:00.000Z",
      "created_at": "2025-12-07T09:00:00.000Z",
      "last_message_at": "2025-12-07T10:30:00.000Z"
    }
  ]
}
```

**Note:** Chats are sorted by most recent message time

### POST `/chats` 🔒

Create or get existing chat with another user

**Behavior:** If chat already exists between users, returns existing chat. Otherwise creates new chat.

**Body:**

```json
{
  "otherUserId": 2
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "user1_id": 1,
    "user2_id": 2,
    "other_user_id": 2,
    "other_user_name": "Jane Doe",
    "other_user_email": "jane@example.com",
    "user_profile_pic": "https://server.com/uploads/profiles/jane.jpg",
    "created_at": "2025-12-07T09:00:00.000Z"
  }
}
```

**Errors:**

- 400: Cannot create chat with yourself
- 404: Other user not found

### POST `/chats/from-item/:itemId` 🔒

Create or get chat with item owner (convenience endpoint for messaging from item details)

**Behavior:** Automatically creates/gets chat with the user who posted the item

**Response:** Same as `POST /chats`

**Errors:**

- 400: Cannot chat with yourself (if you're the item owner)
- 404: Item not found

**Usage:** Call this when user clicks "Message" button on an item details page

### GET `/chats/:id` 🔒

Get chat details by ID

**Response:**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "other_user_id": 2,
    "other_user_name": "Jane Doe",
    "other_user_email": "jane@example.com",
    "user_profile_pic": "https://server.com/uploads/profiles/jane.jpg",
    "created_at": "2025-12-07T09:00:00.000Z"
  }
}
```

### DELETE `/chats/:id` 🔒

Delete chat and all associated messages

**Response:**

```json
{
  "success": true,
  "message": "Chat deleted successfully"
}
```

---

## 📨 Message Routes

### GET `/messages/chat/:chatId` 🔒

Get all messages for a specific chat

**Query Parameters:**

- `limit` - Maximum messages to return (default: 50)
- `offset` - Number of messages to skip (default: 0)

**Behavior:**

- Messages are returned in chronological order (oldest first)
- Automatically marks messages as read for the current user
- Includes sender information with each message

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "chat_id": 1,
      "sender_id": 2,
      "receiver_id": 1,
      "type": "TEXT",
      "content": "Hey, is this still available?",
      "media_url": null,
      "is_read": true,
      "sender_name": "Jane Doe",
      "sender_profile_pic": "https://server.com/uploads/profiles/jane.jpg",
      "created_at": "2025-12-07T10:30:00.000Z"
    },
    {
      "id": 2,
      "chat_id": 1,
      "sender_id": 1,
      "receiver_id": 2,
      "type": "IMAGE",
      "content": null,
      "media_url": "https://server.com/uploads/messages/img-123.jpg",
      "is_read": false,
      "sender_name": "John Doe",
      "sender_profile_pic": "https://server.com/uploads/profiles/john.jpg",
      "created_at": "2025-12-07T10:31:00.000Z"
    }
  ],
  "pagination": {
    "limit": 50,
    "offset": 0,
    "count": 2
  }
}
```

**Note:** Use `sender_id` to determine if message should be displayed on left (received) or right (sent) side

### POST `/messages` 🔒

Send a message (text or image)

**For TEXT message:** (application/json)

```json
{
  "chatId": 1,
  "type": "TEXT",
  "content": "Hello, is this still available?"
}
```

**For IMAGE message:** (application/json)

```json
{
  "chatId": 1,
  "type": "IMAGE",
  "messageImage": "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
}
```

**Response:**

```json
{
  "success": true,
  "message": "Message sent successfully",
  "data": {
    "id": 3,
    "chat_id": 1,
    "sender_id": 1,
    "receiver_id": 2,
    "type": "TEXT",
    "content": "Hello, is this still available?",
    "media_url": null,
    "is_read": false,
    "sender_name": "John Doe",
    "sender_profile_pic": "https://server.com/uploads/profiles/john.jpg",
    "created_at": "2025-12-07T10:35:00.000Z"
  }
}
```

**Notes:**

- `messageImage` should be base64 encoded image string
- Automatically sends push notification to receiver
- Updates chat's `last_message_at` timestamp
- Creates notification in database

**Errors:**

- 400: Missing required fields or invalid type
- 404: Chat not found or no access

### PUT `/messages/chat/:chatId/read` 🔒

Mark all messages in a chat as read

**Response:**

```json
{
  "success": true,
  "message": "Messages marked as read"
}
```

**Usage:** Call when user opens a chat screen

### GET `/messages/unread-count` 🔒

Get total unread message count across all chats

**Response:**

```json
{
  "success": true,
  "data": {
    "unreadCount": 5
  }
}
```

**Usage:** Display badge on chat/messages icon

---

## 🔄 Real-time Chat Updates

**Note:** The backend does not currently use WebSocket/Socket.io. For real-time chat experience in Android, implement polling:

**Recommended Polling Strategy:**

1. **Active Chat Screen:**
   - Poll `GET /messages/chat/:chatId` every 2-3 seconds while chat is open
   - Use offset/limit to fetch only new messages (track last message ID)
2. **Chat List Screen:**
   - Poll `GET /chats` every 5-10 seconds to update last messages
3. **Background Updates:**
   - Rely on FCM push notifications when app is in background
   - When notification received, fetch latest messages

**Example Android Implementation:**

```kotlin
// In ChatActivity
private val pollInterval = 3000L // 3 seconds
private val handler = Handler(Looper.getMainLooper())

private val pollRunnable = object : Runnable {
    override fun run() {
        fetchNewMessages()
        handler.postDelayed(this, pollInterval)
    }
}

override fun onResume() {
    super.onResume()
    handler.post(pollRunnable) // Start polling
}

override fun onPause() {
    super.onPause()
    handler.removeCallbacks(pollRunnable) // Stop polling
}
```

### DELETE `/messages/:id` 🔒

Delete a message (sender only, within 5 minutes)

**Restrictions:**

- Only sender can delete their own messages
- Messages can only be deleted within 5 minutes of sending
- Requires confirmation in UI (long press)

**Response:**

```json
{
  "success": true,
  "message": "Message deleted successfully"
}
```

**Errors:**

- 403: Message is older than 5 minutes
- 404: Message not found or not the sender

### GET `/messages/unread-count` 🔒

Get unread message count

**Response:**

```json
{
  "success": true,
  "data": {
    "unreadCount": 5
  }
}
```

---

## 🔔 Notification Routes

### GET `/notifications` 🔒

Get all notifications

**Query Parameters:**

- `limit` - Default: 50
- `offset` - Default: 0

### GET `/notifications/unread-count` 🔒

Get unread notification count

### PUT `/notifications/:id/read` 🔒

Mark notification as read

### PUT `/notifications/read-all` 🔒

Mark all notifications as read

### DELETE `/notifications/:id` 🔒

Delete notification

### DELETE `/notifications` 🔒

Delete all notifications

---

## 📊 Item Status Flow

1. **ACTIVE** - Item is currently lost/found
2. **RESOLVED** - Item has been recovered/returned
3. **EXPIRED** - Item posting has expired (based on expiresAt date)

Users can update status via PUT `/items/:id`

---

## 🚀 Response Format

### Success Response

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

### Error Response

```json
{
  "success": false,
  "message": "Error description",
  "error": "Detailed error message"
}
```

---

## 📝 Notes

- All timestamps are in ISO 8601 format (UTC)
- **Images are sent as base64 encoded strings** (with or without data URI prefix like `data:image/jpeg;base64,`)
- Base64 image size should not exceed 5MB when decoded
- Items can have maximum 5 images
- Supported image formats: JPEG, PNG, GIF
- Profile pictures are stored in `/uploads/profiles/`
- Item images are stored in `/uploads/items/`
- Message images are stored in `/uploads/messages/`
- All image URLs are returned as full URLs with server domain

**Base64 Image Example:**

```json
{
  "profilePic": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD..."
}
```

Or without data URI:

```json
{
  "profilePic": "/9j/4AAQSkZJRgABAQEAYABgAAD..."
}
```

---

## 🔑 Authentication Token

After login/signup, you'll receive a JWT token. Store this token securely in your Android app (SharedPreferences) and include it in all authenticated requests:

```kotlin
// In Retrofit header
@Headers("Authorization: Bearer \${token}")
```
