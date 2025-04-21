package com.example.lostandfoundapp.userInterface

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
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

    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    val locationPermissionGranted = remember { mutableStateOf(false) }

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

        items = databaseHelper.getLostItems()
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Map Screen", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("home") }) {
                Text("Go Back")
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
                    Text("Enable Location")
                }
            } else {
                Column {
                    TextField(
                        value = searchGeoPoint,
                        onValueChange = { searchGeoPoint = it },
                        label = { Text("Paste GeoPoint location") },
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
                            userLocation = LatLng(latitude, longitude)
                            searchGeoPoint = ""
                        } else {
                            Toast.makeText(context, "Invalid GeoPoint format", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Go to Location")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GoogleMapView(
                        items = items,
                        userLocation = userLocation,
                        onItemFound = {
                            CoroutineScope(Dispatchers.Main).launch {
                                items = databaseHelper.getLostItems()
                                Toast.makeText(context, "Item marked as found!", Toast.LENGTH_SHORT).show()
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
    onItemFound: () -> Unit
) {
    val defaultLocation = LatLng(60.16952, 24.93545)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    var selectedItem by remember { mutableStateOf<Item?>(null) }

    // Update camera position whenever userLocation changes
    LaunchedEffect(userLocation) {
        userLocation?.let {
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
                    state = rememberMarkerState(position = LatLng(location.latitude, location.longitude)),
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

                Text("Category:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.category)

                Spacer(modifier = Modifier.height(10.dp))

                Text("Description:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.description)

                Spacer(modifier = Modifier.height(10.dp))

                Text("Location:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.location.toString())

                Spacer(modifier = Modifier.height(10.dp))

                Text("Reported At:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.registeredAt.toDate().toString())

                Spacer(modifier = Modifier.height(10.dp))

                if (distanceInKm != null) {
                    Text("Distance to item:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(distanceInKm)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (item.showContactEmail) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Contact Email: ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(item.contactEmail)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Mark as Found", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    Text("I am the owner and I found this item")
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Close")
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
