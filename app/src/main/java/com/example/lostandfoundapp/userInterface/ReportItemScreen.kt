package com.example.lostandfoundapp.userInterface

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.lostandfoundapp.database.DatabaseHelper
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun ReportItemScreen(navController: NavHostController) {

    // State variables to hold the form input values
    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("") }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val latitude = remember { mutableStateOf("") }
    val longitude = remember { mutableStateOf("") }

    // For selecting images using ActivityResultContracts
    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri.value = uri
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current // Access the context for showing Toast messages
    val databaseHelper = DatabaseHelper()

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
                latitude.value.isNotEmpty() && longitude.value.isNotEmpty() && imageUri.value != null) {

                // Upload image to Firebase Storage
                val storageRef: StorageReference = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("items/${UUID.randomUUID()}.jpg")

                imageRef.putFile(imageUri.value!!)
                    .addOnSuccessListener { taskSnapshot ->
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            // Get the image URL after upload
                            val imageUrl = uri.toString()

                            // Now add the item to Firestore with the image URL
                            coroutineScope.launch(Dispatchers.Main) {
                                try {
                                    databaseHelper.addItem(
                                        title = title.value,
                                        description = description.value,
                                        category = category.value,
                                        imageUrl = imageUrl, // Pass the image URL here
                                        latitude = latitude.value.toDouble(),
                                        longitude = longitude.value.toDouble()
                                    )
                                    // Show success Toast
                                    Toast.makeText(context, "Item added successfully!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    // Show error Toast
                                    Toast.makeText(context, "Failed to add item: ${e.message}", Toast.LENGTH_SHORT).show()
                                    Log.e("ReportItemScreen", "Error adding item: ${e.message}")
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Show error Toast if image upload fails
                        Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

            } else {
                // Show error Toast if form fields are missing
                Toast.makeText(context, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Report Item")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.navigate("home") }) {
            Text("Go back")
        }
    }
}

