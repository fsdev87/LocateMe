package com.mustafafaraz.locateme

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.mustafafaraz.locateme.data.model.ItemPhoto

class PhotosGridAdapter(
    private val context: Context,
    private val photos: MutableList<ItemPhoto>,
    private val onDeleteClick: (Int) -> Unit = {},
    private val onAddPhotoClick: () -> Unit = {}
) : RecyclerView.Adapter<PhotosGridAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {

        private val photoImage: ImageView = itemView.findViewById(R.id.photo_image)
        private val deleteButton: ImageView = itemView.findViewById(R.id.delete_photo_button)
        private val addPhotoButton: FrameLayout = itemView.findViewById(R.id.add_photo_button)

        fun bind(photo: ItemPhoto?, position: Int) {
            // If this is the add photo button (last item)
            if (photo == null) {
                photoImage.visibility = android.view.View.GONE
                deleteButton.visibility = android.view.View.GONE
                addPhotoButton.visibility = android.view.View.VISIBLE

                addPhotoButton.setOnClickListener {
                    onAddPhotoClick()
                }
            } else {
                // Regular photo item
                photoImage.visibility = android.view.View.VISIBLE
                deleteButton.visibility = android.view.View.VISIBLE
                addPhotoButton.visibility = android.view.View.GONE

                // Load image from bitmap if available, otherwise from URI
                if (photo.bitmap != null) {
                    photoImage.setImageBitmap(photo.bitmap)
                } else {
                    try {
                        photoImage.setImageURI(android.net.Uri.parse(photo.imageUri))
                    } catch (e: Exception) {
                        photoImage.setImageResource(R.drawable.item_placeholder)
                    }
                }

                deleteButton.setOnClickListener {
                    onDeleteClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhotoViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.photo_grid_item, parent, false)
        return PhotoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        // If this is the last position and we have less than 5 photos, show add button
        if (position == photos.size && photos.size < 5) {
            holder.bind(null, position)
        } else if (position < photos.size) {
            holder.bind(photos[position], position)
        }
    }

    override fun getItemCount(): Int {
        // Show add photo button if less than 5 photos
        return if (photos.size < 5) photos.size + 1 else photos.size
    }

    fun addPhoto(photo: ItemPhoto) {
        if (photos.size < 5) {
            photos.add(photo)
            notifyDataSetChanged()
        }
    }

    fun removePhoto(position: Int) {
        if (position < photos.size) {
            photos.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun getPhotos(): List<ItemPhoto> {
        return photos.toList()
    }
}

