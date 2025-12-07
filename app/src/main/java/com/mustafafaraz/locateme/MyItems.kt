package com.mustafafaraz.locateme

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.adapter.MyItemsAdapter
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.model.Item
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.launch

class MyItems : AppCompatActivity() {

    private lateinit var tabActive: TextView
    private lateinit var tabResolved: TextView
    private lateinit var tabExpired: TextView
    private lateinit var backButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyItemsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var tokenManager: TokenManager

    private var currentTab = "ACTIVE" // Track current tab (uppercase to match backend)
    private lateinit var allItems: MutableList<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_items)

        tokenManager = TokenManager(this)

        initializeViews()
        setupRecyclerView()
        setupTabListeners()

        // Load items from API
        loadMyItems()
    }

    private fun initializeViews() {
        tabActive = findViewById(R.id.tab_active)
        tabResolved = findViewById(R.id.tab_resolved)
        tabExpired = findViewById(R.id.tab_expired)
        backButton = findViewById(R.id.back_button)
        recyclerView = findViewById(R.id.my_items_recyclerview)
        progressBar = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)

        backButton.setOnClickListener {
            finish()
        }

        // Initialize empty list
        allItems = mutableListOf()
    }

    private fun setupTabListeners() {
        tabActive.setOnClickListener {
            if (currentTab != "ACTIVE") {
                switchTab("ACTIVE")
            }
        }

        tabResolved.setOnClickListener {
            if (currentTab != "RESOLVED") {
                switchTab("RESOLVED")
            }
        }

        tabExpired.setOnClickListener {
            if (currentTab != "EXPIRED") {
                switchTab("EXPIRED")
            }
        }

        // Set active tab as default
        switchTab("ACTIVE")
    }

    private fun switchTab(tab: String) {
        currentTab = tab

        // Reset all tabs
        tabActive.background = null
        tabResolved.background = null
        tabExpired.background = null

        tabActive.setTextColor(ContextCompat.getColor(this, R.color.text_color))
        tabResolved.setTextColor(ContextCompat.getColor(this, R.color.text_color))
        tabExpired.setTextColor(ContextCompat.getColor(this, R.color.text_color))

        // Highlight selected tab
        when (tab) {
            "ACTIVE" -> {
                tabActive.setBackgroundResource(R.drawable.selected_button_login_signup)
                tabActive.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            "RESOLVED" -> {
                tabResolved.setBackgroundResource(R.drawable.selected_button_login_signup)
                tabResolved.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            "EXPIRED" -> {
                tabExpired.setBackgroundResource(R.drawable.selected_button_login_signup)
                tabExpired.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        // Reload items with filter
        loadMyItems()
    }

    private fun loadMyItems() {
        lifecycleScope.launch {
            try {
                // Show loading
                progressBar.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.GONE

                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@MyItems, "Please login", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val authHeader = "Bearer $token"

                Log.d("MyItems", "Loading items with status: $currentTab")

                // Make API call with status filter
                val response = RetrofitClient.apiService.getMyItems(
                    token = authHeader,
                    status = currentTab
                )

                // Hide loading
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.data ?: emptyList()

                    Log.d("MyItems", "Received ${items.size} items")

                    allItems.clear()
                    allItems.addAll(items)

                    if (items.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                        emptyView.text = "No ${currentTab.lowercase()} items"
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateItems(allItems)
                    }
                } else {
                    Toast.makeText(this@MyItems, "Failed to load items", Toast.LENGTH_SHORT).show()
                    emptyView.visibility = View.VISIBLE
                    emptyView.text = "Failed to load items"
                }
            } catch (e: Exception) {
                Log.e("MyItems", "Error loading items", e)
                progressBar.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
                emptyView.text = "Error: ${e.message}"
                Toast.makeText(this@MyItems, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MyItemsAdapter(this, allItems)
        recyclerView.adapter = adapter
    }
}
