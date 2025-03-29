package com.example.lostandfoundapp.model

import com.google.firebase.firestore.GeoPoint

data class User(
    val username: String = "",
    val email: String = "",
    val location: GeoPoint? = null, // Firestore GeoPoint for GPS coordinates
)