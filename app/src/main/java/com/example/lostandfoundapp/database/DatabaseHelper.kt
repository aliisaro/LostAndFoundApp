package com.example.lostandfoundapp.database

import com.example.lostandfoundapp.model.User
import com.example.lostandfoundapp.model.Item
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DatabaseHelper {
    private val db = FirebaseFirestore.getInstance()

    suspend fun addUser(user: User): String? {
        return try {
            val documentRef = db.collection("users").add(user).await()
            documentRef.id // Returns Firestore-generated ID
        } catch (e: Exception) {
            println("Error adding user: ${e.message}")
            null
        }
    }

    suspend fun addItem(item: Item): String? {
        return try {
            val documentRef = db.collection("items").add(item).await()
            documentRef.id // Returns Firestore-generated ID
        } catch (e: Exception) {
            println("Error adding item: ${e.message}")
            null
        }
    }
}