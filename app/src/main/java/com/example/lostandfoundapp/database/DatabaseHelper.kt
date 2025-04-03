package com.example.lostandfoundapp.database

import com.example.lostandfoundapp.model.Item
import com.example.lostandfoundapp.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

class DatabaseHelper {

    private val db = FirebaseFirestore.getInstance()

    // Register user and save additional data in Firestore
    suspend fun registerUser(email: String, password: String, username: String, onResult: (Boolean, String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        try {
            // Create user in Firebase Authentication
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            // Get user UID from Firebase Authentication
            val userUid = result.user?.uid ?: return onResult(false, "User UID not found")

            // Create a user object
            val user = User(
                email = email,
                username = username,
                profilepicture = "", // empty for now
            )

            // Save user data in Firestore
            db.collection("users").document(userUid).set(user).await()

            // Registration successful
            onResult(true, null)
        } catch (e: Exception) {
            onResult(false, e.message) // Handle errors during registration
        }
    }

    // Get user data from Firestore
    suspend fun getUserData(userUid: String): User? {
        return try {
            val documentSnapshot = db.collection("users").document(userUid).get().await()
            documentSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            println("Error fetching user data: ${e.message}")
            null
        }
    }

    // Add a lost item to Firestore
    suspend fun addItem(title: String, description: String, category: String, imageUrl: String, latitude: Double, longitude: Double, onResult: (Boolean, String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        // Check if user is authenticated
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(false, "User not authenticated")
            return
        }

        // Create new Item object
        val newItem = Item(
            title = title,
            description = description,
            category = category,
            imageUrl = imageUrl,
            location = GeoPoint(latitude, longitude), // GeoPoint for latitude/longitude
            reportedBy = currentUser.uid, // Save user UID as the reporter
            registeredAt = Timestamp.now(),
            lost = true
        )

        try {
            // Save the new item in Firestore under 'items' collection
            db.collection("items").add(newItem).await()
            onResult(true, null) // Successfully added the item
        } catch (e: Exception) {
            onResult(false, e.message) // Handle errors
        }
    }

    // Get all lost items from Firestore
    suspend fun getItems(): List<Item> {
        return try {
            // Fetch all documents from the 'items' collection
            val querySnapshot = db.collection("items").get().await()
            // Convert Firestore documents to a list of Item objects
            querySnapshot.documents.mapNotNull { it.toObject(Item::class.java) }
        } catch (e: Exception) {
            println("Error fetching items: ${e.message}")
            emptyList() // Return an empty list in case of error
        }
    }
}


