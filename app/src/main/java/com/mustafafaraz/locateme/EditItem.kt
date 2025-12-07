package com.mustafafaraz.locateme

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)

        initializeViews()
        setupSpinners()
        loadStaticData()
        setupClickListeners()
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
    }

    private fun setupSpinners() {
        // Item Type Spinner
        val itemTypes = arrayOf("Lost Item", "Found Item")
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
        val statuses = arrayOf("active", "resolved", "expired")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        itemStatusSpinner.adapter = statusAdapter
    }

    private fun loadStaticData() {
        // Load static/hardcoded data
        itemTitle.setText("Dark Chocolate Cake")
        itemDescription.setText("Lost my chocolate cake in the library. It was in a blue container with my name on it. Please contact me if you find it.")
        itemLocation.setText("Library - 3rd floor")

        // Set default selections
        itemTypeSpinner.setSelection(0) // Lost Item
        itemCategory.setSelection(5) // OTHER (valid index)
        itemStatusSpinner.setSelection(0) // active
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        }

        deleteButton.setOnClickListener {
            Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateLocationLabel(typePosition: Int) {
        locationLabel.text = if (typePosition == 0) {
            "Where did you lose it? *"
        } else {
            "Where did you find it? *"
        }
    }
}