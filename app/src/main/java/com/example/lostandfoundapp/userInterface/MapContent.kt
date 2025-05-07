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
        TextField(
            value = searchGeoPoint,
            onValueChange = onSearchGeoPointChange,
            label = { Text(stringResource(R.string.paste_geopoint_location)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                onLocationSelected(parseGeoPoint(searchGeoPoint))
            }) {
                Text(stringResource(R.string.go_to_location))
            }

            Button(onClick = { navController.navigate("home") }) {
                Text(stringResource(R.string.go_back))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
    val defaultLocation = LatLng(60.16952, 24.93545)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    var selectedItem by remember { mutableStateOf<Item?>(null) }

    LaunchedEffect(pastedLocation) {
        pastedLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 12f)
        }
    }

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
    var checked by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = "Item Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(stringResource(R.string.description), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.description)

                Spacer(modifier = Modifier.height(10.dp))

                Text(stringResource(R.string.location), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.location.toString())

                Spacer(modifier = Modifier.height(10.dp))

                Text(stringResource(R.string.reported_at), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.registeredAt.toDate().toString())

                Spacer(modifier = Modifier.height(10.dp))

                Text(stringResource(R.string.days_since_reported, daysSinceReported), fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(10.dp))

                if (distanceInKm != null) {
                    Text(stringResource(R.string.distance_to_item), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(distanceInKm)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (item.showContactEmail) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.contact_email, item.contactEmail), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.mark_as_found), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Checkbox(checked = checked, onCheckedChange = { checked = it })
                }
            }
        },
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
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

