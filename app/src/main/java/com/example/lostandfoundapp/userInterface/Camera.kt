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

@Composable
fun CameraScreen(navController: NavController) {
    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp)
    )
    {
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Camera screen not done yet")
        Text(text = "Might be better to have this in the report item screen")
        Text(text = "But for now it will be here")

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.navigate("home") }) {
            Text("Go to home page")
        }
    }
}