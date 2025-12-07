# LocateMe - Lost and Found App

## Project Overview
LocateMe is a comprehensive lost and found application designed for university students to report and search for lost items on campus. The app features real-time chat messaging, item bookmarking, image uploads, user profiles, and comprehensive item management capabilities.

## Tech Stack

### Frontend (Android - Kotlin)
- **Language**: Kotlin
- **Architecture**: MVVM pattern with lifecycle-aware components
- **Networking**: Retrofit2 for API calls
- **Image Loading**: Glide for efficient image loading and caching
- **Async Operations**: Kotlin Coroutines with lifecycleScope
- **Data Persistence**: DataStore for token management
- **UI Components**: Material Design, RecyclerView, CardView, CircleImageView
- **Real-time Updates**: Handler-based polling (3-second intervals for chat)

### Backend (Node.js)
- **Framework**: Express.js 5.2.1
- **Database**: MySQL with connection pooling (mysql2)
- **Authentication**: JWT (JSON Web Tokens) with bcryptjs for password hashing
- **Image Processing**: Base64 encoding/decoding with Multer middleware
- **Push Notifications**: Firebase Admin SDK (FCM)
- **Middleware**: Custom authentication, image processing, and upload middleware

## Core Features

### 1. Authentication System
- **Signup**: New user registration with email validation
- **Login**: Secure login with JWT token generation
- **Session Management**: Token-based authentication with automatic session handling
- **Auto-refresh**: Profile and items refresh on activity resume
- **Token Storage**: Secure DataStore for user ID and auth token

### 2. User Profile Management
- **View Profile**: Display user information with statistics
  - Profile picture with Glide image loading
  - Full name, email, student ID, batch, department, section
  - Items posted count
  - Items reunited count (resolved items)
  - Success rate percentage (resolved/total * 100)
  
- **Edit Profile**: Update user information
  - Change profile picture with smart upload (only uploads if changed)
  - Edit name, student ID, batch, department, section
  - Email is read-only
  - Auto-refresh on return to profile page

- **Quick Actions**:
  - My Items - View all posted items
  - Saved Items - View bookmarked items
  - Messages - Access chat list
  - Settings - Account management

- **Settings**:
  - **Change Password**: 
    - Validates current password
    - Requires new password confirmation
    - Minimum 6 character validation
    - Clears fields after successful change
  - **Delete Account**:
    - Shows confirmation dialog with warning
    - Permanently deletes user and all associated data
    - Clears session and redirects to login page

### 3. Item Management

#### Report Item (Lost/Found)
- **Lost/Found Toggle**: Circular selector to switch between item types
- **Item Details**:
  - Title, description, category (Electronics, Bags, Keys, Clothing, Other)
  - Location with dynamic label based on type
  - Multiple image uploads (up to 5 photos)
  - Photo preview with add/remove functionality
- **Image Handling**: Base64 encoding for secure uploads
- **Validation**: Required field validation before submission

#### Browse Items (Home)
- **Search & Filter**:
  - Search by keywords with debounce (500ms)
  - Filter by type (Lost/Found)
  - Filter by category
- **Item Display**:
  - Grid layout with item cards
  - Image, title, description, location
  - Time posted (formatted as "X hours/days ago")
  - Posted by user's name
  - Type badge (Lost/Found)
  - Save status indicator (is_saved field from backend)
- **Exclusions**: Current user's items excluded from home feed
- **Auto-refresh**: Reloads on resume to update save status

#### My Items
- **Tab Navigation**: Circular selector with three tabs
  - Active items
  - Resolved items
  - Expired items
- **Auto-refresh**: Reloads on resume after editing/deleting
- **Item Actions**: Edit and view details for each item
- **No Save Option**: Users cannot save their own items

#### Saved Items
- **Dynamic Loading**: Fetches saved items from backend API
- **Real API Integration**: Uses `/api/items/saved` endpoint
- **Display**: Shows all bookmarked items using ItemAdapter
- **Auto-refresh**: Reloads on resume after unsaving items
- **Empty State**: Shows "No saved items yet" message
- **Progress Indicator**: Loading spinner while fetching

#### Item Details
- **Full Information Display**:
  - All item images in gallery (horizontal scroll)
  - Title, description, location, time posted
  - Reporter details (name, student ID, department, batch, section)
  - Type badge (Lost/Found)
  
- **Action Buttons**:
  - **Message**: Opens/creates chat with item owner
  - **Email**: Send email to reporter
  - **Bookmark**: Save/unsave item toggle
    - Hollow icon (ic_bookmark_outline) when not saved
    - Filled icon (ic_bookmark) when saved
    - Updates instantly on click
    - Backend prevents saving own items
  - **Share**: Share item details
  - **Favorite**: Reserved for future feature

