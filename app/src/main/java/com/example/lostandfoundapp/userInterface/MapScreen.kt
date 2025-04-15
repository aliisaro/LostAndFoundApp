package com.example.lostandfoundapp.userInterface

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted.value =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Check location permission and load items
    LaunchedEffect(Unit) {
        locationPermissionGranted.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        items = databaseHelper.getLostItems()
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
            Text("Map Screen", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {  navController.navigate("home")}) {
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
                GoogleMapView(
                    items = items,
                    onItemFound = {
                        CoroutineScope(Dispatchers.Main).launch {
                            val updatedItems = databaseHelper.getLostItems()
                            items = updatedItems
                            Toast.makeText(context, "Item marked as found!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun GoogleMapView(
    items: List<Item>,
    onItemFound: () -> Unit
) {
    val defaultLocation = LatLng(60.16952, 24.93545) // Helsinki
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    var selectedItem by remember { mutableStateOf<Item?>(null) }

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
    onDismiss: () -> Unit,
    onConfirmFound: (Item) -> Unit
) {
    var checked by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = item.title)},
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

                Text(
                    text = "Category:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = item.category)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Description:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = item.description)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Location:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = item.location.toString())

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Reported At:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = item.registeredAt.toDate().toString())

                Spacer(modifier = Modifier.height(10.dp))

                if (item.showContactEmail) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Contact Email: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(text = item.contactEmail)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Mark as Found", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
