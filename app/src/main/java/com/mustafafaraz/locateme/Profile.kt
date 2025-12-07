package com.mustafafaraz.locateme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.repository.UserProfileRepository
import com.mustafafaraz.locateme.utils.TokenManager
import com.mustafafaraz.locateme.utils.NetworkUtils
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

class Profile : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var profileAvatar: CircleImageView
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profileDepartment: TextView
    private lateinit var itemsPostedCount: TextView
    private lateinit var itemsReunitedCount: TextView
    private lateinit var successRateText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize TokenManager and Repository
        tokenManager = TokenManager(this)
        userProfileRepository = UserProfileRepository(this)

        // Initialize views
        initializeViews()

        // Load profile data
        loadProfileData()

        // Setup click listeners
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Reload profile when returning from EditProfile activity
        loadProfileData()
    }

    private fun initializeViews() {
        profileAvatar = findViewById(R.id.profile_avatar)
        profileName = findViewById(R.id.profile_name)
        profileEmail = findViewById(R.id.profile_email)
        profileDepartment = findViewById(R.id.profile_department)
        itemsPostedCount = findViewById(R.id.items_posted_count)
        itemsReunitedCount = findViewById(R.id.items_reunited_count)
        successRateText = findViewById(R.id.success_rate_text)
        progressBar = findViewById(R.id.profile_progress_bar)
    }

    private fun loadProfileData() {
        lifecycleScope.launch {
            try {
                // Show loading
                progressBar.visibility = View.VISIBLE

                // Check if online
                if (NetworkUtils.isOnline(this@Profile)) {
                    // ONLINE: Fetch from API first
                    val result = userProfileRepository.syncProfile()

                    result.onSuccess { user ->
                        progressBar.visibility = View.GONE
                        displayUserData(user)
                        Log.d("Profile", "✅ Profile loaded from server")
                    }.onFailure { error ->
                        Log.e("Profile", "API failed, loading from cache: ${error.message}")

                        // API failed, try loading from cache
                        loadProfileFromCache()
                    }
                } else {
                    // OFFLINE: Load from cache
                    Log.d("Profile", "📴 Offline mode - loading from cache")
                    loadProfileFromCache()
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e("Profile", "Error loading profile", e)
                Toast.makeText(this@Profile, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun loadProfileFromCache() {
        val cachedUser = userProfileRepository.getUserProfileOnce()

        progressBar.visibility = View.GONE

        if (cachedUser != null) {
            displayUserData(cachedUser)
            if (!NetworkUtils.isOnline(this@Profile)) {
                Toast.makeText(this@Profile, "Offline mode - showing cached profile", Toast.LENGTH_SHORT).show()
            }
        } else {
            // No cached data and offline
            Toast.makeText(this@Profile, "No cached data. Please connect to internet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayUserData(user: com.mustafafaraz.locateme.data.model.User) {
        // Load profile picture
        if (!user.profilePic.isNullOrEmpty()) {
            Glide.with(this)
                .load(user.profilePic)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(profileAvatar)
        } else {
            profileAvatar.setImageResource(R.drawable.ic_person)
        }

        // Set user info
        profileName.text = user.fullName
        profileEmail.text = user.email
        profileDepartment.text = user.department

        // Set statistics
        user.stats?.let { stats ->
            itemsPostedCount.text = stats.totalItems.toString()
            itemsReunitedCount.text = stats.resolvedItems.toString()
            successRateText.text = "${stats.successRate.toInt()}%"
        } ?: run {
            // Default values if stats are null
            itemsPostedCount.text = "0"
            itemsReunitedCount.text = "0"
            successRateText.text = "0%"
        }

        Log.d("Profile", "Profile loaded: ${user.fullName}, Stats: ${user.stats}")
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.edit_profile_button).setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.quick_myitems).setOnClickListener {
            val intent = Intent(this, MyItems::class.java)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.quick_saved).setOnClickListener {
            val intent = Intent(this, SavedItems::class.java)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.quick_messages).setOnClickListener {
            val intent = Intent(this, ChatList::class.java)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.quick_settings).setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.sign_out_button).setOnClickListener {
            handleSignOut()
        }

        // Bottom navigation
        findViewById<View>(R.id.nav_search).setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.nav_report).setOnClickListener {
            val intent = Intent(this, Report::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.nav_profile).setOnClickListener {
            // Already on profile screen
        }
    }

    private fun handleSignOut() {
        lifecycleScope.launch {
            try {
                // First, call backend to clear FCM token
                val token = tokenManager.getToken()
                if (!token.isNullOrEmpty()) {
                    try {
                        val authHeader = "Bearer $token"
                        val response = RetrofitClient.apiService.logout(authHeader)

                        if (response.isSuccessful) {
                            Log.d("Profile", "✅ FCM token cleared from backend")
                        } else {
                            Log.e("Profile", "❌ Failed to clear FCM token: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("Profile", "❌ Error calling logout API", e)
                        // Continue with local logout even if backend call fails
                    }
                }

                // Then clear all local auth data
                tokenManager.clearAllData()

                // Show success message
                Toast.makeText(this@Profile, "Signed out successfully", Toast.LENGTH_SHORT).show()

                navigateToLogin()
            } catch (e: Exception) {
                Toast.makeText(this@Profile, "Error signing out: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLogin() {
        // Navigate to LoginSignup and clear the entire activity stack
        val intent = Intent(this@Profile, LoginSignup::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