- **Save/Unsave Functionality**:
  - Shows current save status from backend
  - Toggle with single click
  - Toast confirmation ("Item saved" / "Item unsaved")
  - Error handling for backend validation

#### Edit Item
- **Load Existing Data**: 
  - Pre-fills all fields from API
  - Displays existing images from server
  - Shows current status
- **Image Management**:
  - Displays all existing item images
  - Add new photos (up to 5 total)
  - Remove any photo (existing or new)
  - Smart upload: Only sends new images as base64, keeps existing URLs as paths
- **Update Fields**:
  - Title, description, category, location, type, status
  - Validation for all required fields
- **Delete Item**: 
  - Confirmation dialog
  - Soft delete (sets deleted_at timestamp)
  - Returns to My Items with auto-refresh

### 4. Chat & Messaging System

#### Chat List
- **Dynamic Loading**: Fetches user's chats from `/api/chats` endpoint
- **Display Features**:
  - User profile pictures (circle crop with Glide)
  - Other user's name
  - Last message preview ("📷 Image" for image messages)
  - Timestamp (formatted with TimeFormatter.formatTimeAgo)
  - Empty state: "No messages yet" with illustration
- **Auto-refresh**: Reloads on resume when returning from ChatScreen
- **Click Handler**: Opens ChatScreen for selected conversation

#### Chat Screen
- **Header**: Shows other user's full name with back button
- **Message Display**:
  - Sent messages: Blue background, right-aligned, white text
  - Received messages: Gray background, left-aligned, black text
  - Text messages: Content displayed
  - Image messages: 200x200dp image container with Glide loading
  - Timestamps: Formatted as "2:30 PM" below each message

- **Chat Creation**:
  - From ItemDetails: Click "Message" button
  - Backend creates chat or returns existing chat (no duplicates)
  - Prevents chatting with yourself (backend validation)
  - Auto-loads messages on open

- **Send Text Messages**:
  - Type in input field
  - Click send button
  - Instantly appears in chat
  - Input field clears automatically
  - Sent via `/api/messages` endpoint

- **Send Image Messages**:
  - Click image attachment button (camera icon)
  - Opens gallery picker
  - Image resized to 1024x1024 max
  - Compressed to JPEG 80% quality
  - Converted to base64 (no data URI prefix)
  - Sent to backend
  - Appears immediately in chat

- **Real-time Auto-refresh**:
  - Polls every 3 seconds for new messages
  - Only updates if new messages exist
  - Smart scrolling: Maintains position or scrolls to bottom
  - Both users see messages within 3 seconds
  - Marks messages as read automatically

- **Message Deletion**:
  - Long-press on your own sent messages
  - Only available within 5 minutes of sending
  - Shows confirmation dialog
  - Backend validates time limit
  - Removes from both users' chats instantly
  - Cannot long-press received messages

- **Lifecycle Management**:
  - Handler-based auto-refresh
  - Stops on activity destroy
  - Prevents memory leaks

### 5. Image Handling System

#### Frontend (EditPhotoAdapter)
- Handles both Uri (new images) and String URLs (existing images)
- Displays existing images from server using Glide
- Allows adding/removing images dynamically
- Photo count display (e.g., "3/5 Photos")

#### Backend (processItemImages & processMessageImage middleware)
- **Smart Detection**:
  - Checks if image data starts with "uploads/" (existing path)
  - If existing: Keeps path unchanged
  - If new: Decodes base64 and saves with unique filename
- **File Storage**: 
  - Items: `uploads/items/`
  - Messages: `uploads/messages/`
  - Profiles: `uploads/profiles/`
- **URL Generation**: Returns full server URL for images
- **Size Limits**: 50MB payload limit for base64 images

### 6. Time Formatting (TimeFormatter utility)
- **formatTimeAgo()**: Converts ISO 8601 to relative time
  - Examples: "Just now", "5 minutes ago", "2 hours ago", "3 days ago"
  - Used in Item cards, ChatList
  
- **formatTime()**: Converts to readable time
  - Example: "2:30 PM"
  - Used in ChatScreen message timestamps
  - Converts UTC to local timezone

## API Endpoints

### Authentication
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User login with JWT token

### User Profile
- `GET /api/users/profile` - Get current user profile with statistics
- `PUT /api/users/profile` - Update profile (with optional profile picture)
- `PUT /api/users/change-password` - Change user password
- `DELETE /api/users/account` - Delete user account (soft delete)

