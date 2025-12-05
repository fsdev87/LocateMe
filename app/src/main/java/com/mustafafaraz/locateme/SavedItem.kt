package com.mustafafaraz.locateme

data class SavedItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val badge: String = "",
    val location: String = "",
    val time: String = "",
    val personName: String = "",
    val personEmail: String = "",
    val personPhone: String = "",
    val imageResId: Int = R.drawable.item_placeholder
)

