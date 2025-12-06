package com.mustafafaraz.locateme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tokenManager = TokenManager(this)

        // Check login status after 1 second delay
        lifecycleScope.launch {
            delay(1000) // Show splash for 1 second

            val isLoggedIn = tokenManager.isLoggedIn()

            if (isLoggedIn) {
                // User is logged in, go directly to Home
                val intent = Intent(this@MainActivity, Home::class.java)
                startActivity(intent)
            } else {
                // User not logged in, go to Login/Signup
                val intent = Intent(this@MainActivity, LoginSignup::class.java)
                startActivity(intent)
            }

            finish() // Close splash
        }
    }
}