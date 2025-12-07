package com.mustafafaraz.locateme

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ItemDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        // Get intent data
        val itemTitle = intent.getStringExtra("item_title") ?: "Item"
        val itemDescription = intent.getStringExtra("item_description") ?: ""
        val itemBadge = intent.getStringExtra("item_badge") ?: "LOST"
        val location = intent.getStringExtra("location") ?: ""
        val time = intent.getStringExtra("time") ?: ""
        val personName = intent.getStringExtra("person_name") ?: ""
        val contactEmail = intent.getStringExtra("contact_email") ?: ""
        val contactPhone = intent.getStringExtra("contact_phone") ?: ""

        // Set up views
        val backButton = findViewById<ImageView>(R.id.back_button)
        val titleView = findViewById<TextView>(R.id.item_title)
        var badgeView = findViewById<TextView>(R.id.item_badge)
        val descriptionView = findViewById<TextView>(R.id.item_description)
        val locationView = findViewById<TextView>(R.id.item_location)
        val timeView = findViewById<TextView>(R.id.item_time)
        val personView = findViewById<TextView>(R.id.item_person)
        val messageButton = findViewById<LinearLayout>(R.id.message_button)
        val emailButton = findViewById<LinearLayout>(R.id.email_button)
        val favoriteButton = findViewById<ImageView>(R.id.favorite_button)
        val shareButton = findViewById<ImageView>(R.id.share_button)
        val bookmarkButton = findViewById<ImageView>(R.id.bookmark_button)
        val reporterFullName = findViewById<TextView>(R.id.reporter_full_name)
        val reporterStudentId = findViewById<TextView>(R.id.reporter_student_id)
        val reporterBatch = findViewById<TextView>(R.id.reporter_batch)
        val reporterDepartment = findViewById<TextView>(R.id.reporter_department)


        // Set content
        titleView.text = itemTitle
        badgeView.text = itemBadge
        if (itemBadge == "LOST") {
            badgeView.setBackgroundResource(R.drawable.lost_badge)
        } else {
            badgeView.setBackgroundResource(R.drawable.found_badge)
        }
        descriptionView.text = itemDescription
        locationView.text = location
        timeView.text = time
        personView.text = personName

        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Message button - send SMS or open messaging app
        messageButton.setOnClickListener {
            if (contactPhone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:$contactPhone")
                }
                startActivity(intent)
            } else {
                // Fallback: Show toast or message
                android.widget.Toast.makeText(
                    this,
                    "Contact phone not available",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Email button
        emailButton.setOnClickListener {
            if (contactEmail.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(contactEmail))
                    putExtra(Intent.EXTRA_SUBJECT, "Regarding: $itemTitle")
                }
                startActivity(Intent.createChooser(intent, "Send Email"))
            } else {
                android.widget.Toast.makeText(
                    this,
                    "Contact email not available",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Favorite button
        favoriteButton.setOnClickListener {
            // Toggle favorite state
            favoriteButton.isSelected = !favoriteButton.isSelected
        }

        // Share button
        shareButton.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out this item: $itemTitle - $itemDescription")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share Item"))
        }

        // Bookmark button
        bookmarkButton.setOnClickListener {
            bookmarkButton.isSelected = !bookmarkButton.isSelected
        }
    }
}

