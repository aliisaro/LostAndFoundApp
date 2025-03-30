package com.example.lostandfoundapp.userInterface

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState


@Composable
fun MapScreen(navController: NavController) {
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

            GoogleMapView()
        }
    }
}

@Composable
fun GoogleMapView() {
    val location = LatLng(60.16952000, 24.93545000) // Example location (Helsinki)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 12f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = remember {
            com.google.maps.android.compose.MapUiSettings(
                zoomControlsEnabled = true, // Enables zoom buttons
                scrollGesturesEnabled = true, // Enables panning
                zoomGesturesEnabled = true, // Enables pinch-to-zoom
                tiltGesturesEnabled = true, // Allows tilting
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
