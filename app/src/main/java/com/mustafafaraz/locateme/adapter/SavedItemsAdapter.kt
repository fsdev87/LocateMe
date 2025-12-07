package com.mustafafaraz.locateme.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.ItemDetails
import com.mustafafaraz.locateme.R
import com.mustafafaraz.locateme.SavedItem

class SavedItemsAdapter(
    private val context: Context,
    private val savedItems: List<SavedItem>
) : RecyclerView.Adapter<SavedItemsAdapter.SavedItemViewHolder>() {

    inner class SavedItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        private val itemBadge: TextView = itemView.findViewById(R.id.item_badge)
        private val itemTitle: TextView = itemView.findViewById(R.id.item_title)
        private val itemDescription: TextView = itemView.findViewById(R.id.item_description)
        private val itemLocation: TextView = itemView.findViewById(R.id.item_location)
        private val itemTime: TextView = itemView.findViewById(R.id.item_time)
        private val itemPerson: TextView = itemView.findViewById(R.id.item_person)

        fun bind(savedItem: SavedItem) {
            itemImage.setImageResource(savedItem.imageResId)
            itemBadge.text = savedItem.badge

            if (savedItem.badge == "LOST") {
                itemBadge.setBackgroundResource(R.drawable.lost_badge)
            } else {
                itemBadge.setBackgroundResource(R.drawable.found_badge)
            }

            itemTitle.text = savedItem.title
            itemDescription.text = savedItem.description
            itemLocation.text = savedItem.location
            itemTime.text = savedItem.time
            itemPerson.text = savedItem.personName

            itemView.setOnClickListener {
                val intent = Intent(context, ItemDetails::class.java).apply {
                    putExtra("item_title", savedItem.title)
                    putExtra("item_description", savedItem.description)
                    putExtra("item_badge", savedItem.badge)
                    putExtra("location", savedItem.location)
                    putExtra("time", savedItem.time)
                    putExtra("person_name", savedItem.personName)
                    putExtra("contact_email", savedItem.personEmail)
                    putExtra("contact_phone", savedItem.personPhone)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SavedItemViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.saved_items_row, parent, false)
        return SavedItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SavedItemViewHolder, position: Int) {
        holder.bind(savedItems[position])
    }

    override fun getItemCount(): Int = savedItems.size
}