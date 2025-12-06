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
Get current user profile

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "full_name": "John Doe",
    "email": "john@student.fast.edu.pk",
    "student_id": "23I-0631",
    "profile_pic": "http://server.com/uploads/profiles/pic.jpg",
    ...
  }
}
```

### PUT `/users/profile` 🔒
Update user profile (with optional profile picture)

**Body:** (multipart/form-data)
```
fullName: "John Updated"
batch: "2024"
profilePic: <file>
```

### PUT `/users/change-password` 🔒
Change password

**Body:**
```json
{
  "currentPassword": "old123",
  "newPassword": "new456"
}
```

### GET `/users/:id` 🔒
Get user by ID (to view other users' profiles)

---

## 📦 Item Routes

### GET `/items` 🔒
Get all items (home feed)

**Query Parameters:**
- `type` - Filter by LOST or FOUND
- `category` - Filter by ELECTRONICS, BAGS, KEYS, CLOTHING, OTHER
- `status` - Filter by ACTIVE, RESOLVED, EXPIRED
- `search` - Search in title, description, location
- `limit` - Default: 50
- `offset` - Default: 0

**Example:** `/items?type=LOST&category=ELECTRONICS&search=phone`

### POST `/items` 🔒
Create new item

**Body:** (multipart/form-data)
```
title: "Lost iPhone 13"
description: "Black iPhone 13, lost near library"
category: "ELECTRONICS"
location: "Library Building"
type: "LOST"
expiresAt: "2025-12-31T23:59:59Z" (optional)
itemImages: <file1>, <file2>, ... (max 5)
```

### GET `/items/:id` 🔒
Get item by ID

### GET `/items/my-items` 🔒
Get current user's items

**Query Parameters:**
- `type` - Filter by LOST or FOUND
- `status` - Filter by ACTIVE, RESOLVED, EXPIRED

### PUT `/items/:id` 🔒
Update item

**Body:** (multipart/form-data) - Same as create, all fields optional

### DELETE `/items/:id` 🔒
Delete item (soft delete)

### POST `/items/save` 🔒
Save an item

**Body:**
```json
{
  "itemId": 123
}
```

### DELETE `/items/save/:itemId` 🔒
Unsave an item

### GET `/items/saved` 🔒
Get all saved items

---

## 💬 Chat Routes

### GET `/chats` 🔒
Get all user's chats

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "other_user_id": 2,
      "other_user_name": "Jane Doe",
      "user_profile_pic": "http://...",
      "last_message": "Hey, is this still available?",
      "last_message_type": "TEXT",
      "last_message_time": "2025-09-20T10:30:00Z",
      ...
    }
  ]
}
```

### POST `/chats` 🔒
Create or get existing chat

**Body:**
```json
{
  "otherUserId": 2
}
```

### GET `/chats/:id` 🔒
Get chat by ID

### DELETE `/chats/:id` 🔒
Delete chat

---

## 📨 Message Routes

### GET `/messages/chat/:chatId` 🔒
Get messages for a chat

**Query Parameters:**
- `limit` - Default: 50
- `offset` - Default: 0

### POST `/messages` 🔒
Send message

**For TEXT message:**
```json
{
  "chatId": 1,
  "type": "TEXT",
  "content": "Hello, is this still available?"
}
```

**For IMAGE message:** (multipart/form-data)
```
chatId: 1
type: "IMAGE"
messageImage: <file>
```

### PUT `/messages/chat/:chatId/read` 🔒
Mark all messages in chat as read

### DELETE `/messages/:id` 🔒
Delete message (sender only)

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
- Image uploads are limited to 5MB per file
- Items can have maximum 5 images
- Profile pictures are stored in `/uploads/profiles/`
- Item images are stored in `/uploads/items/`
- Message images are stored in `/uploads/messages/`
- All image URLs are returned as full URLs with server domain

---

## 🔑 Authentication Token

After login/signup, you'll receive a JWT token. Store this token securely in your Android app (SharedPreferences) and include it in all authenticated requests:

```kotlin
// In Retrofit header
@Headers("Authorization: Bearer \${token}")
```