# LocateMe - Lost and Found Android Application

## 📋 Project Overview

**LocateMe** is a comprehensive Lost and Found Android application built for university campus environments. It enables students to report lost or found items, search through listings, communicate with other users, and manage their items efficiently.

**Tech Stack:**
- **Frontend:** Kotlin (Android)
- **Backend:** Node.js + Express
- **Database:** MySQL
- **Image Loading:** Glide
- **Networking:** Retrofit + OkHttp
- **Data Storage:** DataStore (for auth tokens)
- **Architecture:** MVVM-inspired with Repository pattern

---

## 🏗️ Application Architecture

### Frontend Structure
```
app/src/main/java/com/mustafafaraz/locateme/
├── MainActivity.kt                 # Splash screen with auth check
├── LoginSignup.kt                  # Authentication screen
├── Home.kt                         # Main feed with filtering & search
├── Report.kt                       # Create new item (Lost/Found)
├── ItemDetails.kt                  # Detailed view with image gallery
├── Profile.kt                      # User profile with sign out
├── MyItems.kt                      # User's own items
├── EditItem.kt                     # Edit existing items
├── SavedItems.kt                   # Bookmarked items
├── ChatList.kt                     # Chat conversations
└── EditProfile.kt                  # Profile editing

├── adapter/
│   ├── ItemAdapter.kt              # RecyclerView adapter for home feed
│   ├── MyItemsAdapter.kt           # Adapter for user's items
│   ├── ImageGalleryAdapter.kt      # Horizontal image thumbnails
│   └── PhotoAdapter.kt             # Photo selection in Report

├── data/
│   ├── api/
│   │   ├── ApiService.kt           # Retrofit API endpoints
│   │   └── RetrofitClient.kt       # HTTP client configuration
│   └── model/
│       ├── Item.kt                 # Item data class
│       ├── User.kt                 # User data class
│       ├── ApiResponse.kt          # Generic API response wrapper
│       └── AuthResponse.kt         # Authentication response

└── utils/
    ├── TokenManager.kt             # JWT token management with DataStore
    └── ImageHelper.kt              # Base64 image conversion utilities
```

### Backend Structure
```
backend/
├── server.js                       # Express server entry point
├── config/
│   └── db.js                       # MySQL connection pool
├── controllers/
│   ├── authController.js           # Signup, login, FCM token
│   ├── itemController.js           # CRUD operations for items
│   ├── userController.js           # Profile management
│   ├── chatController.js           # Chat functionality
│   └── messageController.js        # Messaging
├── middleware/
│   ├── auth.js                     # JWT verification
│   └── upload.js                   # Base64 to file conversion
├── routes/
│   ├── auth.js                     # /api/auth/*
│   ├── items.js                    # /api/items/*
│   ├── users.js                    # /api/users/*
│   └── chats.js                    # /api/chats/*
└── uploads/
    ├── items/                      # Item images
    ├── profiles/                   # Profile pictures
    └── messages/                   # Message media
```

---

## 🔑 Key Features Implemented

### 1. Authentication & Authorization
- ✅ **JWT-based authentication**
- ✅ **Token stored in DataStore** (encrypted preferences)
- ✅ **Auto-login on app restart** (MainActivity checks token)
- ✅ **Secure sign-out** (clears session + activity stack)
- ✅ **University email validation** (.edu.pk domain)

**Flow:**
```
Login/Signup → Backend validates → Returns JWT + User data → 
TokenManager saves → MainActivity checks on launch → Auto-navigate
```

### 2. Item Management

#### Create Item (Report Lost/Found)
- ✅ **Photo selection** (up to 5 images)
- ✅ **Base64 image encoding** (frontend converts URI to Base64)
- ✅ **Category selection** (ELECTRONICS, BAGS, KEYS, CLOTHING, OTHER)
- ✅ **Type toggle** (LOST/FOUND with dynamic UI)
- ✅ **Validation** (required fields checked)
- ✅ **Backend processing** (Base64 → File → MySQL JSON array)

**Important Implementation Details:**
- **Image Field Name:** `itemImages` (array of Base64 strings)
- **Backend Middleware:** `processItemImages` in `upload.js`
- **Storage:** `uploads/items/itemImage-{timestamp}-{random}.jpg`
- **Database:** `image_urls` stored as JSON array

#### Home Feed (Dynamic Filtering)
- ✅ **Category filters** (All, Electronics, Bags, Keys, Clothing, Other)
- ✅ **Type filters** (All Items, Lost, Found)
- ✅ **Real-time search** (with 500ms debouncing)
- ✅ **Combined filtering** (category + type + search work together)
- ✅ **RecyclerView implementation** (efficient scrolling)
- ✅ **Excludes user's own items** (only shows other users' posts)

