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

    // Add a lost item to Firestore
    suspend fun addItem(
        title: String,
        description: String,
        category: String,
        imageUrl: String,
        latitude: Double,
        longitude: Double
    ) {
        val auth = FirebaseAuth.getInstance()

        // Check if user is authenticated
        val currentUser = auth.currentUser
        if (currentUser == null) {
            throw Exception("User not authenticated")
        }

        // Create new Item object
        val newItem = Item(
            title = title,
            description = description,
            category = category,
            imageUrl = imageUrl,
            location = GeoPoint(latitude, longitude), // GeoPoint for latitude/longitude
            reportedBy = currentUser.uid, // Save user UID as the reporter
            foundBy = null,  // Initially null since the item is lost
            registeredAt = Timestamp.now(),
            foundAt = null,  // Not set yet as the item is still lost
            lost = true // The item is lost when first reported
        )

        try {
            // Save the new item in Firestore under 'items' collection
            db.collection("items").add(newItem).await()
        } catch (e: Exception) {
            throw Exception("Failed to add item: ${e.message}")
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

    // Mark an item as found
    suspend fun markItemAsFound(itemId: String, foundByUserId: String) {
        try {
            // Reference to the item document in Firestore
            val itemRef = db.collection("items").document(itemId)

            // Update the item with the new values
            itemRef.update(
                "lost", false,  // Mark item as found
                "foundAt", Timestamp.now(),  // Set the found time
                "foundBy", foundByUserId  // Set the user who found the item
            ).await()
        } catch (e: Exception) {
            throw Exception("Failed to mark item as found: ${e.message}")
        }
    }
}


