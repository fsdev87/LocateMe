package com.mustafafaraz.locateme.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mustafafaraz.locateme.ItemDetails
import com.mustafafaraz.locateme.R
import com.mustafafaraz.locateme.data.model.Item
import com.mustafafaraz.locateme.utils.TimeFormatter

class ItemAdapter(
    private val context: Context,
    private var items: MutableList<Item>
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        private val itemBadge: TextView = itemView.findViewById(R.id.item_badge)
        private val itemTitle: TextView = itemView.findViewById(R.id.item_title)
        private val itemDescription: TextView = itemView.findViewById(R.id.item_description)
        private val itemLocation: TextView = itemView.findViewById(R.id.item_location)
        private val itemTime: TextView = itemView.findViewById(R.id.item_time)
        private val itemPerson: TextView = itemView.findViewById(R.id.item_person)
        private val itemContainer: LinearLayout = itemView.findViewById(R.id.item_container)

        fun bind(item: Item) {
            // Load first image or placeholder
            if (item.imageUrls.isNotEmpty()) {
                Glide.with(context)
                    .load(item.imageUrls[0])
                    .placeholder(R.drawable.item_placeholder)
                    .error(R.drawable.item_placeholder)
                    .centerCrop()
                    .into(itemImage)
            } else {
                itemImage.setImageResource(R.drawable.item_placeholder)
            }

            // Set badge
            itemBadge.text = item.type
            if (item.type == "LOST") {
                itemBadge.setBackgroundResource(R.drawable.lost_badge)
            } else {
                itemBadge.setBackgroundResource(R.drawable.found_badge)
            }

            // Set item details
            itemTitle.text = item.title
            itemDescription.text = item.description
            itemLocation.text = item.location
            itemTime.text = TimeFormatter.formatTimeAgo(item.createdAt)
            itemPerson.text = item.userName

            // Click listener to open item details
            itemContainer.setOnClickListener {
                val intent = Intent(context, ItemDetails::class.java).apply {
                    putExtra("item_id", item.id)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Item>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
