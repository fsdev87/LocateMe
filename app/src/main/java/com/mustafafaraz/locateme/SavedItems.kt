package com.mustafafaraz.locateme

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.adapter.ItemAdapter
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.launch

class SavedItems : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private lateinit var tokenManager: TokenManager
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_items)

        tokenManager = TokenManager(this)

        recyclerView = findViewById(R.id.saved_items_recyclerview)
        progressBar = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)
        val backButton = findViewById<ImageView>(R.id.back_button)

        backButton.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        loadSavedItems()
    }

    override fun onResume() {
        super.onResume()
        // Reload saved items when returning from ItemDetails
        loadSavedItems()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItemAdapter(this, mutableListOf())
        recyclerView.adapter = adapter
    }

    private fun loadSavedItems() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                emptyView.visibility = View.GONE

                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@SavedItems, "Please login", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val authHeader = "Bearer $token"
                val response = RetrofitClient.apiService.getSavedItems(authHeader)

                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.data ?: emptyList()

                    if (items.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                        emptyView.text = "No saved items yet"
                    } else {
                        emptyView.visibility = View.GONE
                        adapter.updateItems(items)
                    }
                } else {
                    Toast.makeText(this@SavedItems, "Failed to load saved items", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SavedItems", "Error loading saved items", e)
                progressBar.visibility = View.GONE
                Toast.makeText(this@SavedItems, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
