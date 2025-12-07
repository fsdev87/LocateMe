package com.mustafafaraz.locateme

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.adapter.PhotoAdapter
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.model.CreateItemRequest
import com.mustafafaraz.locateme.utils.ImageHelper
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.launch

class Report : AppCompatActivity() {

    private lateinit var toggleLost: TextView
    private lateinit var toggleFound: TextView
    private lateinit var locationLabel: TextView
    private lateinit var reportButton: Button
    private lateinit var photoCountText: TextView

    private lateinit var itemTitle: EditText
    private lateinit var itemCategory: Spinner
    private lateinit var itemDescription: EditText
    private lateinit var itemLocation: EditText

    private lateinit var photosRecyclerView: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter
    private val selectedPhotos = mutableListOf<Uri>()

    private lateinit var tokenManager: TokenManager
    private var isLostItem = true

    // Photo picker launcher
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        // `uris` is never null for GetMultipleContents(), so drop the null check
        if (uris.isNotEmpty()) {
            for (uri in uris) {
                if (selectedPhotos.size < 5) {
                    // Avoid duplicates: only add if not already present
                    if (!selectedPhotos.contains(uri)) {
                        photoAdapter.addPhoto(uri)
                    }
                }
            }
            updatePhotoCount()
            updatePhotosVisibility()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        tokenManager = TokenManager(this)

        initializeViews()
        setupToggleListeners()
        setupSpinners()
        setupPhotoRecyclerView()
        setupClickListeners()
    }

    private fun initializeViews() {
        toggleLost = findViewById(R.id.toggle_lost)
        toggleFound = findViewById(R.id.toggle_found)
        locationLabel = findViewById(R.id.location_label)
        reportButton = findViewById(R.id.report_button)
        photoCountText = findViewById(R.id.photo_count_text)

        itemTitle = findViewById(R.id.item_title)
        itemCategory = findViewById(R.id.item_category)
        itemDescription = findViewById(R.id.item_description)
        itemLocation = findViewById(R.id.item_location)

        photosRecyclerView = findViewById(R.id.photos_recycler_view)
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
        // Category Spinner - MUST match backend categories exactly
        val categories = arrayOf(
            "Select Category",
            "ELECTRONICS",
            "BAGS",
            "KEYS",
            "CLOTHING",
            "OTHER"
        )
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        itemCategory.adapter = categoryAdapter
    }

    private fun setupPhotoRecyclerView() {
        photoAdapter = PhotoAdapter(selectedPhotos) { position ->
            // The adapter already removes from the shared list; don't remove from
            // `selectedPhotos` here as that caused double-removal/duplicates before.
            photoAdapter.removePhoto(position)
            updatePhotoCount()
            updatePhotosVisibility()
        }

        photosRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@Report, LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }
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
            if (selectedPhotos.size < 5) {
                photoPickerLauncher.launch("image/*")
            } else {
                Toast.makeText(this, "Maximum 5 photos allowed", Toast.LENGTH_SHORT).show()
            }
        }

        // Bottom navigation
        findViewById<View>(R.id.nav_search).setOnClickListener {
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

    private fun updatePhotoCount() {
        photoCountText.text = "${selectedPhotos.size}/5 Photos"
    }

    private fun updatePhotosVisibility() {
        photosRecyclerView.visibility = if (selectedPhotos.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun handleReportSubmit() {
        val title = itemTitle.text.toString().trim()
        val category = itemCategory.selectedItem.toString()
        val description = itemDescription.text.toString().trim()
        val location = itemLocation.text.toString().trim()

        // Validation
        if (title.isEmpty()) {
            itemTitle.error = "Please enter item title"
            itemTitle.requestFocus()
            return
        }

        if (category == "Select Category") {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            itemDescription.error = "Please enter description"
            itemDescription.requestFocus()
            return
        }

        if (location.isEmpty()) {
            itemLocation.error = "Please enter location"
            itemLocation.requestFocus()
            return
        }

        if (selectedPhotos.isEmpty()) {
            Toast.makeText(this, "Please add at least 1 photo", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button and show loading
        reportButton.isEnabled = false
        reportButton.text = "Uploading..."

        // Submit to backend
        submitItem(title, category, description, location)
    }

    private fun submitItem(title: String, category: String, description: String, location: String) {
        lifecycleScope.launch {
            try {
                // Get raw token and validate
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@Report, "You must be logged in to report an item", Toast.LENGTH_LONG).show()
                    reportButton.isEnabled = true
                    reportButton.text = if (isLostItem) "Report Lost Item" else "Report Found Item"
                    return@launch
                }

                val authHeader = "Bearer $token"

                // Determine item type
                val itemType = if (isLostItem) "LOST" else "FOUND"

                // Convert images to Base64 strings
                val base64Images = mutableListOf<String>()
                for (uri in selectedPhotos) {
                    val base64String = ImageHelper.uriToBase64(this@Report, uri)
                    if (base64String != null) {
                        base64Images.add(base64String)
                    } else {
                        Log.w("Report", "Failed to encode image: $uri")
                    }
                }

                // Debug logs: token presence and image count
                Log.d("Report", "Has token: ${!token.isNullOrEmpty()}, tokenPreview=${token?.take(10)?.replace(".*", "***")}")
                Log.d("Report", "Preparing to upload ${base64Images.size} base64-encoded image(s)")

                // Build request with base64 images
                val request = CreateItemRequest(
                    title = title,
                    description = description,
                    category = category,
                    location = location,
                    type = itemType,
                    itemImages = base64Images
                )

                // Make API call
                val response = RetrofitClient.apiService.createItem(
                    token = authHeader,
                    request = request
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(
                        this@Report,
                        "Item reported successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Clear form
                    clearForm()

                    // Navigate back to home
                    val intent = Intent(this@Report, Home::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                } else {
                    // Try to extract server error message
                    val errBody = response.errorBody()?.string()
                    val serverMessage = response.body()?.message ?: errBody ?: "Failed to report item. Please try again."

                    Log.e("Report", "Create item failed: code=${response.code()} msg=$serverMessage body=$errBody")

                    Toast.makeText(this@Report, serverMessage, Toast.LENGTH_LONG).show()

                    // Re-enable button
                    reportButton.isEnabled = true
                    reportButton.text = if (isLostItem) "Report Lost Item" else "Report Found Item"
                }
            } catch (e: Exception) {
                Log.e("Report", "Network/submit error", e)
                Toast.makeText(
                    this@Report,
                    "Network error: ${e.message}. Please check your connection.",
                    Toast.LENGTH_LONG
                ).show()

                // Re-enable button
                reportButton.isEnabled = true
                reportButton.text = if (isLostItem) "Report Lost Item" else "Report Found Item"
            }
        }
    }

    private fun clearForm() {
        itemTitle.text.clear()
        itemCategory.setSelection(0)
        itemDescription.text.clear()
        itemLocation.text.clear()
        selectedPhotos.clear()
        photoAdapter.notifyDataSetChanged()
        updatePhotoCount()
        updatePhotosVisibility()
    }
}