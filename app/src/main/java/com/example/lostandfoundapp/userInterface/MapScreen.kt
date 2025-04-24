package com.example.lostandfoundapp.userInterface

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val databaseHelper = DatabaseHelper()

    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    val locationPermissionGranted = remember { mutableStateOf(false) }

    var pastedLocation by remember { mutableStateOf<LatLng?>(null) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var searchGeoPoint by remember { mutableStateOf<String>("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted.value =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        locationPermissionGranted.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (locationPermissionGranted.value) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)
                }
            }
        }

        // Max days limit (30 days)
        val maxDaysOld = 30

        // Fetch and filter items to show only those within 30 days
        items = databaseHelper.getLostItems().filter {
            val daysSinceReported = (System.currentTimeMillis() - it.registeredAt.seconds * 1000) / (1000 * 60 * 60 * 24)
            daysSinceReported <= maxDaysOld
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.map), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("home") }) {
                Text(stringResource(R.string.go_back))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!locationPermissionGranted.value) {
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
                    TextField(
                        value = searchGeoPoint,
                        onValueChange = { searchGeoPoint = it },
                        label = { Text(stringResource(R.string.paste_geopoint_location)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = {
                        // Parse GeoPoint input
                        val regex = """GeoPoint \{ latitude=([-\d.]+), longitude=([-\d.]+) \}""".toRegex()
                        val matchResult = regex.find(searchGeoPoint)
                        if (matchResult != null) {
                            val latitude = matchResult.groupValues[1].toDouble()
                            val longitude = matchResult.groupValues[2].toDouble()
                            pastedLocation = LatLng(latitude, longitude)
                            searchGeoPoint = ""
                        } else {
                            Toast.makeText(context,
                                context.getString(R.string.invalid_geopoint_format), Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(stringResource(R.string.go_to_location))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GoogleMapView(
                        items = items,
                        userLocation = userLocation,
                        pastedLocation = pastedLocation,
                        onItemFound = {
                            CoroutineScope(Dispatchers.Main).launch {
                                items = databaseHelper.getLostItems()
                                Toast.makeText(context,
                                    context.getString(R.string.item_marked_as_found), Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GoogleMapView(
    items: List<Item>,
    userLocation: LatLng?,
    pastedLocation: LatLng?,
    onItemFound: () -> Unit
) {
    val defaultLocation = LatLng(60.16952, 24.93545)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

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

    selectedItem?.let { item ->
        ItemDetails(
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

@Composable
fun ItemDetails(
    item: Item,
    userLocation: LatLng?,
    onDismiss: () -> Unit,
    onConfirmFound: (Item) -> Unit
) {
    var checked by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Extract the seconds field from the Timestamp object
    val daysSinceReported = remember(item) {
        val currentDate = System.currentTimeMillis() // Current timestamp in milliseconds

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

                // Display days since reported
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
