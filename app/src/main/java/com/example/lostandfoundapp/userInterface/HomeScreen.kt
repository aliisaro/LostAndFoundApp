package com.example.lostandfoundapp.userInterface

import android.util.Log
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
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Welcome to Lost & Found App!")

        Spacer(modifier = Modifier.height(20.dp))

        // Go to map screen
        Button(onClick = { navController.navigate("map") }) {
            Text("Go to Map")
        }

        // Go to report item screen
        Button(onClick = { navController.navigate("reportItem") }) {
            Text("Report item")
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