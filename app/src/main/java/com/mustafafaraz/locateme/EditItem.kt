package com.mustafafaraz.locateme

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.data.model.ItemPhoto
import java.io.IOException

class EditItem : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var itemTitleEditText: EditText
    private lateinit var itemDescriptionEditText: EditText
    private lateinit var itemLocationEditText: EditText
    private lateinit var itemTypeSpinner: Spinner
    private lateinit var itemCategorySpinner: Spinner
    private lateinit var itemStatusSpinner: Spinner
    private lateinit var photosRecyclerView: RecyclerView
    private lateinit var deleteButton: Button
    private lateinit var saveButton: Button

    private lateinit var photosAdapter: PhotosGridAdapter
    private val photosList = mutableListOf<ItemPhoto>()

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)

        initializeViews()
        setupSpinners()
        setupPhotosRecyclerView()
        setupClickListeners()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        itemTitleEditText = findViewById(R.id.item_title)
        itemDescriptionEditText = findViewById(R.id.item_description)
        itemLocationEditText = findViewById(R.id.item_location)
        itemTypeSpinner = findViewById(R.id.item_type_spinner)
        itemCategorySpinner = findViewById(R.id.item_category)
        itemStatusSpinner = findViewById(R.id.item_status_spinner)
        photosRecyclerView = findViewById(R.id.photos_grid_recyclerview)
        deleteButton = findViewById(R.id.delete_button)
        saveButton = findViewById(R.id.save_button)
    }

    private fun setupSpinners() {
        // Item Type Spinner
        val itemTypes = arrayOf("Select Item Type", "Lost Item", "Found Item")
        val itemTypeAdapter = ArrayAdapter(this, R.layout.spinner_item_custom, itemTypes)
        itemTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_custom)
        itemTypeSpinner.adapter = itemTypeAdapter
        itemTypeSpinner.setSelection(0)

        // Item Category Spinner
        val categories = arrayOf("Select Category", "Electronics", "Documents", "Clothing", "Accessories", "Food Items", "Personal Items", "Other")
        val categoryAdapter = ArrayAdapter(this, R.layout.spinner_item_custom, categories)
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_custom)
        itemCategorySpinner.adapter = categoryAdapter
        itemCategorySpinner.setSelection(0)

        // Item Status Spinner
        val statuses = arrayOf("Select Status", "Active", "Resolved", "Expired")
        val statusAdapter = ArrayAdapter(this, R.layout.spinner_item_custom, statuses)
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_custom)
        itemStatusSpinner.adapter = statusAdapter
        itemStatusSpinner.setSelection(0)
    }

    private fun setupPhotosRecyclerView() {
        // Create adapter with callbacks
        photosAdapter = PhotosGridAdapter(
            context = this,
            photos = photosList,
            onDeleteClick = { position ->
                deletePhoto(position)
            },
            onAddPhotoClick = {
                openImagePicker()
            }
        )

        // Setup GridLayoutManager with 3 columns
        val gridLayoutManager = GridLayoutManager(this, 3)
        photosRecyclerView.layoutManager = gridLayoutManager
        photosRecyclerView.adapter = photosAdapter

        // Add sample photos for demonstration (optional)
        addSamplePhotos()
    }

    private fun addSamplePhotos() {
        // Optional: Add 2 sample photos
        photosList.add(ItemPhoto("1", "sample_1"))
        photosList.add(ItemPhoto("2", "sample_2"))
        photosAdapter.notifyDataSetChanged()
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        deleteButton.setOnClickListener {
            onDeleteItemClick()
        }

        saveButton.setOnClickListener {
            onSaveChangesClick()
        }
    }

    private fun openImagePicker() {
        if (photosList.size >= 5) {
            Toast.makeText(this, "Maximum 5 photos allowed", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data
            if (selectedImageUri != null) {
                try {
                    // Convert URI to Bitmap
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)

                    // Add photo to list
                    val photo = ItemPhoto(
                        id = System.currentTimeMillis().toString(),
                        imageUri = selectedImageUri.toString(),
                        bitmap = bitmap
                    )
                    photosAdapter.addPhoto(photo)
                    Toast.makeText(this, "Photo added successfully", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun deletePhoto(position: Int) {
        photosAdapter.removePhoto(position)
        Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show()
    }

    private fun onDeleteItemClick() {
        // Show confirmation dialog or delete the item
        Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun onSaveChangesClick() {
        // Validate and save changes
        val title = itemTitleEditText.text.toString().trim()
        val description = itemDescriptionEditText.text.toString().trim()
        val location = itemLocationEditText.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the photos list
        val photos = photosAdapter.getPhotos()

        Toast.makeText(this, "Changes saved successfully!\nPhotos: ${photos.size}", Toast.LENGTH_SHORT).show()

        // Here you would normally save the data to backend/database
    }
}

