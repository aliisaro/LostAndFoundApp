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
        val currentUser = auth.currentUser
        if (currentUser == null) {
            throw Exception("User not authenticated")
        }

        try {
            // Create a new document reference to get the generated ID
            val newDocRef = db.collection("items").document()

            // Create the Item with the ID included
            val newItem = Item(
                id = newDocRef.id,  // Assign the generated document ID
                title = title,
                description = description,
                category = category,
                imageUrl = imageUrl,
                location = GeoPoint(latitude, longitude),
                reportedBy = currentUser.uid,
                foundBy = null,
                registeredAt = Timestamp.now(),
                foundAt = null,
                lost = true
            )

            // Set the document using the newItem with ID included
            newDocRef.set(newItem).await()
        } catch (e: Exception) {
            throw Exception("Failed to add item: ${e.message}")
        }
    }

    // Get only lost items from Firestore
    suspend fun getLostItems(): List<Item> {
        return try {
            // Fetch documents where 'lost' == true
            val querySnapshot = db.collection("items")
                .whereEqualTo("lost", true) // Only get lost items
                .get()
                .await()

            // Convert Firestore documents to a list of Item objects
            querySnapshot.documents.mapNotNull { it.toObject(Item::class.java) }
        } catch (e: Exception) {
            println("Error fetching items: ${e.message}")
            emptyList()
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


