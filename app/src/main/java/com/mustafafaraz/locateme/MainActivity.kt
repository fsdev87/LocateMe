package com.mustafafaraz.locateme

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Redirect to Login activity after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginSignup::class.java)
            startActivity(intent)
            finish() // Close splash screen so user can't go back to it
        }, 3000) // 3000 milliseconds = 3 seconds
    }
}