### Items
- `POST /api/items` - Create new item (lost/found)
- `GET /api/items` - Get all items (excludes current user's items, includes is_saved flag)
- `GET /api/items/:id` - Get specific item details (includes is_saved flag)
- `GET /api/items/my-items` - Get current user's items with status filter
- `PUT /api/items/:id` - Update item with smart image handling
- `DELETE /api/items/:id` - Delete item (soft delete)
- `POST /api/items/save` - Save/bookmark an item (prevents saving own items)
- `DELETE /api/items/save/:itemId` - Unsave/unbookmark an item
- `GET /api/items/saved` - Get all saved items (includes is_saved: true)

### Chats
- `GET /api/chats` - Get all user's chats (sorted by last message)
- `POST /api/chats` - Create or get existing chat (prevents duplicates)
- `POST /api/chats/from-item/:itemId` - Create chat from item (convenience endpoint)
- `GET /api/chats/:id` - Get chat by ID with other user's info
- `DELETE /api/chats/:id` - Delete chat

### Messages
- `GET /api/messages/chat/:chatId` - Get all messages for a chat (chronological order)
- `POST /api/messages` - Send text or image message
- `DELETE /api/messages/:id` - Delete message (within 5 minutes only)
- `PUT /api/messages/chat/:chatId/read` - Mark messages as read
- `GET /api/messages/unread-count` - Get total unread message count

### Notifications (Backend Ready)
- `GET /api/notifications` - Get user's notifications
- `PUT /api/notifications/:id/read` - Mark notification as read
- `DELETE /api/notifications/:id` - Delete notification

## Data Models

### User
```kotlin
{
  id: Int
  fullName: String
  email: String
  studentId: String
  batch: String
  department: String
  section: String
  profilePic: String? (URL with :5000 port stripped)
  createdAt: String
  updatedAt: String
  stats: {
    totalItems: Int
    resolvedItems: Int
    successRate: Float
  }
}
```

### Item
```kotlin
{
  id: Int
  title: String
  description: String
  imageUrls: List<String> (URLs with :5000 port stripped)
  category: String (ELECTRONICS/BAGS/KEYS/CLOTHING/OTHER)
  location: String
  type: String (LOST/FOUND)
  status: String (ACTIVE/RESOLVED/EXPIRED)
  userId: Int
  userName: String
  userEmail: String
  userStudentId: String?
  userBatch: String?
  userDepartment: String?
  userSection: String?
  userProfilePic: String?
  createdAt: String
  updatedAt: String
  isSaved: Boolean (indicates if current user saved this item)
}
```

### Chat
```kotlin
{
  id: Int
  otherUserId: Int
  otherUserName: String
  otherUserEmail: String
  userProfilePic: String? (URL with :5000 port stripped)
  lastMessage: String?
  lastMessageType: String? (TEXT/IMAGE)
  lastMessageTime: String?
  createdAt: String
  lastMessageAt: String?
}
```

### ChatMessage
```kotlin
{
  id: Int
  chatId: Int
  senderId: Int
  receiverId: Int
  type: String (TEXT/IMAGE)
  content: String? (for text messages)
  mediaUrl: String? (for image messages, URL with :5000 port stripped)
  isRead: Boolean (converted from MySQL TINYINT 0/1)
  createdAt: String
  senderName: String?
  senderProfilePic: String?
}
```

## Database Schema

### users table
- Stores user authentication and profile information
- Fields: id, full_name, email, password (hashed), student_id, batch, department, section, profile_pic, fcm_token
- Timestamps: created_at, updated_at, deleted_at (soft delete)

### items table
- Stores lost/found item reports
- Fields: id, user_id (FK), title, description, image_urls (JSON), category, location, date_reported, expires_at, type, status
- Timestamps: created_at, updated_at, deleted_at (soft delete)
- Indexes on user_id, type, status, category, created_at

### saved_items table
- Many-to-many relationship between users and items
- Fields: id, user_id (FK), item_id (FK)
- Unique constraint on (user_id, item_id) to prevent duplicates
- Cascade delete when user or item is deleted

### chats table
- Stores conversation threads between two users
- Fields: id, user1_id (FK), user2_id (FK), last_message_at
- Unique constraint on (user1_id, user2_id)
- Timestamps: created_at, last_message_at

### messages table
- Stores individual messages in chats
- Fields: id, chat_id (FK), sender_id (FK), receiver_id (FK), type, content, media_url, is_read (TINYINT 0/1)
- Timestamp: created_at
- Indexes on chat_id, sender_id, receiver_id, created_at

### notifications table
- Stores push notification history
- Fields: id, user_id (FK), title, body, image_url, data (JSON), is_read (BOOLEAN)
- Timestamp: created_at
- Indexes on user_id, is_read, created_at

## Key Features Implementation Details

### Smart Image Handling (Edit Item)
When editing an item:
1. **Frontend**: Separates existing URLs from new Uri selections
2. **Existing Images**: Extracts path (e.g., "uploads/items/itemImage-123.jpg")
3. **New Images**: Converts Uri to base64 using ImageHelper.uriToBase64()
4. **Backend**: Detects paths vs base64, only processes new images
5. **Result**: Efficient updates without re-uploading unchanged images

### Profile Picture Updates (Edit Profile)
- Tracks `hasImageChanged` flag
- Only converts to base64 if user selects new image
- Sends `null` for profilePic if unchanged
- Backend only updates database if new image provided

### Save/Unsave Items Flow
1. User views item in Home or ItemDetails
2. Backend returns `is_saved` flag for each item
3. ItemDetails shows filled/hollow bookmark icon based on status
4. Click bookmark → API call to save/unsave
5. Backend validates (prevents saving own items)
6. Icon updates immediately
7. Home and SavedItems auto-refresh on resume

### Chat System Flow
1. **Create Chat**: User clicks "Message" on ItemDetails
2. **Backend Check**: `/api/chats/from-item/:itemId` checks for existing chat
3. **Return Chat**: Returns existing chat or creates new one
4. **Load Messages**: ChatScreen fetches messages chronologically
5. **Send Message**: Text/image sent via `/api/messages`
6. **Auto-refresh**: Polls every 3 seconds for new messages
7. **Both Users**: See messages within 3 seconds
8. **Delete**: Long-press within 5 minutes to delete

### Auto-Refresh Pattern
- `onResume()` lifecycle method in:
  - Profile: Reloads user data after edits
  - Home: Updates item save status
  - MyItems: Refreshes after editing/deleting
  - SavedItems: Updates after unsaving items
  - ChatList: Refreshes after chatting
  - ChatScreen: Built-in 3-second polling

### Circular Tab Selector
- Background: `login_signup_background` (rounded gray container)
- Selected: `selected_button_login_signup` (white rounded background)
- Used in: Report page (Lost/Found), My Items page (Active/Resolved/Expired)

## Security Features
- JWT-based authentication with Bearer token scheme
- Password hashing with bcryptjs (10 rounds)
- Token validation middleware on all protected routes
- Soft deletes (preserves data integrity)
- Input validation on both frontend and backend
- Prevents users from saving own items
- Prevents creating chat with yourself
- Time-based message deletion (5-minute window)
- FCM token storage for push notifications

## User Experience Features
- Loading states on all async operations
- Error handling with user-friendly messages
- Confirmation dialogs for destructive actions
- Field validation with inline error messages
- Read-only email field (cannot be changed)
- Auto-clearing of password fields after successful change
- Progress indicators during API calls
- Empty states for no data scenarios
- Image compression for faster uploads
- Debounced search (500ms delay)
- Real-time message updates (3-second polling)
- Smart scrolling in chat (maintains position)

## Backend Highlights
- **Express.js 5.2.1** with CORS support
- **50MB payload limit** for base64 images
- **MySQL connection pooling** for performance
- **Organized upload directories** by type
- **Firebase Admin SDK** for FCM notifications
- **Comprehensive error logging** with stack traces
- **Health check endpoint** at `/health`
- **API documentation** at root endpoint `/`

## Icons & Assets
- **ic_bookmark.xml** - Filled bookmark (saved state)
- **ic_bookmark_outline.xml** - Hollow bookmark (unsaved state)
- All icons use Material Design style
- Consistent 24dp sizing
- Black fill color (can be tinted)

## Recent Updates (December 2025)

### Save/Unsave Functionality
- Implemented bookmark icon toggle in ItemDetails
- Added SavedItems screen with real API integration
- Backend prevents saving own items with validation
- Auto-refresh on Home and SavedItems pages

### Complete Chat System
- Chat creation from ItemDetails (message button)
- Real-time messaging with 3-second auto-refresh
- Text and image message support
- Message deletion within 5-minute window
- ChatList with last message preview
- Profile pictures in chat list
- Duplicate chat prevention
- Self-chat prevention

### Bug Fixes
- Fixed ChatMessage isRead field (MySQL TINYINT to Boolean conversion)
- Fixed getUserId() type mismatch (String to Int conversion)
- Updated ChatList to use real API data
- Added progress bars and empty states

## Future Enhancements
- ✅ ~~Real-time chat between users~~ (COMPLETED)
- ✅ ~~Item bookmarking/saving~~ (COMPLETED)
- 🔜 Push notifications for new messages (FCM ready)
- 🔜 Push notifications for item matches
- 🔜 Item claiming/verification system
- 🔜 WebSocket for instant message delivery (replace polling)
- 🔜 Unread message count badges
- 🔜 User reputation system
- 🔜 Advanced search with filters
- 🔜 Item expiration handling

---

**Last Updated**: December 7, 2025  
**Version**: 2.0.0  
**Status**: Chat & Messaging System Complete, Push Notifications Pending
