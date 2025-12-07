package com.mustafafaraz.locateme

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.adapter.ItemAdapter
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Home : AppCompatActivity() {

    private lateinit var tabAllItems: TextView
    private lateinit var tabLost: TextView
    private lateinit var tabFound: TextView
    private lateinit var tabIndicator: View

    // Category chips
    private lateinit var chipAll: TextView
    private lateinit var chipElectronics: TextView
    private lateinit var chipBags: TextView
    private lateinit var chipKeys: TextView
    private lateinit var chipClothing: TextView
    private lateinit var chipOther: TextView

    // RecyclerView
    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var searchInput: EditText

    // TokenManager
    private lateinit var tokenManager: TokenManager

    // Current filters
    private var currentCategory: String? = null // null means "All Categories"
    private var currentType: String? = null // null means "All Items"
    private var currentSearch: String? = null

    // Search debounce
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tokenManager = TokenManager(this)

        initializeViews()
        initializeTabs()
        initializeCategoryChips()
        setupRecyclerView()
        setupBottomNavigation()
        setupSearch()

        // Load initial data
        loadItems()
    }

    override fun onResume() {
        super.onResume()
        // Reload items when returning from ItemDetails to update save status
        loadItems()
    }

    private fun initializeViews() {
        itemsRecyclerView = findViewById(R.id.items_recycler_view)
        progressBar = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)
        searchInput = findViewById(R.id.search_input)
    }

    private fun initializeTabs() {
        tabAllItems = findViewById(R.id.tab_all_items)
        tabLost = findViewById(R.id.tab_lost)
        tabFound = findViewById(R.id.tab_found)
        tabIndicator = findViewById(R.id.tab_indicator)

        // Set click listeners for tabs
        tabAllItems.setOnClickListener {
            selectTab(0)
            currentType = null
            loadItems()
        }

        tabLost.setOnClickListener {
            selectTab(1)
            currentType = "LOST"
            loadItems()
        }

        tabFound.setOnClickListener {
            selectTab(2)
            currentType = "FOUND"
            loadItems()
        }

        // Wait for layout to be ready, then position indicator
        tabAllItems.post {
            selectTab(0) // Default to "All Items" tab
        }
    }

    private fun initializeCategoryChips() {
        chipAll = findViewById(R.id.chip_all)
        chipElectronics = findViewById(R.id.chip_electronics)
        chipBags = findViewById(R.id.chip_bags)
        chipKeys = findViewById(R.id.chip_keys)
        chipClothing = findViewById(R.id.chip_clothing)
        chipOther = findViewById(R.id.chip_other)

        // Set click listeners
        chipAll.setOnClickListener {
            selectCategory(chipAll, null)
        }
        chipElectronics.setOnClickListener {
            selectCategory(chipElectronics, "ELECTRONICS")
        }
        chipBags.setOnClickListener {
            selectCategory(chipBags, "BAGS")
        }
        chipKeys.setOnClickListener {
            selectCategory(chipKeys, "KEYS")
        }
        chipClothing.setOnClickListener {
            selectCategory(chipClothing, "CLOTHING")
        }
        chipOther.setOnClickListener {
            selectCategory(chipOther, "OTHER")
        }
    }

    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter(this, mutableListOf())
        itemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@Home)
            adapter = itemAdapter
        }
    }

    private fun selectCategory(selectedChip: TextView, category: String?) {
        // Reset all chips to unselected state
        val chips = listOf(chipAll, chipElectronics, chipBags, chipKeys, chipClothing, chipOther)

        for (chip in chips) {
            chip.setBackgroundResource(R.drawable.chip_unselected)
            chip.setTextColor(ContextCompat.getColor(this, R.color.black))
            chip.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        // Set selected chip to black background with white text and bold
        selectedChip.setBackgroundResource(R.drawable.chip_selected)
        selectedChip.setTextColor(ContextCompat.getColor(this, R.color.white))
        selectedChip.setTypeface(null, android.graphics.Typeface.BOLD)

        // Update current category and reload items
        currentCategory = category
        loadItems()
    }

    private fun selectTab(position: Int) {
        // Reset all tabs to normal state
        tabAllItems.setTextColor(ContextCompat.getColor(this, R.color.text_color))
        tabLost.setTextColor(ContextCompat.getColor(this, R.color.text_color))
        tabFound.setTextColor(ContextCompat.getColor(this, R.color.text_color))

        tabAllItems.setTypeface(null, android.graphics.Typeface.NORMAL)
        tabLost.setTypeface(null, android.graphics.Typeface.NORMAL)
        tabFound.setTypeface(null, android.graphics.Typeface.NORMAL)

        // Set selected tab to bold and black
        val selectedTab = when (position) {
            0 -> tabAllItems
            1 -> tabLost
            2 -> tabFound
            else -> tabAllItems
        }

        selectedTab.setTextColor(ContextCompat.getColor(this, R.color.black))
        selectedTab.setTypeface(null, android.graphics.Typeface.BOLD)

        // Animate indicator to the selected tab
        animateIndicator(selectedTab)
    }

    private fun animateIndicator(selectedTab: TextView) {
        val tabWidth = selectedTab.width
        val tabLeft = selectedTab.left

        // Calculate center position
        val indicatorWidth = tabIndicator.layoutParams.width
        val newX = tabLeft + (tabWidth - indicatorWidth) / 2

        // Animate the indicator
        tabIndicator.animate()
            .x(newX.toFloat())
            .setDuration(200)
            .start()
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Cancel previous search job
                searchJob?.cancel()

                // Create new search job with debounce
                searchJob = lifecycleScope.launch {
                    delay(500) // Wait 500ms after user stops typing

                    val query = s.toString().trim()
                    currentSearch = if (query.isEmpty()) null else query

                    Log.d("Home", "Search query: $currentSearch")
                    loadItems()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed
            }
        })
    }

    private fun loadItems() {
        lifecycleScope.launch {
            try {
                // Show loading
                progressBar.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
                itemsRecyclerView.visibility = View.GONE

                // Get token
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@Home, "Please login to view items", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val authHeader = "Bearer $token"

                Log.d("Home", "Loading items: category=$currentCategory, type=$currentType")

                // Make API call with filters
                val response = RetrofitClient.apiService.getItems(
                    token = authHeader,
                    type = currentType,
                    category = currentCategory,
                    search = currentSearch
                )

                // Hide loading
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.data ?: emptyList()

                    Log.d("Home", "Received ${items.size} items")

                    if (items.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                        itemsRecyclerView.visibility = View.GONE
                    } else {
                        emptyView.visibility = View.GONE
                        itemsRecyclerView.visibility = View.VISIBLE
                        itemAdapter.updateItems(items)
                    }
                } else {
                    Toast.makeText(this@Home, "Failed to load items", Toast.LENGTH_SHORT).show()
                    emptyView.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("Home", "Error loading items", e)
                progressBar.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
                Toast.makeText(this@Home, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        // Search navigation (already on home/search page)
        findViewById<View>(R.id.nav_search).setOnClickListener {
            // Already on search/home screen
        }

        // Report navigation
        findViewById<View>(R.id.nav_report).setOnClickListener {
            val intent = Intent(this, Report::class.java)
            startActivity(intent)
        }

        // Profile navigation
        findViewById<View>(R.id.nav_profile).setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.chat_icon).setOnClickListener {
            val intent = Intent(this, ChatList::class.java)
            startActivity(intent)
        }
    }
}