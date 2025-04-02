package com.example.lostandfoundapp.userInterface

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {
    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp)
    )
    {
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Login screen not done yet")

        Spacer(modifier = Modifier.height(20.dp))

        Spacer(modifier = Modifier.height(20.dp))

        // Temporary login button (tämän voi poistaa kun login form on tehty)
        Button(onClick = {
            // Log in test user
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

            // Navigate to the home screen when logged in
            navController.navigate("home")
        }) {
            Text("Login with test user")
        }

        Button(onClick = { navController.navigate("register") }) {
            Text("Go to Register page")
        }
    }
}