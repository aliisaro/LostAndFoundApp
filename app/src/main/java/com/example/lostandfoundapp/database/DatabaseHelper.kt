package com.example.lostandfoundapp.database

import com.example.lostandfoundapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    suspend fun getUserData(userUid: String): User? {
        return try {
            val documentSnapshot = db.collection("users").document(userUid).get().await()
            documentSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            println("Error fetching user data: ${e.message}")
            null
        }
    }
}