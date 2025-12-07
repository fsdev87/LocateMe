package com.mustafafaraz.locateme.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.R

class PhotoAdapter(
    private val photos: MutableList<Uri>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.photo_image)
        val removeButton: ImageView = itemView.findViewById(R.id.remove_photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoUri = photos[position]

        // Load image from URI
        holder.imageView.setImageURI(photoUri)

        // Remove button click
        holder.removeButton.setOnClickListener {
            onRemoveClick(position)
        }
    }

    override fun getItemCount(): Int = photos.size

    fun addPhoto(uri: Uri) {
        if (photos.size < 5) {
            photos.add(uri)
            notifyItemInserted(photos.size - 1)
        }
    }

    fun removePhoto(position: Int) {
        if (position < photos.size) {
            photos.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, photos.size)
        }
    }

    fun getPhotos(): List<Uri> = photos
}