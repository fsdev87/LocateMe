package com.mustafafaraz.locateme.data.model

import android.graphics.Bitmap

data class ItemPhoto(
    val id: String,
    val imageUri: String,
    val bitmap: Bitmap? = null  // remove if not using
)