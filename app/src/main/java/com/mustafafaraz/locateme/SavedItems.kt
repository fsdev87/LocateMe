package com.mustafafaraz.locateme

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.adapter.SavedItemsAdapter

class SavedItems : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedItemsAdapter
    private lateinit var savedItemsList: MutableList<SavedItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_items)

        recyclerView = findViewById(R.id.saved_items_recyclerview)
        val backButton = findViewById<ImageView>(R.id.back_button)

        backButton.setOnClickListener {
            finish()
        }

        savedItemsList = mutableListOf(
            SavedItem(
                id = "1",
                title = "Dark Chocolate Cake",
                description = "Found a chocolate cake in the library. Contact me asap.",
                badge = "FOUND",
                location = "Library - 3rd floor",
                time = "1 hour ago",
                personName = "Abigail",
                personEmail = "abigail@example.com",
                personPhone = "+1234567890",
                imageResId = R.drawable.item_placeholder
            ),
            SavedItem(
                id = "2",
                title = "Blue Backpack",
                description = "Lost my blue backpack with important documents.",
                badge = "LOST",
                location = "Student Center",
                time = "2 hours ago",
                personName = "John Doe",
                personEmail = "john@example.com",
                personPhone = "+1987654321",
                imageResId = R.drawable.item_placeholder
            ),
            SavedItem(
                id = "3",
                title = "Silver Headphones",
                description = "Found silver headphones near the cafeteria.",
                badge = "FOUND",
                location = "Cafeteria",
                time = "3 hours ago",
                personName = "Sarah",
                personEmail = "sarah@example.com",
                personPhone = "+1122334455",
                imageResId = R.drawable.item_placeholder
            ),
            SavedItem(
                id = "4",
                title = "Car Keys",
                description = "Lost my car keys, very important.",
                badge = "LOST",
                location = "Parking Lot",
                time = "30 minutes ago",
                personName = "Mike",
                personEmail = "mike@example.com",
                personPhone = "+1555666777",
                imageResId = R.drawable.item_placeholder
            ),
            SavedItem(
                id = "5",
                title = "Red Wallet",
                description = "Found a red wallet with cash and ID.",
                badge = "FOUND",
                location = "Library - 2nd floor",
                time = "45 minutes ago",
                personName = "Emma",
                personEmail = "emma@example.com",
                personPhone = "+1888999000",
                imageResId = R.drawable.item_placeholder
            )
        )

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SavedItemsAdapter(this, savedItemsList)
        recyclerView.adapter = adapter
    }
}

