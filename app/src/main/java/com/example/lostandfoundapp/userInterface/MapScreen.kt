package com.example.lostandfoundapp.userInterface

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostandfoundapp.R
import com.example.lostandfoundapp.database.DatabaseHelper
import com.example.lostandfoundapp.model.Item
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val databaseHelper = DatabaseHelper()

    // State for items, initialized as an empty list
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }

    // State to track if location permission is granted
    var locationPermissionGranted by remember { mutableStateOf(false) }

    // State for the pasted location from the TextField
    var pastedLocation by remember { mutableStateOf<LatLng?>(null) }

    // State for the user's current location
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // State for the text input in the search field
    var searchGeoPoint by remember { mutableStateOf("") }

    // Launcher for requesting location permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Fetch items and user location when the composable is first launched
    LaunchedEffect(Unit) {
        // Check if location permission is already granted
        locationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // If permission is granted, fetch the user's last known location
        if (locationPermissionGranted) {
            fetchUserLocation(context) { location ->
                userLocation = location
            }
        }

        // Fetch and filter items to show only those within 30 days
        items = fetchRecentItems(databaseHelper)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                stringResource(R.string.map),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Show button to request location permission if not granted
            if (!locationPermissionGranted) {
                Button(onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }) {
                    Text(stringResource(R.string.enable_location))
                }
            } else {
                Column {
                    // Text field for pasting GeoPoint location
                    TextField(
                        value = searchGeoPoint,
                        onValueChange = { searchGeoPoint = it },
                        label = { Text(stringResource(R.string.paste_geopoint_location)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row for navigation and location buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { navController.navigate("home") }) {
                            Text(stringResource(R.string.go_back))
                        }

                        Spacer(modifier = Modifier.width(140.dp))

                        // Button to navigate to the pasted location
                        Button(onClick = {
                            pastedLocation = parseGeoPoint(searchGeoPoint, context)
                            searchGeoPoint = ""
                        }) {
                            Text(stringResource(R.string.go_to_location))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Google Map view
                    GoogleMapView(
                        items = items,
                        userLocation = userLocation,
                        pastedLocation = pastedLocation,
                        onItemFound = {
                            CoroutineScope(Dispatchers.Main).launch {
                                items = databaseHelper.getLostItems()
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.item_marked_as_found),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

// Function to fetch the user's last known location
@SuppressLint("MissingPermission")
private fun fetchUserLocation(context: android.content.Context, onLocationFetched: (LatLng) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            onLocationFetched(LatLng(location.latitude, location.longitude))
        }
    }
}

// Function to fetch recent items (within 30 days)
private suspend fun fetchRecentItems(databaseHelper: DatabaseHelper): List<Item> {
    val maxDaysOld = 30
    return databaseHelper.getLostItems().filter {
        val daysSinceReported =
            (System.currentTimeMillis() - it.registeredAt.seconds * 1000) / (1000 * 60 * 60 * 24)
        daysSinceReported <= maxDaysOld
    }
}

// Function to parse GeoPoint input
private fun parseGeoPoint(geoPointString: String, context: android.content.Context): LatLng? {
    val regex = """GeoPoint \{ latitude=([-\d.]+), longitude=([-\d.]+) \}""".toRegex()
    val matchResult = regex.find(geoPointString)
    return if (matchResult != null) {
        val latitude = matchResult.groupValues[1].toDouble()
        val longitude = matchResult.groupValues[2].toDouble()
        LatLng(latitude, longitude)
    } else {
        Toast.makeText(
            context,
            context.getString(R.string.invalid_geopoint_format),
            Toast.LENGTH_SHORT
        ).show()
        null
    }
}

// Function to calculate distance between two points
fun calculateDistance(
    startLat: Double,
    startLng: Double,
    endLat: Double,
    endLng: Double
): Float {
    val startLocation = Location("").apply {
        latitude = startLat
        longitude = startLng
    }
    val endLocation = Location("").apply {
        latitude = endLat
        longitude = endLng
    }
    return startLocation.distanceTo(endLocation) / 1000f
}


@Composable
fun GoogleMapView(
    items: List<Item>,
    userLocation: LatLng?,
    pastedLocation: LatLng?,
    onItemFound: () -> Unit
) {

    // Set default location for map
    val defaultLocation = LatLng(60.16952, 24.93545)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    // State
    var selectedItem by remember { mutableStateOf<Item?>(null) }

    // Update camera position whenever the pasted location changes
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

    // if an item is selected, show its details in an AlertDialog
    selectedItem?.let { item ->
        ItemDetailsOnMap(
            item = item,
            userLocation = userLocation,
            onDismiss = { selectedItem = null },
            onConfirmFound = { foundItem ->
                val dbHelper = DatabaseHelper()
                CoroutineScope(Dispatchers.Main).launch {
                    dbHelper.markItemAsFound(foundItem.id, foundItem.reportedBy)
                    selectedItem = null
                    onItemFound()
                }
            }
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
    // State to track checkbox state
    var checked by remember { mutableStateOf(false) }

    // Coroutine scope for asynchronous operations
    val coroutineScope = rememberCoroutineScope()

    // Extract the seconds field from the Timestamp object
    val daysSinceReported = remember(item) {
        // Current timestamp in milliseconds
        val currentDate = System.currentTimeMillis()

        // Timestamp object from the Item
        val timestamp = item.registeredAt

        // Extract the seconds field and convert to milliseconds
        val registeredDateMillis = timestamp.seconds * 1000 // Convert seconds to milliseconds

        // Calculate the difference in milliseconds
        val diffInMillis = currentDate - registeredDateMillis

        // Convert milliseconds to days
        val daysSinceReported = diffInMillis / (1000 * 60 * 60 * 24)
        daysSinceReported
    }

    // Calculate distance to the item from the devices distance
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
                Text(
                    stringResource(R.string.description),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(item.description)

                Spacer(modifier = Modifier.height(10.dp))

                // Display item location
                Text(
                    stringResource(R.string.location),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(item.location.toString())

                Spacer(modifier = Modifier.height(10.dp))

                // Display item reported date
                Text(
                    stringResource(R.string.reported_at),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(item.registeredAt.toDate().toString())

                Spacer(modifier = Modifier.height(10.dp))

                // Display days since reported
                Text(
                    stringResource(R.string.days_since_reported, daysSinceReported),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Display distance to the item
                if (distanceInKm != null) {
                    Text(
                        stringResource(R.string.distance_to_item),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(distanceInKm)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Display contact email if showContactEmail is true
                if (item.showContactEmail) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.contact_email, item.contactEmail),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                // Checkbox to mark item as found
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.mark_as_found),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Checkbox(checked = checked, onCheckedChange = { checked = it })
                }
            }
        },
        // Confirm button only if checkbox is checked
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
        // Dismiss button
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
