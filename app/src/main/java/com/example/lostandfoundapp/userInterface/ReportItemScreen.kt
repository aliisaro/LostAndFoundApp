package com.example.lostandfoundapp.userInterface

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.example.lostandfoundapp.R
import com.example.lostandfoundapp.database.DatabaseHelper
import com.google.android.gms.location.LocationServices
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun ReportItemScreen(navController: NavHostController) {
    // State variables to hold the form input values
    val title = rememberSaveable { mutableStateOf("") }
    val description = rememberSaveable { mutableStateOf("") }
    val category = rememberSaveable { mutableStateOf("") }
    val imageUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    val latitude = rememberSaveable { mutableStateOf("") }
    val longitude = rememberSaveable { mutableStateOf("") }
    val checked = rememberSaveable { mutableStateOf(false) }

    // For selecting images using ActivityResultContracts
    val getContent =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri.value = uri
        }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current // Access the context for showing Toast messages
    val databaseHelper = DatabaseHelper()

    // FusedLocationProviderClient for fetching location
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Function to get the current location
    fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions or handle the error
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                // Set the current latitude and longitude in the state
                latitude.value = it.latitude.toString()
                longitude.value = it.longitude.toString()
            }
        }
    }

    // Call getCurrentLocation() when the screen is first displayed
    LaunchedEffect(Unit) {
        getCurrentLocation()
    }

    // Function to handle the "Report Item" button click
    fun reportItemButtonAction() {
        if (title.value.isNotEmpty() && description.value.isNotEmpty() && category.value.isNotEmpty() &&
            latitude.value.isNotEmpty() && longitude.value.isNotEmpty() && imageUri.value != null
        ) {

            // Upload image to Firebase Storage
            val storageRef: StorageReference = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("items/${UUID.randomUUID()}.jpg")

            imageRef.putFile(imageUri.value!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Get the image URL after upload
                        val imageUrl = uri.toString()

                        // Add the item to Fire store with the image URL
                        coroutineScope.launch(Dispatchers.Main) {
                            try {
                                databaseHelper.addItem(
                                    title = title.value,
                                    description = description.value,
                                    category = category.value, // Pass the category here
                                    imageUrl = imageUrl, // Pass the image URL here
                                    latitude = latitude.value.toDouble(),
                                    longitude = longitude.value.toDouble(),
                                    showContactEmail = checked.value
                                )
                                // Show success message
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.item_added_successfully),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                // Show error message
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.failed_to_add_item),
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(
                                    "ReportItemScreen",
                                    context.getString(R.string.error_adding_item)
                                )
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    // Show error message if image upload fails
                    Toast.makeText(
                        context,
                        context.getString(R.string.failed_to_upload_image), Toast.LENGTH_SHORT
                    ).show()
                }

        } else {
            // Show error message if form fields are missing
            Toast.makeText(
                context,
                context.getString(R.string.required_fields_error), Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Title of the screen
        Text(
            text = stringResource(R.string.report_lost_item),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Title input field
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.enter_title)) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Description input field
        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.enter_description)) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category selection using Radio Buttons
        Text(stringResource(R.string.select_a_category), fontSize = 18.sp)

        // Radio Buttons for categories
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = category.value == "Clothing",
                    onClick = { category.value = "Clothing" }
                )
                Text(stringResource(R.string.clothing))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
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
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = category.value == "Other",
                    onClick = { category.value = "Other" }
                )
                Text(stringResource(R.string.other))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Image selection button
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { getContent.launch("image/*") }) {
                Text(text = stringResource(R.string.select_image))
            }

            // Display selected image
            if (imageUri.value != null) {
                Text(text = stringResource(R.string.image_selected))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Image selected",
                    tint = Color(0xFF4CAF50) // Green checkmark
                )
            } else {
                Text(text = stringResource(R.string.no_image_selected), color = Color.Gray)
            }

            // Go to take a picture with the camera
            Button(
                onClick = { navController.navigate("camera") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.take_a_picture))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Latitude input field (automatically set from device)
        TextField(
            value = latitude.value,
            onValueChange = { latitude.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.enter_latitude)) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Longitude input field (automatically set from device)
        TextField(
            value = longitude.value,
            onValueChange = { longitude.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.enter_longitude)) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Information about latitude and longitude
        Text(
            text = stringResource(R.string.latlng_info),
            fontSize = 13.sp,
            color = Color.Gray
        )

        // Checkbox to show contact email
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        ) {
            Text(text = stringResource(R.string.show_email))
            Spacer(modifier = Modifier.width(8.dp))
            Checkbox(
                checked = checked.value,
                onCheckedChange = { checked.value = it }
            )
        }

        // Button to add the item
        Button(onClick = {
            reportItemButtonAction()
        }) {
            Text(stringResource(R.string.report_item))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Button to go back to the home screen
        Button(onClick = { navController.navigate("home") }) {
            Text(stringResource(R.string.go_back))
        }
    }
}




