package com.example.lostandfoundapp.userInterface

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lostandfoundapp.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val userEmail = user?.email ?: ""

    // List of emails that are allowed admin access
    val adminEmails = listOf(
        "admin1@example.com"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Welcome message
        Text(
            text = stringResource(R.string.welcome_message),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Button to go to map screen
        Button(
            onClick = { navController.navigate("map") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.open_map), color = MaterialTheme.colorScheme.onPrimary)
        }

        // Button to go to item search screen
        Button(
            onClick = { navController.navigate("searchItem") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                stringResource(R.string.search_for_items),
                color = MaterialTheme.colorScheme.onSecondary
            )
        }

        // Button to go to report item screen
        Button(
            onClick = { navController.navigate("reportItem") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(
                stringResource(R.string.report_item),
                color = MaterialTheme.colorScheme.onSecondary
            )
        }

        // Button to go to edit reports screen
        Button(
            onClick = { navController.navigate("searchItemEdit") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(
                stringResource(R.string.edit_my_reports),
                color = MaterialTheme.colorScheme.onSecondary
            )
        }

        // Button to go to Profile screen
        Button(
            onClick = { navController.navigate("profile") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text(
                stringResource(R.string.go_to_profile),
                color = MaterialTheme.colorScheme.onTertiary
            )
        }


        // Button to go to Statistics screen
        Button(
            onClick = { navController.navigate("statistics") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text(stringResource(R.string.statistics), color = MaterialTheme.colorScheme.onTertiary)
        }


        // Log out button
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("loggedOutScreen")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.log_out), color = MaterialTheme.colorScheme.onError)
        }

        // Show Admin Panel button only if the user is in the allowed list
        if (userEmail in adminEmails) {
            Button(
                onClick = { navController.navigate("adminPanel") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text(stringResource(R.string.admin_panel), color = MaterialTheme.colorScheme.onTertiary)
            }
        }

        LanguageSelector() // LanguageSelector at the bottom
    }
}
