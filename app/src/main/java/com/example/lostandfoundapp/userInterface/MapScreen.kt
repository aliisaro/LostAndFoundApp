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
    val items by itemViewModel.items.observeAsState(listOf()) // Observe LiveData for items
    val locationPermissionGranted = remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted.value = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Check and request permissions
    LaunchedEffect(Unit) {
        locationPermissionGranted.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Fetch items from the ViewModel
    LaunchedEffect(Unit) {
        itemViewModel.getItems()
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

            if (!locationPermissionGranted.value) {
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
                // Show map when permission is granted
                GoogleMapView(items = items)
            }
        }
    }
}

@Composable
fun GoogleMapView(items: List<Item>) {
    val defaultLocation = LatLng(60.16952000, 24.93545000) // Default to Helsinki
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    var selectedItem by remember { mutableStateOf<Item?>(null) }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true // Always enable user location if permission is granted
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true,
            tiltGesturesEnabled = true
        )
    ) {
        items.forEach { item ->
            item.location?.let { location ->
                Marker(
                    state = rememberMarkerState(position = LatLng(location.latitude, location.longitude)),
                    title = item.title,
                    snippet = item.description,
                    onClick = {
                        selectedItem = item // Set selected item when marker is clicked
                        true // Consume the event
                    }
                )
            }
        }
    }

    // Show item image and description in a dialog when a marker is clicked
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
                    model = item.imageUrl,
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