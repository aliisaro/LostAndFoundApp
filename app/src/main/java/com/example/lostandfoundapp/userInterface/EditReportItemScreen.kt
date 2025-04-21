package com.example.lostandfoundapp.userInterface

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.lostandfoundapp.database.DatabaseHelper
import com.example.lostandfoundapp.model.Item
import com.google.android.gms.location.LocationServices
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun EditReportItemScreen(navController: NavHostController, itemId: String) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var item by remember { mutableStateOf<Item?>(null) }
    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("") }
    val latitude = remember { mutableStateOf("") }
    val longitude = remember { mutableStateOf("") }
    val checked = remember { mutableStateOf(false) }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val imageChanged = remember { mutableStateOf(false) }
    val showDeleteConfirmation = remember { mutableStateOf(false) }

    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri.value = uri
        imageChanged.value = true
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            item = DatabaseHelper().getItemById(itemId)
            item?.let {
                title.value = it.title
                description.value = it.description
                category.value = it.category
                latitude.value = it.location?.latitude?.toString() ?: ""
                longitude.value = it.location?.longitude?.toString() ?: ""
                checked.value = it.showContactEmail
            }
        }
    }

    fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                latitude.value = it.latitude.toString()
                longitude.value = it.longitude.toString()
            }
        }
    }

    fun updateItemButtonAction() {
        if (title.value.isEmpty() || description.value.isEmpty() || category.value.isEmpty() ||
            latitude.value.isEmpty() || longitude.value.isEmpty()) {
            Toast.makeText(context, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        fun updateToDatabase(imageUrl: String?) {
            coroutineScope.launch {
                try {
                    DatabaseHelper().updateItem(
                        itemId = itemId,
                        title = title.value,
                        description = description.value,
                        category = category.value,
                        imageUrl = imageUrl ?: item?.imageUrl ?: "",
                        latitude = latitude.value.toDouble(),
                        longitude = longitude.value.toDouble(),
                        showContactEmail = checked.value
                    )
                    Toast.makeText(context, "Item updated successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to update item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (imageChanged.value && imageUri.value != null) {
            val storageRef: StorageReference = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("items/${UUID.randomUUID()}.jpg")
            imageRef.putFile(imageUri.value!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        updateToDatabase(uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            updateToDatabase(null)
        }
    }

    fun deleteItem() {
        coroutineScope.launch {
            try {
                DatabaseHelper().deleteItem(itemId)
                Toast.makeText(context, "Item deleted successfully!", Toast.LENGTH_SHORT).show()
                navController.navigate("home")
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to delete item: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    if (showDeleteConfirmation.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation.value = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this report? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation.value = false
                    coroutineScope.launch {
                        try {
                            DatabaseHelper().deleteItem(itemId)
                            Toast.makeText(context, "Item deleted successfully!", Toast.LENGTH_SHORT).show()
                            navController.navigate("home")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to delete item: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    item?.let {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Edit Reported Item", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(20.dp))

            TextField(value = title.value, onValueChange = { title.value = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            TextField(value = description.value, onValueChange = { description.value = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            Text("Select a category:", fontSize = 18.sp)
            val categories = listOf("Clothing", "Accessories", "Keys", "Other")
            categories.forEach { cat ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = category.value == cat, onClick = { category.value = cat })
                    Text(cat)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { getContent.launch("image/*") }) {
                    Text("Select Image")
                }
                Spacer(modifier = Modifier.width(10.dp))
                if (imageUri.value != null || it.imageUrl.isNotEmpty()) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                    Text(" Image selected")
                } else {
                    Text(" No image selected", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            TextField(value = latitude.value, onValueChange = { latitude.value = it }, label = { Text("Latitude") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            TextField(value = longitude.value, onValueChange = { longitude.value = it }, label = { Text("Longitude") }, modifier = Modifier.fillMaxWidth())
            Text("Tap 'Use Current Location' to autofill coordinates.", fontSize = 13.sp, color = Color.Gray)

            Button(onClick = { getCurrentLocation() }, modifier = Modifier.padding(vertical = 10.dp)) {
                Text("Use Current Location")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Show contact email")
                Checkbox(checked = checked.value, onCheckedChange = { checked.value = it })
            }

            Button(onClick = { updateItemButtonAction() }) {
                Text("Update Item")
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { showDeleteConfirmation.value = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete Report", color = Color.White)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { navController.navigate("home") }) {
                Text("Go back")
            }
        }
    }
}