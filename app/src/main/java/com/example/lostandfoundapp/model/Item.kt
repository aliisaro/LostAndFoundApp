package com.example.lostandfoundapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Item(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val location: GeoPoint? = null, // Firestore GeoPoint for GPS coordinates
    val reportedBy: String = "", // ID of the user who reported it
    val foundBy: String? = null, // ID of the user who found it/ is the owner
    val registeredAt: Timestamp = Timestamp.now(), // Time when item was reported
    val foundAt: Timestamp? = null, // Time when item was found
    val lost: Boolean = true // True if item is still lost, false if found
)