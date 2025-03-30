package com.example.lostandfoundapp.userInterface

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Navigation() {
    val navController = rememberNavController()

    val user = FirebaseAuth.getInstance().currentUser

    // logs in a test user by default
    // delete when register and login screens are done
    if (user == null) {
        // Log in with a test user by default (using FirebaseAuth)
        FirebaseAuth.getInstance().signInWithEmailAndPassword("testuser@test.com", "Test123?")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Logged in successfully
                    Log.d("Login", "Logged in with test user")
                } else {
                    // Handle failed login
                    Log.d("Login", "Failed to log in test user: ${task.exception?.message}")
                }
            }
    }

    NavHost(navController = navController, startDestination = if (user != null) "home" else "login") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("map") {
            MapScreen(navController)
        }
        composable("loggedOutScreen") {
            LoggedOutScreen(navController)
        }
    }
}