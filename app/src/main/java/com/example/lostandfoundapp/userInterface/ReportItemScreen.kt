package com.example.lostandfoundapp.userInterface

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.lostandfoundapp.viewmodel.ItemViewModel

@Composable
fun ReportItemScreen(navController: NavHostController, itemViewModel: ItemViewModel) {

    // State variables to hold the form input values
    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("") }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val latitude = remember { mutableStateOf("") }
    val longitude = remember { mutableStateOf("") }

    // For success and error messages
    val message = remember { mutableStateOf("") }
    val messageColor = remember { mutableStateOf(Color.Red) }

    // For selecting images using ActivityResultContracts
    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // This is where the selected image URI will be returned
        imageUri.value = uri
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Report Lost Item")

        Spacer(modifier = Modifier.height(20.dp))

        // Title input field
        Text("Title")
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter title") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Description input field
        Text("Description")
        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter description") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category input field
        Text("Category")
        TextField(
            value = category.value,
            onValueChange = { category.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter category") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Image selection button
        Button(onClick = { getContent.launch("image/*") }) {
            Text(text = "Select Image")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Display selected image (if any)
        if (imageUri.value != null) {
            Text(text = "Selected image URI: ${imageUri.value}")
        } else {
            Text(text = "No image selected")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Latitude input field
        Text("Latitude")
        TextField(
            value = latitude.value,
            onValueChange = { latitude.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter latitude") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Longitude input field
        Text("Longitude")
        TextField(
            value = longitude.value,
            onValueChange = { longitude.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter longitude") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Button to add the item
        Button(onClick = {
            if (title.value.isNotEmpty() && description.value.isNotEmpty() && category.value.isNotEmpty() &&
                latitude.value.isNotEmpty() && longitude.value.isNotEmpty()) {
                try {
                    // Convert latitude and longitude to Double before calling addItem
                    itemViewModel.addItem(
                        title = title.value,
                        description = description.value,
                        category = category.value,
                        imageUrl = imageUri.value.toString(),  // Store the URI string here
                        latitude = latitude.value.toDouble(),
                        longitude = longitude.value.toDouble()
                    )
                    message.value = "Item added successfully!"
                    messageColor.value = Color.Green
                } catch (e: Exception) {
                    message.value = "Failed to add item: ${e.localizedMessage}"
                    messageColor.value = Color.Red
                }
            } else {
                message.value = "Please fill in all required fields."
                messageColor.value = Color.Red
            }
        }) {
            Text("Report item")
        }

        // Display feedback message
        if (message.value.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = message.value, color = messageColor.value)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.navigate("home") }) {
            Text("Go back")
        }
    }
}