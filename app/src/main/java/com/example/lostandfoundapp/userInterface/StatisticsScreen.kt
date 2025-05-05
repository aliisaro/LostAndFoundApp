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
import androidx.compose.ui.res.stringResource
import com.example.lostandfoundapp.R

@Composable
fun StatisticsScreen(navController: NavController) {
    // State to hold statistics data and potential error message
    var stats by remember { mutableStateOf<Map<String, Int>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch statistics data when the screen is first composed
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
        // Screen title
        Text(
            text = stringResource(R.string.statistics),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Handle loading, error, or display of statistics
        when {
            error != null -> {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            stats == null -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            else -> {
                // Total items reported
                StatCard(
                    title = stringResource(R.string.total_items_reported),
                    value = stats!!["total"]
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Lost items
                StatCard(
                    title = stringResource(R.string.items_lost),
                    value = stats!!["lost"],
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Found items
                StatCard(
                    title = stringResource(R.string.items_found),
                    value = stats!!["found"],
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Back to home button
                Button(
                    onClick = { navController.navigate("home") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.go_back))
                }
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
            // Title of the stat
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Value or fallback text
            Text(
                text = value?.toString() ?: "N/A",
                style = MaterialTheme.typography.headlineMedium.copy(color = color)
            )
        }
    }
}
