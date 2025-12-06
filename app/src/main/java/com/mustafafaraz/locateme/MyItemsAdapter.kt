package com.mustafafaraz.locateme

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyItemsAdapter(
    private val context: Context,
    private var items: MutableList<MyItem>
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

        fun bind(myItem: MyItem) {
            itemBadge.text = myItem.badge

            // Set badge background color based on type
            if (myItem.badge == "LOST") {
                itemBadge.setBackgroundResource(R.drawable.lost_badge)
            } else {
                itemBadge.setBackgroundResource(R.drawable.found_badge)
            }

            itemTitle.text = myItem.title
            itemDescription.text = myItem.description
            itemLocation.text = myItem.location
            itemTime.text = myItem.time
            //viewsAndResponses.text = myItem.viewsAndResponses
            statusBadge.text = myItem.status
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

    fun updateItems(newItems: MutableList<MyItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}

