package com.mustafafaraz.locateme

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Profile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val quicksaved = findViewById<LinearLayout>(R.id.quick_saved)
        val quickmessages = findViewById<LinearLayout>(R.id.quick_messages)
        val quicksettings = findViewById<LinearLayout>(R.id.quick_settings)
        val signoutbutton = findViewById<LinearLayout>(R.id.sign_out_button)
        val navsearchbtn = findViewById<LinearLayout>(R.id.nav_search)
        val navreportbtn = findViewById<LinearLayout>(R.id.nav_report)
        val navprofilebtn = findViewById<LinearLayout>(R.id.nav_profile)
        val quickmyitems = findViewById<LinearLayout>(R.id.quick_myitems)


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
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginSignup::class.java)
            startActivity(intent)
            finish()
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
            finish()
        }

        navprofilebtn.setOnClickListener {
            // Already on profile screen
        }

    }
}