**API Query Examples:**
```
GET /api/items?category=ELECTRONICS&type=LOST
GET /api/items?search=phone
GET /api/items?category=BAGS&type=FOUND&search=black
```

#### Item Details
- ✅ **Full item information** (title, description, location, time)
- ✅ **Image gallery** (horizontal scrollable thumbnails)
- ✅ **Click thumbnail to change main image**
- ✅ **Reporter details** (name, student ID, batch, department, section)
- ✅ **Action buttons** (Email, Message, Share, Bookmark, Favorite)
- ✅ **Safety tips** (campus security guidelines)

**Image Gallery Implementation:**
- **Main ImageView:** Displays selected image (default: first image)
- **RecyclerView:** Horizontal scroll with thumbnails
- **Click handler:** Updates main ImageView via Glide

#### My Items
- ✅ **Status filtering** (Active, Resolved, Expired)
- ✅ **Tab switching** with visual feedback
- ✅ **Edit functionality** (navigate to EditItem)
- ✅ **API-driven** (loads from `/api/items/my-items`)

### 3. Search Functionality
- ✅ **Debounced search** (waits 500ms after typing stops)
- ✅ **Searches in:** title, description, location
- ✅ **Works with filters** (category + type + search combined)
- ✅ **Auto-clear** (removing search text reloads all items)

**Implementation:**
```kotlin
TextWatcher with coroutine Job:
- User types → Cancel previous job → Delay 500ms → API call
```

### 4. Image Handling

#### Frontend (Kotlin)
```kotlin
// Convert URI to Base64
val base64 = ImageHelper.uriToBase64(context, uri)

// Send to backend
CreateItemRequest(
    itemImages = listOf(base64String1, base64String2)
)

// Load from URL
Glide.with(context)
    .load(imageUrl.replace(":5000", ""))
    .into(imageView)
```

#### Backend (Node.js)
```javascript
// Middleware processes Base64
processItemImages → decodeBase64Image → validateImage → 
saveToFile → returns array of file paths

// Store in MySQL
image_urls: JSON array ["uploads/items/img1.jpg", "uploads/items/img2.jpg"]

// Format response with full URLs
formatItem: maps each path to ${SERVER_URL}/${path}
```

**Critical Fix Applied:**
- Backend was adding `:5000` port to URLs
- Frontend strips `:5000` in Item model getter
- Ensures images load correctly from Render deployment

---

## 📊 Data Models

### Item Model (Frontend)
```kotlin
data class Item(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("image_urls")
    private val _imageUrls: List<String>,
    val category: String,
    val location: String,
    val type: String,                    // LOST or FOUND
    val status: String,                  // ACTIVE, RESOLVED, EXPIRED
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("user_email")
    val userEmail: String,
    @SerializedName("user_student_id")
    val userStudentId: String?,
    @SerializedName("user_batch")
    val userBatch: String?,
    @SerializedName("user_department")
    val userDepartment: String?,
    @SerializedName("user_section")
    val userSection: String?,
    @SerializedName("user_profile_pic")
    private val _userProfilePic: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
) {
    // Remove :5000 port from URLs
    val imageUrls: List<String>
        get() = _imageUrls.map { it.replace(":5000", "") }
    
    val userProfilePic: String?
        get() = _userProfilePic?.replace(":5000", "")
}
```

**Key Points:**
- Uses `@SerializedName` to map snake_case (backend) → camelCase (Kotlin)
- Custom getters strip `:5000` port from image URLs
- Nullable fields for optional user data

### Database Schema (MySQL)

**Items Table:**
```sql
CREATE TABLE items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    image_urls JSON DEFAULT NULL,
    category ENUM('ELECTRONICS', 'BAGS', 'KEYS', 'CLOTHING', 'OTHER'),
    location VARCHAR(255) NOT NULL,
    type ENUM('LOST', 'FOUND') NOT NULL,
    status ENUM('ACTIVE', 'RESOLVED', 'EXPIRED') DEFAULT 'ACTIVE',
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**Users Table:**
```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    student_id VARCHAR(100) NOT NULL,
    batch VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    section VARCHAR(50) NOT NULL,
    profile_pic VARCHAR(500) DEFAULT NULL,
    fcm_token VARCHAR(500) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🔄 Complete User Workflows

### 1. First-Time User Flow
```
App Launch → MainActivity (Splash) → No token found → 
LoginSignup → User enters details → Backend validates → 
Returns JWT + user data → TokenManager.saveAuthData() → 
Navigate to Home → Show items feed
```

### 2. Returning User Flow
```
App Launch → MainActivity (Splash) → 
TokenManager.isLoggedIn() = true → Navigate to Home directly
```

### 3. Report Lost Item Flow
```
Home → Bottom Nav "Report" → Report Activity →
Select "Lost Item" → Fill title, category, description, location →
Select photos (up to 5) → Photos converted to Base64 →
Click "Report Lost Item" → API POST /api/items →
Middleware processes Base64 → Saves files → Inserts to DB →
Success → Clear form → Navigate to Home
```

