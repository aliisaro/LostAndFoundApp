package com.example.lostandfoundapp.userInterface

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostandfoundapp.R
import com.example.lostandfoundapp.model.Item
import com.example.lostandfoundapp.utilities.calculateDistance
import com.example.lostandfoundapp.utilities.parseGeoPoint
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun MapContent(
    navController: NavController,
    items: List<Item>,
    userLocation: LatLng?,
    pastedLocation: LatLng?,
    searchGeoPoint: String,
    onSearchGeoPointChange: (String) -> Unit,
    onLocationSelected: (LatLng?) -> Unit,
    onConfirmFound: (Item) -> Unit
) {
    Column {
        // TextField for pasting GeoPoint location
        TextField(
            value = searchGeoPoint,
            onValueChange = onSearchGeoPointChange,
            label = { Text(stringResource(R.string.paste_geopoint_location)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Buttons for navigating to the pasted location and going back to home screen
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                onLocationSelected(parseGeoPoint(searchGeoPoint))
            }) {
                Text(stringResource(R.string.go_to_location))
            }

            Spacer(modifier = Modifier.width(140.dp))

            // Button to go back to home screen
            Button(onClick = { navController.navigate("home") }) {
                Text(stringResource(R.string.go_back))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Google map
        GoogleMapView(
            items = items,
            userLocation = userLocation,
            pastedLocation = pastedLocation,
            onConfirmFound = onConfirmFound
        )
    }
}


@Composable
fun GoogleMapView(
    items: List<Item>,
    userLocation: LatLng?,
    pastedLocation: LatLng?,
    onConfirmFound: (Item) -> Unit
) {
    // Default location for the map
    val defaultLocation = LatLng(60.16952, 24.93545)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    var selectedItem by remember { mutableStateOf<Item?>(null) }

    // Move camera to user location when user location is available
    LaunchedEffect(pastedLocation) {
        pastedLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 12f)
        }
    }

    // Google map with markers for lost items
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true),
        uiSettings = MapUiSettings(zoomControlsEnabled = true)
    ) {
        items.forEach { item ->
            item.location?.let { location ->
                Marker(
                    state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                    title = item.title,
                    snippet = item.description,
                    onClick = {
                        selectedItem = item
                        true
                    }
                )
            }
        }
    }

    selectedItem?.let { item ->
        ItemDetailsOnMap(
            item = item,
            userLocation = userLocation,
            onDismiss = { selectedItem = null },
            onConfirmFound = onConfirmFound
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ItemDetailsOnMap(
    item: Item,
    userLocation: LatLng?,
    onDismiss: () -> Unit,
    onConfirmFound: (Item) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var checked by remember { mutableStateOf(false) }

    val daysSinceReported = remember(item) {
        val currentDate = System.currentTimeMillis()
        val registeredDateMillis = item.registeredAt.seconds * 1000
        val diffInMillis = currentDate - registeredDateMillis
        diffInMillis / (1000 * 60 * 60 * 24)
    }

    val distanceInKm = remember(item, userLocation) {
        item.location?.let { itemLoc ->
            userLocation?.let { userLoc ->
                val result = calculateDistance(
                    userLoc.latitude,
                    userLoc.longitude,
                    itemLoc.latitude,
                    itemLoc.longitude
                )
                String.format("%.2f km", result)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = item.title) },
        text = {
            Column {
                // Display item image
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = "Item Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Display item description
                Text(stringResource(R.string.description), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.description)

                Spacer(modifier = Modifier.height(10.dp))

                // Display item location
                Text(stringResource(R.string.location), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.location.toString())

                Spacer(modifier = Modifier.height(10.dp))

                // Display item registered date
                Text(stringResource(R.string.reported_at), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.registeredAt.toDate().toString())

                Spacer(modifier = Modifier.height(10.dp))

                // Display days since reported
                Text(stringResource(R.string.days_since_reported, daysSinceReported), fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(10.dp))

                // Display distance to item if user location is available
                if (distanceInKm != null) {
                    Text(stringResource(R.string.distance_to_item), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(distanceInKm)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Display contact email if showContactEmail is true
                if (item.showContactEmail) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.contact_email, item.contactEmail), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // Checkbox for marking item as found
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.mark_as_found), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Checkbox(checked = checked, onCheckedChange = { checked = it })
                }
            }
        },
        // Confirm button to mark item as found if checkbox is checked
        confirmButton = {
            if (checked) {
                Button(onClick = {
                    coroutineScope.launch {
                        onConfirmFound(item)
                    }
                    onDismiss()
                }) {
                    Text(stringResource(R.string.confirm_ownership))
                }
            }
        },
        // Dismiss button to close the dialog
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}