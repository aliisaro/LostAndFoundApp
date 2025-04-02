package com.example.lostandfoundapp.userInterface

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(navController: NavController) {
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

            Spacer(modifier = Modifier.height(20.dp))

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
                GoogleMapView(locationPermissionGranted)
            }
        }
    }
}

@Composable
fun GoogleMapView(locationPermissionGranted: Boolean) {
    val location = LatLng(60.16952000, 24.93545000) // Default to Helsinki
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 12f)
    }

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
        Marker(
            state = rememberMarkerState(position = location),
            title = "Example Marker",
            snippet = "This is Helsinki."
        )
    }
}
