package com.example.lostandfoundapp.userInterface

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, // Keskitetään elementit
        verticalArrangement = Arrangement.spacedBy(16.dp) // Välimatkat elementtien välillä
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Tervetuloteksti
        Text(
            text = "Welcome to the Lost & Found App!",
            style = MaterialTheme.typography.headlineMedium, // Käytetään Materiaali-tyylitystä
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Go to map screen
        Button(
            onClick = { navController.navigate("map") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Go to Map", color = MaterialTheme.colorScheme.onPrimary)
        }

        // Go to search screen
        Button(
            onClick = { navController.navigate("searchItem") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Search for items", color = MaterialTheme.colorScheme.onSecondary)
        }

        // Go to report item screen
        Button(
            onClick = { navController.navigate("reportItem") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Report item", color = MaterialTheme.colorScheme.onSecondary)
        }

        Button(
            onClick = { navController.navigate("searchItemEdit") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Edit My Reports", color = MaterialTheme.colorScheme.onSecondary)
        }

        // Go to take a picture with the camera
        Button(
            onClick = { navController.navigate("camera") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("Take picture", color = MaterialTheme.colorScheme.onTertiary)
        }

        // Go to Profile screen
        Button(
            onClick = { navController.navigate("profile") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("Go to Profile", color = MaterialTheme.colorScheme.onTertiary)
        }


        // Go to Statistics screen
        Button(
            onClick = { navController.navigate("statistics") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("Statistics", color = MaterialTheme.colorScheme.onTertiary)
        }


        // Kirjaudu ulos -painike
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("loggedOutScreen")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Log out", color = MaterialTheme.colorScheme.onError)
        }
    }
}
