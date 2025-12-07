package com.mustafafaraz.locateme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.launch

class Profile : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize TokenManager
        tokenManager = TokenManager(this)

        val quicksaved = findViewById<LinearLayout>(R.id.quick_saved)
        val quickmessages = findViewById<LinearLayout>(R.id.quick_messages)
        val quicksettings = findViewById<LinearLayout>(R.id.quick_settings)
        val signoutbutton = findViewById<LinearLayout>(R.id.sign_out_button)
        val navsearchbtn = findViewById<LinearLayout>(R.id.nav_search)
        val navreportbtn = findViewById<LinearLayout>(R.id.nav_report)
        val navprofilebtn = findViewById<LinearLayout>(R.id.nav_profile)
        val quickmyitems = findViewById<LinearLayout>(R.id.quick_myitems)
        val editProfile = findViewById<ImageView>(R.id.edit_profile_button)

        editProfile.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        quickmyitems.setOnClickListener {
            val intent = Intent(this, MyItems::class.java)
            startActivity(intent)
        }

        quicksaved.setOnClickListener {
            val intent = Intent(this, SavedItems::class.java)
            startActivity(intent)
        }

        quickmessages.setOnClickListener {
            Toast.makeText(this, "Messages clicked", Toast.LENGTH_SHORT).show()
        }

        quicksettings.setOnClickListener {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
        }

        signoutbutton.setOnClickListener {
            handleSignOut()
        }

        // Bottom navigation wiring
        navsearchbtn.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }

        navreportbtn.setOnClickListener {
            val intent = Intent(this, Report::class.java)
            startActivity(intent)
        }

        navprofilebtn.setOnClickListener {
            // Already on profile screen
        }
    }

    private fun handleSignOut() {
        lifecycleScope.launch {
            try {
                // Clear all auth data from DataStore
                tokenManager.clearAllData()

                // Show success message
                Toast.makeText(this@Profile, "Signed out successfully", Toast.LENGTH_SHORT).show()

                // Navigate to LoginSignup and clear the entire activity stack
                val intent = Intent(this@Profile, LoginSignup::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@Profile, "Error signing out: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