### 4. Search & Filter Flow
```
Home → Type in search box → Debounce 500ms → 
API GET /api/items?search=query&category=X&type=Y →
Backend searches in title/description/location →
Returns filtered items → RecyclerView updates → Display results
```

### 5. View Item Details Flow
```
Home → Click item card → Intent with item_id →
ItemDetails → API GET /api/items/{id} →
Backend joins with users table → Returns full item + user data →
Display: images, details, reporter info, safety tips →
Image gallery: click thumbnail → update main image
```

### 6. Sign Out Flow
```
Profile → Click "Sign Out" → 
TokenManager.clearAllData() (removes token, user info) →
Intent to LoginSignup with FLAG_ACTIVITY_CLEAR_TASK →
Clears entire activity stack → Must login again
```

---

## 🛠️ API Endpoints

### Authentication
```
POST /api/auth/signup
POST /api/auth/login
PUT  /api/auth/fcm-token
```

### Items
```
GET    /api/items                  # Home feed (excludes own items)
       ?type=LOST|FOUND
       &category=ELECTRONICS|BAGS|KEYS|CLOTHING|OTHER
       &search=query
       &status=ACTIVE|RESOLVED|EXPIRED
       
POST   /api/items                  # Create item
GET    /api/items/:id              # Get single item with user details
GET    /api/items/my-items         # Get user's own items
       ?type=LOST|FOUND
       &status=ACTIVE|RESOLVED|EXPIRED
       
PUT    /api/items/:id              # Update item
DELETE /api/items/:id              # Soft delete
```

### Users
```
GET /api/users/profile
PUT /api/users/profile
PUT /api/users/change-password
```

### Saved Items
```
POST   /api/items/save             # Save item
DELETE /api/items/save/:itemId     # Unsave item
GET    /api/items/saved            # Get saved items
```

---

## 🔧 Critical Fixes & Implementations

### 1. Image Upload Fix (Base64)
**Problem:** Frontend was sending binary, backend expected Base64
**Solution:**
- Created `ImageHelper.uriToBase64()` 
- Changed `CreateItemRequest.itemImages` to `List<String>`
- Backend middleware `processItemImages` decodes Base64
- Field name: `itemImages` (frontend) matches middleware expectation

### 2. JSON Serialization Fix
**Problem:** Gson wasn't properly serializing arrays
**Solution:**
- Added explicit `GsonBuilder()` in RetrofitClient
- Configured `.serializeNulls()` for proper null handling

### 3. Image URL Port Fix
**Problem:** Backend adding `:5000` to URLs on Render
**Solution:**
- Item model uses custom getters
- `.replace(":5000", "")` strips port from all image URLs
- Works for both item images and profile pictures

### 4. Field Name Mapping Fix
**Problem:** Backend snake_case vs Frontend camelCase mismatch
**Solution:**
- Added `@SerializedName` annotations to all fields
- Example: `@SerializedName("image_urls") val imageUrls`

### 5. Reporter Details Fix
**Problem:** Only showing "N/A" for student details
**Solution:**
- Updated backend query to include: `user_batch`, `user_department`, `user_section`
- Added fields to Item model
- ItemDetails now displays actual data

### 6. Item Model Duplication Fix
**Problem:** Two Item.kt files (one in `locateme/`, one in `data/model/`)
**Solution:**
- Deleted old `locateme/Item.kt` (had wrong types)
- All files now use `data.model.Item` (correct model)
- Fixed MyItemsAdapter import

### 7. CircleImageView Crash Fix
**Problem:** ScaleType not supported by CircleImageView library
**Solution:**
- Removed `android:scaleType` from XML layouts
- Library handles scaling automatically

### 8. EditItem Array Index Fix
**Problem:** `setSelection(6)` on 6-element array
**Solution:**
- Changed to `setSelection(5)` (valid index for "OTHER")

---

## 🎨 UI Components & Layouts

### Key XML Layouts
```
activity_home.xml           # RecyclerView, search, filters, tabs
activity_report.xml         # Photo picker, form inputs, toggles
activity_item_details.xml   # ScrollView, image gallery, details
activity_profile.xml        # User info, quick actions, sign out
activity_my_items.xml       # RecyclerView, status tabs
item_card.xml              # Single item in feed (title, image, badge)
item_gallery_image.xml     # Thumbnail in image gallery
```

### Drawables Used
```
lost_badge.xml             # Red badge for LOST items
found_badge.xml            # Green badge for FOUND items
chip_selected.xml          # Black background (selected category)
chip_unselected.xml        # Gray background (unselected)
item_card_background.xml   # Card with shadow and rounded corners
```

---

## 📱 App Navigation Flow

