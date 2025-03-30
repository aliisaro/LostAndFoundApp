package com.example.lostandfoundapp.model

import com.google.firebase.firestore.GeoPoint

data class User(
    val email: String = "",
    val username: String = "",
    val profilepicture: String = "",
    val location: GeoPoint? = null, // Firestore GeoPoint for GPS coordinates
)