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
import com.mustafafaraz.locateme.EditItem
import com.mustafafaraz.locateme.ItemDetails
import com.mustafafaraz.locateme.R
import com.mustafafaraz.locateme.data.model.Item
import com.mustafafaraz.locateme.utils.TimeFormatter

class MyItemsAdapter(
    private val context: Context,
    private var items: MutableList<Item>
) : RecyclerView.Adapter<MyItemsAdapter.MyItemViewHolder>() {

    inner class MyItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        private val itemBadge: TextView = itemView.findViewById(R.id.item_badge)
        private val itemTitle: TextView = itemView.findViewById(R.id.item_title)
        private val itemDescription: TextView = itemView.findViewById(R.id.item_description)
        private val itemLocation: TextView = itemView.findViewById(R.id.item_location)
        private val itemTime: TextView = itemView.findViewById(R.id.item_time)
        private val itemPerson: TextView = itemView.findViewById(R.id.item_person)
        private val statusBadge: TextView = itemView.findViewById(R.id.status_badge)
        private val editIcon: ImageView = itemView.findViewById(R.id.edit_icon)
        private val itemContainer: LinearLayout = itemView.findViewById(R.id.item_container)

        fun bind(myItem: Item) {
            // Load first image or placeholder using Glide
            if (myItem.imageUrls.isNotEmpty()) {
                Glide.with(context)
                    .load(myItem.imageUrls[0])
                    .placeholder(R.drawable.item_placeholder)
                    .error(R.drawable.item_placeholder)
                    .centerCrop()
                    .into(itemImage)
            } else {
                itemImage.setImageResource(R.drawable.item_placeholder)
            }

            itemBadge.text = myItem.type

            // Set badge background color based on type
            if (myItem.type == "LOST") {
                itemBadge.setBackgroundResource(R.drawable.lost_badge)
            } else {
                itemBadge.setBackgroundResource(R.drawable.found_badge)
            }

            itemTitle.text = myItem.title
            itemDescription.text = myItem.description
            itemLocation.text = myItem.location
            itemTime.text = TimeFormatter.formatTimeAgo(myItem.createdAt)
            itemPerson.text = myItem.userName
            statusBadge.text = myItem.status

            editIcon.setOnClickListener {
                val intent = Intent(context, EditItem::class.java).apply {
                    putExtra("item_id", myItem.id)
                    putExtra("item_title", myItem.title)
                    putExtra("item_description", myItem.description)
                    putExtra("item_badge", myItem.type)
                    putExtra("item_location", myItem.location)
                    putExtra("item_status", myItem.status)
                }
                context.startActivity(intent)
            }

            itemContainer.setOnClickListener {
                val intent = Intent(context, ItemDetails::class.java).apply {
                    putExtra("item_id", myItem.id)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyItemViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.my_items_row, parent, false)
        return MyItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyItemViewHolder, position: Int) {
        holder.bind(items[position])

    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: MutableList<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}