```
MainActivity (Splash)
    ├─ (Not Logged In) → LoginSignup
    │                        └─ (Success) → Home
    └─ (Logged In) → Home
                        ├─ Bottom Nav → Report
                        ├─ Bottom Nav → Profile
                        │                   ├─ Edit Profile
                        │                   ├─ My Items → EditItem
                        │                   ├─ Saved Items
                        │                   └─ Sign Out → LoginSignup
                        ├─ Click Item → ItemDetails
                        └─ Chat Icon → ChatList
```

---

## 🚀 Deployment Configuration

### Backend (Render)
```
Environment Variables:
- DB_HOST=your-mysql-host
- DB_USER=root
- DB_PASSWORD=your-password
- DB_NAME=locateme
- JWT_SECRET=your-secret-key
- SERVER_URL=https://locateme-backend.onrender.com
- PORT=5000 (but Render uses dynamic port)
```

### Frontend (Android)
```kotlin
RetrofitClient.kt:
private const val BASE_URL = "https://locateme-backend.onrender.com/"

// Note: No port in URL, Render handles routing
```

---

## 📝 Testing Checklist

### Authentication
- [x] Signup with valid university email
- [x] Login with correct credentials
- [x] Token persists across app restarts
- [x] Sign out clears session

### Item Management
- [x] Create lost item with images
- [x] Create found item with images
- [x] Images display correctly (no :5000 port)
- [x] Edit existing item
- [x] Delete item (soft delete)

### Filtering & Search
- [x] Filter by category (all 5 categories)
- [x] Filter by type (Lost/Found/All)
- [x] Search by title/description/location
- [x] Combined filters work together
- [x] Search debouncing (no excessive API calls)

### Item Details
- [x] All item info displays correctly
- [x] Reporter details show (not N/A)
- [x] Image gallery works
- [x] Clicking thumbnail updates main image
- [x] Email button opens email app

### My Items
- [x] Shows only user's items
- [x] Status tabs work (Active/Resolved/Expired)
- [x] Edit button navigates correctly
- [x] Click item opens details

---

## 🔐 Security Features

1. **JWT Authentication:** Tokens expire, verified on every request
2. **Password Hashing:** bcrypt with salt rounds
3. **SQL Injection Prevention:** Parameterized queries
4. **CORS Configuration:** Only allowed origins
5. **File Upload Validation:** Max size 5MB, allowed types (JPEG, PNG, GIF)
6. **User Isolation:** Users can only edit/delete their own items
7. **Soft Delete:** Items marked as deleted, not physically removed

---

## 🐛 Known Issues & Future Enhancements

### Completed ✅
- [x] Base64 image upload
- [x] Image URL port fix
- [x] Search with debouncing
- [x] Reporter details display
- [x] Item model field mapping
- [x] Sign out functionality
- [x] Dynamic item filtering

### Future Enhancements 🚧
- [ ] Real-time chat implementation
- [ ] Push notifications (FCM)
- [ ] Save/bookmark items
- [ ] Image compression before upload
- [ ] Pagination for large datasets
- [ ] Profile picture upload
- [ ] Item expiration logic
- [ ] Advanced search filters
- [ ] Item statistics on profile

---

## 📚 Key Libraries & Dependencies

### Android (build.gradle)
```gradle
// Networking
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'

// Image Loading
implementation 'com.github.bumptech.glide:glide:4.16.0'

// DataStore (Token Management)
implementation 'androidx.datastore:datastore-preferences:1.0.0'

// RecyclerView
implementation 'androidx.recyclerview:recyclerview:1.3.2'

// Circular ImageView
implementation 'de.hdodenhof:circleimageview:3.1.0'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
```

### Backend (package.json)
```json
{
  "dependencies": {
    "express": "^4.18.2",
    "mysql2": "^3.6.5",
    "bcryptjs": "^2.4.3",
    "jsonwebtoken": "^9.0.2",
    "dotenv": "^16.3.1",
    "cors": "^2.8.5",
    "firebase-admin": "^12.0.0"
  }
}
```

---

## 🎯 Summary

LocateMe is a fully functional Lost and Found application with:
- **Secure authentication** (JWT + DataStore)
- **Image upload** (Base64 encoding/decoding)
- **Dynamic filtering** (category + type + search)
- **Real-time search** (debounced)
- **Image gallery** (clickable thumbnails)
- **Complete CRUD** for items
- **Clean architecture** (separation of concerns)
- **Production-ready** (deployed on Render)

All major features are implemented and tested. The app follows Android best practices and uses modern libraries for efficient development.

---

## 📞 Contact & Repository

**Developer:** Mustafa Faraz  
**Project Type:** University Lost & Found System  
**Date Completed:** December 7, 2025

---

*This documentation covers all implementations completed as of December 7, 2025. Use this as a reference for understanding the complete application architecture and workflow.*

