package com.example.lostandfoundapp.userInterface

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lostandfoundapp.database.DatabaseHelper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun StatisticsScreen(navController: NavController) {
    var stats by remember { mutableStateOf<Map<String, Int>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Load stats when screen is shown
    LaunchedEffect(Unit) {
        try {
            val result = DatabaseHelper().getItemStatistics()
            stats = result
        } catch (e: Exception) {
            error = e.message
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (error != null) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
        } else if (stats == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            // Displaying stats with simple cards and spacing
            StatCard(title = "Total Items reported", value = stats!!["total"])
            Spacer(modifier = Modifier.height(12.dp))

            StatCard(title = "Items Lost", value = stats!!["lost"], color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))

            StatCard(title = "Items Found", value = stats!!["found"], color = MaterialTheme.colorScheme.secondary)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go back")
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: Int?, color: Color = MaterialTheme.colorScheme.primary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value?.toString() ?: "N/A",
                style = MaterialTheme.typography.headlineMedium.copy(color = color)
            )
        }
    }
}