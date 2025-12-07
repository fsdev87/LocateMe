package com.mustafafaraz.locateme

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mustafafaraz.locateme.adapter.ImageGalleryAdapter
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.model.Item
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.launch

class ItemDetails : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var mainImageView: ImageView
    private lateinit var imagesRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var currentItem: Item? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        tokenManager = TokenManager(this)

        // Initialize views
        initializeViews()

        // Get item ID from intent
        val itemId = intent.getIntExtra("item_id", -1)
        if (itemId != -1) {
            loadItemDetails(itemId)
        } else {
            Toast.makeText(this, "Invalid item", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initializeViews() {
        mainImageView = findViewById(R.id.item_image)
        imagesRecyclerView = findViewById(R.id.images_recycler_view)
        progressBar = findViewById(R.id.progress_bar)

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }
    }

    private fun loadItemDetails(itemId: Int) {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE

                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@ItemDetails, "Please login", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val authHeader = "Bearer $token"
                val response = RetrofitClient.apiService.getItemById(authHeader, itemId)

                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    currentItem = response.body()?.data
                    currentItem?.let { displayItemDetails(it) }
                } else {
                    Toast.makeText(this@ItemDetails, "Failed to load item details", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("ItemDetails", "Error loading item", e)
                progressBar.visibility = View.GONE
                Toast.makeText(this@ItemDetails, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayItemDetails(item: Item) {
        // Set badge
        findViewById<TextView>(R.id.item_badge).apply {
            text = item.type
            setBackgroundResource(
                if (item.type == "LOST") R.drawable.lost_badge else R.drawable.found_badge
            )
        }

        // Set basic info
        findViewById<TextView>(R.id.item_title).text = item.title
        findViewById<TextView>(R.id.item_description).text = item.description
        findViewById<TextView>(R.id.item_location).text = item.location
        findViewById<TextView>(R.id.item_time).text = formatTime(item.createdAt)
        findViewById<TextView>(R.id.item_person).text = item.userName

        // Set reporter details
        findViewById<TextView>(R.id.reporter_full_name).text = item.userName
        findViewById<TextView>(R.id.reporter_student_id).text = item.userStudentId ?: "N/A"
        findViewById<TextView>(R.id.reporter_department).text = item.userDepartment ?: "N/A"
        findViewById<TextView>(R.id.reporter_batch).text = item.userBatch ?: "N/A"
        findViewById<TextView>(R.id.reporter_section).text = item.userSection ?: "N/A"

        // Load images
        setupImageGallery(item.imageUrls)

        // Setup action buttons
        setupActionButtons(item)
    }

    private fun setupImageGallery(imageUrls: List<String>) {
        if (imageUrls.isEmpty()) {
            mainImageView.setImageResource(R.drawable.item_placeholder)
            imagesRecyclerView.visibility = View.GONE
            return
        }

        // Load first image in main view
        Glide.with(this)
            .load(imageUrls[0])
            .placeholder(R.drawable.item_placeholder)
            .error(R.drawable.item_placeholder)
            .centerCrop()
            .into(mainImageView)

        // Setup image gallery if multiple images
        if (imageUrls.size > 1) {
            imagesRecyclerView.visibility = View.VISIBLE
            val adapter = ImageGalleryAdapter(imageUrls) { imageUrl ->
                // When thumbnail clicked, load in main view
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.item_placeholder)
                    .error(R.drawable.item_placeholder)
                    .centerCrop()
                    .into(mainImageView)
            }
            imagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            imagesRecyclerView.adapter = adapter
        } else {
            imagesRecyclerView.visibility = View.GONE
        }
    }

    private fun setupActionButtons(item: Item) {
        // Message button
        findViewById<LinearLayout>(R.id.message_button).setOnClickListener {
            Toast.makeText(this, "Chat feature coming soon", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to chat with this user
        }

        // Email button
        findViewById<LinearLayout>(R.id.email_button).setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(item.userEmail))
                putExtra(Intent.EXTRA_SUBJECT, "Regarding: ${item.title}")
            }
            try {
                startActivity(Intent.createChooser(intent, "Send Email"))
            } catch (e: Exception) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }

        // Favorite button
        findViewById<ImageView>(R.id.favorite_button).setOnClickListener {
            Toast.makeText(this, "Favorite feature coming soon", Toast.LENGTH_SHORT).show()
        }

        // Share button
        findViewById<ImageView>(R.id.share_button).setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "${item.type}: ${item.title}\n${item.description}\nLocation: ${item.location}")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Item"))
        }

        // Bookmark button
        findViewById<ImageView>(R.id.bookmark_button).setOnClickListener {
            Toast.makeText(this, "Bookmark feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatTime(timestamp: String): String {
        return try {
            timestamp.substringBefore("T")
        } catch (e: Exception) {
            timestamp
        }
    }
}
