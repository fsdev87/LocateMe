# LocateMe Backend - Complete Setup Checklist

## ✅ Step-by-Step Setup Guide

### 1️⃣ Railway Database Setup

1. Go to [Railway.app](https://railway.app)
2. Create new project
3. Add MySQL database service
4. Copy connection details:
   - Host
   - Port (usually 3306)
   - Username (usually root)
   - Password
   - Database name (usually railway)

### 2️⃣ Create Schema in Railway

**Option A: Using Railway's Query Tab**
1. Go to your MySQL service → Data tab
2. Click "Query"
3. Paste the entire SQL schema from the artifact
4. Click "Run"

**Option B: Using MySQL Client**
```bash
mysql -h <railway-host> -u root -p railway < schema.sql
```

### 3️⃣ Firebase Setup (for FCM)

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create new project or use existing
3. Go to Project Settings → Service Accounts
4. Click "Generate New Private Key"
5. Download the JSON file
6. Extract these values for `.env`:
   - `project_id`
   - `client_email`
   - `private_key`

### 4️⃣ Local Backend Setup

```bash
# Clone or create project directory
mkdir locateme-backend
cd locateme-backend

# Initialize project
npm init -y

# Install dependencies
npm install express mysql2 bcryptjs jsonwebtoken dotenv cors multer firebase-admin

# Install dev dependencies
npm install --save-dev nodemon

# Create folder structure
mkdir config middleware routes controllers utils uploads
mkdir uploads/profiles uploads/items uploads/messages
```

### 5️⃣ Create `.env` File

Create `.env` in root directory:

```env
# Server
PORT=5000
NODE_ENV=development

# Railway MySQL Database
DB_HOST=containers-us-west-123.railway.app
DB_PORT=3306
DB_USER=root
DB_PASSWORD=your_password_here
DB_NAME=railway

# JWT
JWT_SECRET=your_super_secret_jwt_key_min_32_chars_long
JWT_EXPIRES_IN=7d

# Firebase (for FCM notifications)
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@your-project.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nYOUR_PRIVATE_KEY_HERE\n-----END PRIVATE KEY-----\n"

# Server URL (update after deploying to Render)
SERVER_URL=http://localhost:5000
```

### 6️⃣ Copy All Backend Files

Copy all the files from the artifacts into your project:

```
locateme-backend/
├── config/
│   └── db.js
├── middleware/
│   ├── auth.js
│   └── upload.js
├── routes/
│   ├── auth.js
│   ├── users.js
│   ├── items.js
│   ├── chats.js
│   ├── messages.js
│   └── notifications.js
├── controllers/
│   ├── authController.js
│   ├── userController.js
│   ├── itemController.js
│   ├── chatController.js
│   ├── messageController.js
│   └── notificationController.js
├── utils/
│   ├── jwt.js
│   └── fcm.js
├── uploads/
│   ├── profiles/
│   ├── items/
│   └── messages/
├── .env
├── .gitignore
├── server.js
└── package.json
```

### 7️⃣ Update package.json

Add these scripts:
```json
{
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js"
  }
}
```

### 8️⃣ Test Locally

```bash
# Run in development mode
npm run dev

# Should see:
# ✅ MySQL Database connected successfully
# ✅ Firebase Admin SDK initialized
# 🚀 Server is running on port 5000
```

Test endpoints:
```bash
# Health check
curl http://localhost:5000/health

# Test signup
curl -X POST http://localhost:5000/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@student.fast.edu.pk",
    "password": "test123",
    "studentId": "23I-1234",
    "batch": "2023",
    "department": "CS",
    "section": "A"
  }'
```

### 9️⃣ Deploy to Render

1. Push code to GitHub:
```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin <your-github-repo>
git push -u origin main
```

2. Go to [Render Dashboard](https://dashboard.render.com)
3. Click "New +" → "Web Service"
4. Connect GitHub repository
5. Configure:
   - **Name**: `locateme-backend`
   - **Environment**: `Node`
   - **Build Command**: `npm install`
   - **Start Command**: `npm start`
   - **Instance Type**: Free

6. Add Environment Variables (all from your `.env`)

7. Deploy!

8. After deployment, get your Render URL: `https://locateme-backend.onrender.com`

9. Update `.env` on Render:
   - Change `SERVER_URL` to your Render URL

### 🔟 Test Production Deployment

```bash
# Replace with your Render URL
curl https://locateme-backend.onrender.com/health
```

---

## 🔧 Troubleshooting

### Database Connection Issues
```bash
# Test connection directly
node test-db.js
```

### Firebase Issues
- Make sure private key has `\n` properly escaped in `.env`
- Ensure Firebase project has Cloud Messaging enabled

### Image Upload Issues
- Check `uploads/` directories exist
- Verify file permissions on server

### CORS Issues
- CORS is already configured in `server.js`
- If issues persist, add your Android app's IP to allowed origins

---

## 📱 Android Integration

### 1. Add Retrofit Dependencies

```gradle
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
```

### 2. Create API Service

```kotlin
interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @GET("api/items")
    suspend fun getItems(
        @Header("Authorization") token: String,
        @Query("type") type: String? = null
    ): Response<ItemsResponse>
    
    @Multipart
    @POST("api/items")
    suspend fun createItem(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("category") category: RequestBody,
        @Part("location") location: RequestBody,
        @Part("type") type: RequestBody,
        @Part itemImages: List<MultipartBody.Part>
    ): Response<ItemResponse>
}
```

### 3. Create Retrofit Client

```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://your-app.onrender.com/"
    
    val instance: ApiService by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        
        retrofit.create(ApiService::class.java)
    }
}
```

---

## 🎯 Testing Endpoints

Use Postman or Thunder Client to test all endpoints:

1. **Signup** → Get token
2. **Login** → Verify token
3. **Create Item** → Test image upload
4. **Get Items** → Verify filtering
5. **Create Chat** → Test messaging
6. **Send Message** → Verify FCM notification

---

## 📊 Database Monitoring

Monitor your Railway database:
- Check connections
- View query logs
- Monitor storage usage
- Backup regularly

---

## 🔒 Security Checklist

- ✅ JWT secret is strong (32+ characters)
- ✅ Passwords are hashed with bcrypt
- ✅ Firebase private key is secure
- ✅ CORS is properly configured
- ✅ Input validation on all endpoints
- ✅ File upload limits are set (5MB)
- ✅ SQL injection protection (parameterized queries)

---

## 📈 Performance Optimization

- Database indexes are already added
- Connection pooling is configured
- Image compression should be done on Android side
- Consider adding Redis for caching (future enhancement)

---

## 🚀 You're All Set!

Your backend is now:
- ✅ Connected to Railway MySQL
- ✅ Deployed on Render
- ✅ Serving images via static routes
- ✅ Sending push notifications via FCM
- ✅ Ready for Android integration

Next steps:
1. Start building your Android app
2. Use Retrofit to call these APIs
3. Implement Room database for local storage
4. Add data sync logic
5. Test all features end-to-end