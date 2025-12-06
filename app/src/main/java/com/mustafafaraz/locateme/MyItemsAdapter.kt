package com.mustafafaraz.locateme

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyItemsAdapter(
    private val context: Context,
    private var items: MutableList<Item>
) : RecyclerView.Adapter<MyItemsAdapter.MyItemViewHolder>() {

    inner class MyItemViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {

        private val itemBadge: TextView = itemView.findViewById(R.id.item_badge)
        private val itemTitle: TextView = itemView.findViewById(R.id.item_title)
        private val itemDescription: TextView = itemView.findViewById(R.id.item_description)
        private val itemLocation: TextView = itemView.findViewById(R.id.item_location)
        private val itemTime: TextView = itemView.findViewById(R.id.item_time)
        //private val viewsAndResponses: TextView = itemView.findViewById(R.id.views_and_responses)
        private val statusBadge: TextView = itemView.findViewById(R.id.status_badge)
        private val editIcon: ImageView = itemView.findViewById(R.id.edit_icon)
        private val itemContainer: LinearLayout = itemView.findViewById(R.id.item_container)

        fun bind(myItem: Item) {
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
            itemTime.text = myItem.createdAt
            //viewsAndResponses.text = myItem.viewsAndResponses
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
                    putExtra("item_title", myItem.title)
                    putExtra("item_description", myItem.description)
                    putExtra("item_badge", myItem.type)
                    putExtra("location", myItem.location)
                    putExtra("time", myItem.createdAt)
                    putExtra("person_name", "You")
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

