package com.example.lostandfoundapp.userInterface

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
import com.example.lostandfoundapp.viewmodel.UserViewModel

@Composable
fun RegisterScreen(navController: NavController, userViewModel: UserViewModel) {
    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp)
    )
    {
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Register screen not done yet")

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.navigate("login") }) {
            Text("Go to login page")
        }
    }
}