package com.example.lostandfoundapp.userInterface

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(text = "Welcome to Lost & Found App!")

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.navigate("map") }) {
            Text("Go to Map")
        }

        // Log out button
        Button(onClick = {
            // Log out the user from Firebase
            FirebaseAuth.getInstance().signOut()

            // Navigate to the LoggedOutScreen
            navController.navigate("loggedOutScreen")
        }) {
            Text("Log out")
        }
    }
}