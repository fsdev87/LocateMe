package com.mustafafaraz.locateme.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeFormatter {

    fun formatTimeAgo(timestamp: String): String {
        return try {
            // Parse the ISO 8601 timestamp from backend (e.g., "2025-12-07T08:30:00.000Z")
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(timestamp.substringBefore("."))

            if (date != null) {
                val now = System.currentTimeMillis()
                val diff = now - date.time

                val seconds = diff / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24
                val weeks = days / 7
                val months = days / 30
                val years = days / 365

                when {
                    years > 0 -> "$years year${if (years > 1) "s" else ""} ago"
                    months > 0 -> "$months month${if (months > 1) "s" else ""} ago"
                    weeks > 0 -> "$weeks week${if (weeks > 1) "s" else ""} ago"
                    days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
                    hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
                    minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
                    else -> "Just now"
                }
            } else {
                timestamp.substringBefore("T")
            }
        } catch (e: Exception) {
            // Fallback: just show the date part
            timestamp.substringBefore("T")
        }
    }
}

