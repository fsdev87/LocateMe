# LocateMe - Lost and Found App

## Project Overview
LocateMe is a comprehensive lost and found application designed for university students to report and search for lost items on campus. The app features real-time notifications, image uploads, user profiles, and item management capabilities.

## Tech Stack

### Frontend (Android - Kotlin)
- **Language**: Kotlin
- **Architecture**: MVVM pattern with lifecycle-aware components
- **Networking**: Retrofit2 for API calls
- **Image Loading**: Glide for efficient image loading and caching
- **Async Operations**: Kotlin Coroutines with lifecycleScope
- **Data Persistence**: DataStore for token management
- **UI Components**: Material Design, RecyclerView, CardView, CircleImageView

### Backend (Node.js)
- **Framework**: Express.js
- **Database**: MySQL with connection pooling
- **Authentication**: JWT (JSON Web Tokens) with bcrypt for password hashing
- **Image Processing**: Base64 encoding/decoding with file storage
- **Middleware**: Custom authentication and image processing middleware

## Core Features

### 1. Authentication System
- **Signup**: New user registration with email validation
- **Login**: Secure login with JWT token generation
- **Session Management**: Token-based authentication with automatic session handling
- **Auto-refresh**: Profile and items refresh on activity resume

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
  - Search by keywords
  - Filter by type (Lost/Found)
  - Filter by category
- **Item Display**:
  - Grid layout with item cards
  - Image, title, description, location
  - Time posted (formatted as "X hours/days ago")
  - Posted by user's name
  - Type badge (Lost/Found)
- **Time Formatting**: Smart relative time display (TimeFormatter utility)

#### My Items
- **Tab Navigation**: Circular selector with three tabs
  - Active items
  - Resolved items
  - Expired items
- **Auto-refresh**: Reloads on resume after editing/deleting
- **Item Actions**: Edit and view details for each item

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

### 4. Image Handling System

#### Frontend (EditPhotoAdapter)
- Handles both Uri (new images) and String URLs (existing images)
- Displays existing images from server using Glide
- Allows adding/removing images dynamically
- Photo count display (e.g., "3/5 Photos")

#### Backend (processItemImages middleware)
- **Smart Detection**:
  - Checks if image data starts with "uploads/" (existing path)
  - If existing: Keeps path unchanged
  - If new: Decodes base64 and saves with unique filename
- **File Storage**: Organized in uploads/items/ directory
- **URL Generation**: Returns full server URL for images

### 5. Time Formatting (TimeFormatter utility)
- Converts ISO 8601 timestamps to human-readable format
- Examples: "Just now", "5 minutes ago", "2 hours ago", "3 days ago"
- Used consistently across Item cards and My Items

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
- `GET /api/items` - Get all items with optional filters (type, category, search)
- `GET /api/items/:id` - Get specific item details
- `GET /api/items/my-items` - Get current user's items with status filter
- `PUT /api/items/:id` - Update item with smart image handling
- `DELETE /api/items/:id` - Delete item (soft delete)

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
  category: String
  location: String
  type: String (LOST/FOUND)
  status: String (ACTIVE/RESOLVED/EXPIRED)
  userId: Int
  userName: String
  userEmail: String
  userProfilePic: String?
  createdAt: String
  updatedAt: String
}
```

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

### Auto-Refresh Pattern
- `onResume()` lifecycle method in Profile and MyItems activities
- Automatically reloads data when returning from child activities
- Ensures UI always shows latest data after edits/deletions

### Circular Tab Selector
- Background: `login_signup_background` (rounded gray container)
- Selected: `selected_button_login_signup` (white rounded background)
- Used in: Report page (Lost/Found), My Items page (Active/Resolved/Expired)

## Security Features
- JWT-based authentication
- Password hashing with bcrypt
- Token validation middleware on all protected routes
- Soft deletes (preserves data integrity)
- Input validation on both frontend and backend

## User Experience Features
- Loading states on all async operations
- Error handling with user-friendly messages
- Confirmation dialogs for destructive actions
- Field validation with inline error messages
- Read-only email field (cannot be changed)
- Auto-clearing of password fields after successful change
- Progress indicators during API calls

## Database Schema
- **users** table: User profiles with authentication
- **items** table: Lost/found items with soft delete support
- Foreign key relationships for data integrity
- Timestamps for created_at, updated_at, deleted_at

## Future Enhancements (Potential)
- Real-time chat between users
- Push notifications for item matches
- Item claiming/verification system
- Advanced search with filters
- Item expiration handling
- User reputation system

---

**Last Updated**: December 7, 2025
**Version**: 1.0.0
