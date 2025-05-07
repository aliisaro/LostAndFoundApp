package com.example.lostandfoundapp.userInterface

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.lostandfoundapp.R
import com.example.lostandfoundapp.database.DatabaseHelper
import com.example.lostandfoundapp.model.Item
import com.example.lostandfoundapp.utilities.fetchUserLocation
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun EditReportItemScreen(navController: NavHostController, itemId: String) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // State variables to hold the form input values
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

    val getContent =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri.value = uri
            imageChanged.value = true
        }

    // Fetch item details when the screen is first displayed
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
                if (it.imageUrl.isNotEmpty()) {
                    imageUri.value = Uri.parse(it.imageUrl)
                }
            }
        }
    }

    // Fetch current location
    fun setToCurrentLocation() {
        fetchUserLocation(context) { location ->
            latitude.value = location.latitude.toString()
            longitude.value = location.longitude.toString()
            //Toast.makeText(context, context.getString(R.string.location_updated), Toast.LENGTH_SHORT).show()
        }
    }

    // Function to handle the "Update Item" button click
    fun updateItemButtonAction() {
        if (title.value.isEmpty() || description.value.isEmpty() || category.value.isEmpty() ||
            latitude.value.isEmpty() || longitude.value.isEmpty()
        ) {
            Toast.makeText(
                context,
                context.getString(R.string.required_fields_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Update the item in the database
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
                    Toast.makeText(
                        context,
                        context.getString(R.string.item_updated_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failed_to_update_item, e.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Upload new image if changed
        if (imageChanged.value && imageUri.value != null) {
            val storageRef: StorageReference = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("items/${UUID.randomUUID()}.jpg")
            imageRef.putFile(imageUri.value!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        updateToDatabase(uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failed_to_upload_image), Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            updateToDatabase(null)
        }
    }

    // Confirmation dialog for deleting the item
    if (showDeleteConfirmation.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation.value = false },
            title = { Text(stringResource(R.string.confirm_deletion)) },
            text = { Text(stringResource(R.string.action_cannot_be_undone)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation.value = false
                    coroutineScope.launch {
                        try {
                            DatabaseHelper().deleteItem(itemId)
                            Toast.makeText(
                                context,
                                context.getString(R.string.item_deleted_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("home")
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.failed_to_delete_item, e.message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation.value = false }) {
                    Text(stringResource(R.string.cancel))
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

            // Title of the screen
            Text(
                text = stringResource(R.string.edit_reported_item),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Title input field
            TextField(
                value = title.value,
                onValueChange = { title.value = it },
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Description input field
            TextField(
                value = description.value,
                onValueChange = { description.value = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Selection
            Text(stringResource(R.string.select_a_category), fontSize = 18.sp)

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = category.value == "Clothing",
                        onClick = { category.value = "Clothing" }
                    )
                    Text(stringResource(R.string.clothing))

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = category.value == "Accessories",
                        onClick = { category.value = "Accessories" }
                    )
                    Text(stringResource(R.string.accessories))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = category.value == "Keys",
                        onClick = { category.value = "Keys" }
                    )
                    Text(stringResource(R.string.keys))

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = category.value == "Other",
                        onClick = { category.value = "Other" }
                    )
                    Text(stringResource(R.string.other))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display Image
            if (imageUri.value != null) {
                AsyncImage(
                    model = imageUri.value,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 10.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.image_selected))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Image selected",
                        tint = Color(0xFF4CAF50)
                    )
                }
            } else {
                Text(text = stringResource(R.string.no_image_selected), color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Image Selection Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { getContent.launch("image/*") }) {
                    Text(stringResource(R.string.select_image))
                }
            }


            Spacer(modifier = Modifier.height(10.dp))

            // Latitude input field
            TextField(
                value = latitude.value,
                onValueChange = { latitude.value = it },
                label = { Text("Latitude") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Longitude input field
            TextField(
                value = longitude.value,
                onValueChange = { longitude.value = it },
                label = { Text("Longitude") },
                modifier = Modifier.fillMaxWidth()
            )

            // Information about coordinates
            Text(
                stringResource(R.string.autofill_coordinates),
                fontSize = 13.sp,
                color = Color.Gray
            )

            // Button to set location to current location
            Button(
                onClick = { setToCurrentLocation() },
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                Text(stringResource(R.string.use_current_location))
            }

            // Checkbox to show email
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.show_contact_email))
                Checkbox(checked = checked.value, onCheckedChange = { checked.value = it })
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Buttons to update item and delete report
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { updateItemButtonAction() }) {
                    Text(stringResource(R.string.update_item))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = { showDeleteConfirmation.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(stringResource(R.string.delete_report), color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Button to go back
            Button(onClick = { navController.popBackStack() }) {
                Text(stringResource(R.string.go_back))
            }
        }
    }
}