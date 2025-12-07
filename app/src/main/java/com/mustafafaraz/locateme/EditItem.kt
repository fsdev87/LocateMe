package com.mustafafaraz.locateme

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.adapter.EditPhotoAdapter
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.model.UpdateItemRequest
import com.mustafafaraz.locateme.utils.ImageHelper
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.launch

class EditItem : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var itemTypeSpinner: Spinner
    private lateinit var itemTitle: EditText
    private lateinit var itemCategory: Spinner
    private lateinit var itemDescription: EditText
    private lateinit var itemStatusSpinner: Spinner
    private lateinit var itemLocation: EditText
    private lateinit var locationLabel: TextView
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var photoCountText: TextView
    private lateinit var photosRecyclerView: RecyclerView
    private lateinit var addPhotoButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var tokenManager: TokenManager
    private lateinit var photoAdapter: EditPhotoAdapter
    private val selectedPhotos = mutableListOf<Any>() // Can be Uri or String URL

    private var itemId: Int = -1
    private var currentItem: com.mustafafaraz.locateme.data.model.Item? = null

    // Photo picker launcher
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            for (uri in uris) {
                if (selectedPhotos.size < 5) {
                    if (!selectedPhotos.contains(uri)) {
                        photoAdapter.addPhoto(uri)
                    }
                }
            }
            updatePhotoCount()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)

        tokenManager = TokenManager(this)

        // Get item ID from intent
        itemId = intent.getIntExtra("item_id", -1)

        if (itemId == -1) {
            Toast.makeText(this, "Invalid item", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupSpinners()
        setupPhotoRecyclerView()
        setupClickListeners()

        // Load item details from API
        loadItemDetails()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        itemTypeSpinner = findViewById(R.id.item_type_spinner)
        itemTitle = findViewById(R.id.item_title)
        itemCategory = findViewById(R.id.item_category)
        itemDescription = findViewById(R.id.item_description)
        itemStatusSpinner = findViewById(R.id.item_status_spinner)
        itemLocation = findViewById(R.id.item_location)
        locationLabel = findViewById(R.id.location_label)
        saveButton = findViewById(R.id.save_button)
        deleteButton = findViewById(R.id.delete_button)
        photoCountText = findViewById(R.id.photo_count_text)
        photosRecyclerView = findViewById(R.id.photos_recycler_view)
        addPhotoButton = findViewById(R.id.add_photo_button)
    }

    private fun setupSpinners() {
        // Item Type Spinner
        val itemTypes = arrayOf("LOST", "FOUND")
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemTypes)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        itemTypeSpinner.adapter = typeAdapter

        // Update location label when item type changes
        itemTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateLocationLabel(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Category Spinner
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

        // Status Spinner
        val statuses = arrayOf("ACTIVE", "RESOLVED", "EXPIRED")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        itemStatusSpinner.adapter = statusAdapter
    }

    private fun setupPhotoRecyclerView() {
        photoAdapter = EditPhotoAdapter(selectedPhotos) { position ->
            photoAdapter.removePhoto(position)
            updatePhotoCount()
        }

        photosRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@EditItem, LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            handleSaveChanges()
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmation()
        }

        addPhotoButton.setOnClickListener {
            if (selectedPhotos.size < 5) {
                photoPickerLauncher.launch("image/*")
            } else {
                Toast.makeText(this, "Maximum 5 photos allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadItemDetails() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@EditItem, "Please login", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val authHeader = "Bearer $token"
                val response = RetrofitClient.apiService.getItemById(authHeader, itemId)

                if (response.isSuccessful && response.body()?.success == true) {
                    currentItem = response.body()?.data
                    currentItem?.let { displayItemData(it) }
                } else {
                    Toast.makeText(this@EditItem, "Failed to load item details", Toast.LENGTH_SHORT).show()
                    Log.e("EditItem", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("EditItem", "Error loading item", e)
                Toast.makeText(this@EditItem, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayItemData(item: com.mustafafaraz.locateme.data.model.Item) {
        // Set item type
        val typePosition = if (item.type == "LOST") 0 else 1
        itemTypeSpinner.setSelection(typePosition)

        // Set basic info
        itemTitle.setText(item.title)
        itemDescription.setText(item.description)
        itemLocation.setText(item.location)

        // Set category
        val categoryList = listOf("Select Category", "ELECTRONICS", "BAGS", "KEYS", "CLOTHING", "OTHER")
        val categoryPosition = categoryList.indexOf(item.category)
        if (categoryPosition >= 0) {
            itemCategory.setSelection(categoryPosition)
        }

        // Set status
        val statusList = listOf("ACTIVE", "RESOLVED", "EXPIRED")
        val statusPosition = statusList.indexOf(item.status)
        if (statusPosition >= 0) {
            itemStatusSpinner.setSelection(statusPosition)
        }

        // Load existing images
        selectedPhotos.clear()
        if (item.imageUrls.isNotEmpty()) {
            selectedPhotos.addAll(item.imageUrls)
            photoAdapter.notifyDataSetChanged()
            updatePhotoCount()
        }

        Log.d("EditItem", "Item loaded: ${item.title}, Images: ${item.imageUrls.size}")
    }

    private fun handleSaveChanges() {
        // Validate inputs
        val title = itemTitle.text.toString().trim()
        val description = itemDescription.text.toString().trim()
        val location = itemLocation.text.toString().trim()
        val type = itemTypeSpinner.selectedItem.toString()
        val category = itemCategory.selectedItem.toString()
        val status = itemStatusSpinner.selectedItem.toString()

        if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (category == "Select Category") {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                saveButton.isEnabled = false
                saveButton.text = "Saving..."

                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@EditItem, "Please login", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val authHeader = "Bearer $token"

                // Process images: convert new URIs to base64, keep existing URLs as is
                val imageList = mutableListOf<String>()
                for (photo in selectedPhotos) {
                    when (photo) {
                        is Uri -> {
                            // New image - convert to base64
                            val base64 = ImageHelper.uriToBase64(this@EditItem, photo)
                            if (base64 != null) {
                                imageList.add(base64)
                            }
                        }
                        is String -> {
                            // Existing image URL - extract the path (remove server URL)
                            val path = photo.substringAfter("/uploads/")
                            if (path.isNotEmpty()) {
                                imageList.add("uploads/$path")
                            }
                        }
                    }
                }

                val updateRequest = UpdateItemRequest(
                    title = title,
                    description = description,
                    category = category,
                    location = location,
                    type = type,
                    status = status,
                    itemImages = if (imageList.isNotEmpty()) imageList else null
                )

                val response = RetrofitClient.apiService.updateItem(authHeader, itemId, updateRequest)

                saveButton.isEnabled = true
                saveButton.text = "Save Changes"

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@EditItem, "Item updated successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@EditItem, "Failed to update item", Toast.LENGTH_SHORT).show()
                    Log.e("EditItem", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                saveButton.isEnabled = true
                saveButton.text = "Save Changes"
                Log.e("EditItem", "Error updating item", e)
                Toast.makeText(this@EditItem, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                handleDeleteItem()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleDeleteItem() {
        lifecycleScope.launch {
            try {
                deleteButton.isEnabled = false
                deleteButton.text = "Deleting..."

                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@EditItem, "Please login", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val authHeader = "Bearer $token"
                val response = RetrofitClient.apiService.deleteItem(authHeader, itemId)

                deleteButton.isEnabled = true
                deleteButton.text = "Delete Item"

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@EditItem, "Item deleted successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@EditItem, "Failed to delete item", Toast.LENGTH_SHORT).show()
                    Log.e("EditItem", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                deleteButton.isEnabled = true
                deleteButton.text = "Delete Item"
                Log.e("EditItem", "Error deleting item", e)
                Toast.makeText(this@EditItem, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLocationLabel(typePosition: Int) {
        locationLabel.text = if (typePosition == 0) {
            "Where did you lose it? *"
        } else {
            "Where did you find it? *"
        }
    }

    private fun updatePhotoCount() {
        photoCountText.text = "${selectedPhotos.size}/5 Photos"
    }
}