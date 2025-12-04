package com.mustafafaraz.locateme

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initializeTabs()
        initializeCategoryChips()
        setupBottomNavigation()
        setupItemClickListeners()
    }

    private fun initializeTabs() {
        tabAllItems = findViewById(R.id.tab_all_items)
        tabLost = findViewById(R.id.tab_lost)
        tabFound = findViewById(R.id.tab_found)
        tabIndicator = findViewById(R.id.tab_indicator)

        // Set click listeners for tabs
        tabAllItems.setOnClickListener {
            selectTab(0)
        }

        tabLost.setOnClickListener {
            selectTab(1)
        }

        tabFound.setOnClickListener {
            selectTab(2)
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
        chipAll.setOnClickListener { selectCategory(chipAll) }
        chipElectronics.setOnClickListener { selectCategory(chipElectronics) }
        chipBags.setOnClickListener { selectCategory(chipBags) }
        chipKeys.setOnClickListener { selectCategory(chipKeys) }
        chipClothing.setOnClickListener { selectCategory(chipClothing) }
        chipOther.setOnClickListener { selectCategory(chipOther) }
    }

    private fun selectCategory(selectedChip: TextView) {
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

        // TODO: Filter items based on selected category
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

        // TODO: Filter items based on selected tab
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
        }
    }

    private fun setupItemClickListeners() {
        // Item 1: FOUND item - Dark Chocolate Cake
        findViewById<LinearLayout>(R.id.item_1_found).setOnClickListener {
            val intent = Intent(this, ItemDetails::class.java).apply {
                putExtra("item_title", "Dark Chocolate Cake")
                putExtra("item_description", "Found a chocolate cake in the library. Contact me asap.")
                putExtra("item_badge", "FOUND")
                putExtra("location", "Library - 3rd floor")
                putExtra("time", "1 hour ago")
                putExtra("person_name", "Abigail")
                putExtra("contact_email", "abigail@example.com")
                putExtra("contact_phone", "+1234567890")
            }
            startActivity(intent)
        }

        // Item 2: LOST item - Dark Chocolate Cake
        findViewById<LinearLayout>(R.id.item_2_lost).setOnClickListener {
            val intent = Intent(this, ItemDetails::class.java).apply {
                putExtra("item_title", "Dark Chocolate Cake")
                putExtra("item_description", "Lost my cake in the library. Please find it.")
                putExtra("item_badge", "LOST")
                putExtra("location", "Library - 3rd floor")
                putExtra("time", "2 hours ago")
                putExtra("person_name", "John A")
                putExtra("contact_email", "john@example.com")
                putExtra("contact_phone", "+1987654321")
            }
            startActivity(intent)
        }
    }
}