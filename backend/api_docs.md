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

- This is a soft delete (sets `deleted_at` timestamp)
- All user's items are also soft deleted
- User cannot login after account deletion
- After successful deletion, Android app should clear session/token and redirect to login

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

**Response:** Array of items (excluding current user's items)

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
    "created_at": "2025-12-07T10:00:00.000Z"
  }
}
```

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
```

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
```

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

**Note:** `messageImage` should be a base64 encoded image string

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
