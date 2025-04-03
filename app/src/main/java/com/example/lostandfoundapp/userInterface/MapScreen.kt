package com.example.lostandfoundapp.userInterface

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostandfoundapp.model.Item
import com.example.lostandfoundapp.viewmodel.ItemViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.Marker

@Composable
fun MapScreen(navController: NavController, itemViewModel: ItemViewModel) {
    val context = LocalContext.current
    var locationPermissionGranted by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Check if permissions are granted
    LaunchedEffect(Unit) {
        locationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Observe LiveData (items) from ViewModel using observeAsState
    val items by itemViewModel.items.observeAsState(listOf()) // Using observeAsState to observe LiveData

    // Fetch items when the screen is first composed
    LaunchedEffect(Unit) {
        itemViewModel.getItems() // Fetch items from the database
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Map Screen")

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }

            if (!locationPermissionGranted) {
                Button(onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }) {
                    Text("Enable Location")
                }
            } else {
                GoogleMapView(locationPermissionGranted, items) // Pass the 'items' to GoogleMapView
            }
        }
    }
}


@Composable
fun GoogleMapView(locationPermissionGranted: Boolean, items: List<Item>) {
    val location = LatLng(60.16952000, 24.93545000) // Default to Helsinki
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 12f)
    }

    // Store the selected item (null if no selection)
    var selectedItem by remember { mutableStateOf<Item?>(null) }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = locationPermissionGranted // Enable user location
        ),
        uiSettings = remember {
            MapUiSettings(
                zoomControlsEnabled = true,
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true,
                tiltGesturesEnabled = true
            )
        }
    ) {
        // Add a marker for each item
        items.forEach { item ->
            item.location?.let { location ->
                Marker(
                    state = rememberMarkerState(position = LatLng(location.latitude, location.longitude)),
                    title = item.title,
                    snippet = item.description,
                    onClick = {
                        selectedItem = item // Set selected item
                        true // Consume the event
                    }
                )
            }
        }
    }

    // Show image dialog when a marker is clicked
    selectedItem?.let { item ->
        ImageDialog(item = item, onDismiss = { selectedItem = null })
    }
}

@Composable
fun ImageDialog(item: Item, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = item.title ?: "No Title") },
        text = {
            Column {
                AsyncImage(
                    model = item.imageUrl, // Load image from URL
                    contentDescription = "Item Image",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = item.description ?: "No Description")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}