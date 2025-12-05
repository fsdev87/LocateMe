package com.mustafafaraz.locateme

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class Report : AppCompatActivity() {

    private lateinit var toggleLost: TextView
    private lateinit var toggleFound: TextView
    private lateinit var locationLabel: TextView
    private lateinit var reportButton: Button

    private lateinit var itemTitle: EditText
    private lateinit var itemCategory: Spinner
    private lateinit var itemDescription: EditText
    private lateinit var itemLocation: EditText

    private var isLostItem = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        initializeViews()
        setupToggleListeners()
        setupSpinners()
        setupClickListeners()
    }

    private fun initializeViews() {
        toggleLost = findViewById(R.id.toggle_lost)
        toggleFound = findViewById(R.id.toggle_found)
        locationLabel = findViewById(R.id.location_label)
        reportButton = findViewById(R.id.report_button)

        itemTitle = findViewById(R.id.item_title)
        itemCategory = findViewById(R.id.item_category)
        itemDescription = findViewById(R.id.item_description)
        itemLocation = findViewById(R.id.item_location)
    }

    private fun setupToggleListeners() {
        toggleLost.setOnClickListener {
            if (!isLostItem) {
                switchToLost()
            }
        }

        toggleFound.setOnClickListener {
            if (isLostItem) {
                switchToFound()
            }
        }
    }

    private fun setupSpinners() {
        // Category Spinner
        val categories = arrayOf(
            "Select Category",
            "Electronics",
            "Bags",
            "Keys",
            "Clothing",
            "Books",
            "Accessories",
            "Other"
        )
        val categoryAdapter = ArrayAdapter(this, R.layout.spinner_item, categories)
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        itemCategory.adapter = categoryAdapter
    }

    private fun setupClickListeners() {
        // Back button
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Report button
        reportButton.setOnClickListener {
            handleReportSubmit()
        }

        // Add photo button
        findViewById<View>(R.id.add_photo_button).setOnClickListener {
            // TODO: Implement photo picker
        }

        // Bottom navigation
        findViewById<View>(R.id.nav_search).setOnClickListener {
            // Navigate to home/search
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.nav_report).setOnClickListener {
            // Already on report screen
        }

        findViewById<View>(R.id.nav_profile).setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun switchToLost() {
        isLostItem = true

        // Update toggle buttons
        toggleLost.setBackgroundResource(R.drawable.selected_button_login_signup)
        toggleLost.setTextColor(ContextCompat.getColor(this, R.color.black))

        toggleFound.background = null
        toggleFound.setTextColor(ContextCompat.getColor(this, R.color.black))

        // Update text labels
        locationLabel.text = "Where did you lose it? *"
        reportButton.text = "Report Lost Item"
    }

    private fun switchToFound() {
        isLostItem = false

        // Update toggle buttons
        toggleLost.background = null
        toggleLost.setTextColor(ContextCompat.getColor(this, R.color.black))

        toggleFound.setBackgroundResource(R.drawable.selected_button_login_signup)
        toggleFound.setTextColor(ContextCompat.getColor(this, R.color.black))

        // Update text labels
        locationLabel.text = "Where did you find it? *"
        reportButton.text = "Report Found Item"
    }

    private fun handleReportSubmit() {
        val title = itemTitle.text.toString()
        val category = itemCategory.selectedItem.toString()
        val description = itemDescription.text.toString()
        val location = itemLocation.text.toString()

        // Validation
        if (title.isEmpty()) {
            itemTitle.error = "Please enter item title"
            return
        }

        if (category == "Select Category") {
            // Show error for category
            return
        }

        if (description.isEmpty()) {
            itemDescription.error = "Please enter description"
            return
        }

        if (location.isEmpty()) {
            itemLocation.error = "Please enter location"
            return
        }

        // TODO: Submit the report to backend/database
        val itemType = if (isLostItem) "Lost" else "Found"

        // For now, just finish the activity
        finish()
    }
}
