package com.mustafafaraz.locateme

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.adapter.MyItemsAdapter

class MyItems : AppCompatActivity() {

    private lateinit var tabActive: TextView
    private lateinit var tabResolved: TextView
    private lateinit var tabExpired: TextView
    private lateinit var backButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyItemsAdapter

    private var currentTab = "active" // Track current tab
    private lateinit var allItems: MutableList<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_items)

        initializeViews()
        setupRecyclerView()
        setupTabListeners()

    }

    private fun initializeViews() {
        tabActive = findViewById(R.id.tab_active)
        tabResolved = findViewById(R.id.tab_resolved)
        tabExpired = findViewById(R.id.tab_expired)
        backButton = findViewById(R.id.back_button)
        recyclerView = findViewById(R.id.my_items_recyclerview)

        backButton.setOnClickListener {
            finish()
        }

        // Initialize all items with sample data
        allItems = mutableListOf(
            Item("1", "Dark Chocolate Cake", "Found a chocolate cake in the library. Contact me asap.", "LOST", "Library - 3rd floor", "2 hours ago", "active"),
            Item("2", "Blue Backpack", "Lost my blue backpack with important documents.", "LOST", "Student Center", "1 hour ago", "active"),
            Item("3", "Silver Headphones", "Found silver headphones near the cafeteria.", "FOUND", "Cafeteria", "30 minutes ago", "resolved"),
            Item("4", "Car Keys", "Lost my car keys, very important.", "LOST", "Parking Lot", "3 hours ago", "expired"),
            Item("5", "Red Wallet", "Found a red wallet with cash and ID.", "FOUND", "Library - 2nd floor", "45 minutes ago", "active")
        )
    }

    private fun setupTabListeners() {
        tabActive.setOnClickListener {
            if (currentTab != "active") {
                switchTab("active")
            }
        }

        tabResolved.setOnClickListener {
            if (currentTab != "resolved") {
                switchTab("resolved")
            }
        }

        tabExpired.setOnClickListener {
            if (currentTab != "expired") {
                switchTab("expired")
            }
        }

        // Set active tab as default
        switchTab("active")
    }

    private fun switchTab(tab: String) {
        currentTab = tab

        // Reset all tabs
        tabActive.background = null
        tabResolved.background = null
        tabExpired.background = null

        // Highlight selected tab
        when (tab) {
            "active" -> {
                tabActive.setBackgroundResource(R.drawable.selected_button_login_signup)
                tabActive.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            "resolved" -> {
                tabResolved.setBackgroundResource(R.drawable.selected_button_login_signup)
                tabResolved.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            "expired" -> {
                tabExpired.setBackgroundResource(R.drawable.selected_button_login_signup)
                tabExpired.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        // Filter items and update adapter
        filterAndUpdateItems(tab)
    }

    private fun filterAndUpdateItems(tab: String) {
        val filteredItems = allItems.filter { it.status == tab }.toMutableList()
        adapter.updateItems(filteredItems)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MyItemsAdapter(this, allItems)
        recyclerView.adapter = adapter

        // Load active items by default
        filterAndUpdateItems("active")
    }
}

