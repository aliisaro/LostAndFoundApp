package com.example.lostandfoundapp.userInterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import java.lang.reflect.Modifier


@Composable
fun LoggedOutScreen(navController: NavController) {
    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Spacer(modifier = androidx.compose.ui.Modifier.height(20.dp))

        Text(text = "You have logged out")

        Spacer(modifier = androidx.compose.ui.Modifier.height(20.dp))

        // Go to login page button
        Button(onClick = {
            // Navigate to the login page
            navController.navigate("login")
        }) {
            Text("Login")
        }

        Text(text = "or")

        // Go to register page button
        Button(onClick = {
            // Navigate to the register page
            navController.navigate("register")
        }) {
            Text("Register")
        }
    }
}