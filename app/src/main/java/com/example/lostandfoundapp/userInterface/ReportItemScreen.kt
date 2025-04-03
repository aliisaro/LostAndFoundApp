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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.lostandfoundapp.viewmodel.ItemViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ReportItemScreen(navController: NavHostController, itemViewModel: ItemViewModel) {

    // ADDED FOR TESTING PURPOSES ONLY/////////////////////////////////////////
    // Add item only once when the screen is first launched
    LaunchedEffect(Unit) {
        // Only call addItem if it's not already in progress
        itemViewModel.addItem(
            title = "wallet",
            description = "Testing item addition",
            category = "wallet",
            imageUrl = "", // Empty for now
            latitude = 40.7128,
            longitude = -74.0060
        )
        Log.d("TEST", "Item addition initiated")
    }
    //////////////////////////////////////////////////////////////////////////

    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp)
    )
    {
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Report item form not done yet")

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.navigate("home") }) {
            Text("Go back")
        }
    }
}