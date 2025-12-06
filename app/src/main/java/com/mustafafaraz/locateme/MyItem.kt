package com.mustafafaraz.locateme

data class MyItem(
    val id: String,
    val title: String,
    val description: String,
    val badge: String, // "FOUND" or "LOST"
    val location: String,
    val time: String,
    //val viewsAndResponses: String,
    val status: String // "active", "resolved", "expired"
)

