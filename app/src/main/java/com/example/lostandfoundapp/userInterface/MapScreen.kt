package com.example.lostandfoundapp.userInterface

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.lostandfoundapp.R
import com.example.lostandfoundapp.database.DatabaseHelper
import com.example.lostandfoundapp.model.Item
import com.example.lostandfoundapp.utilities.fetchUserLocation
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val databaseHelper = DatabaseHelper()
    val coroutineScope = rememberCoroutineScope()

    // State variables for managing map and location data
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var pastedLocation by remember { mutableStateOf<LatLng?>(null) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var searchGeoPoint by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Ask for location permission and items when the composable is first composed
    // and fetch user location and lost items if permission is granted
    LaunchedEffect(Unit) {
        locationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (locationPermissionGranted) {
            fetchUserLocation(context) { location ->
                userLocation = location
            }
        }

        // Fetch items from database
        items = databaseHelper.getLostItems()
    }

    // Callback for marking an item as found
    fun handleItemFound(item: Item) {
        coroutineScope.launch {
            try {
                databaseHelper.markItemAsFound(item.id, item.reportedBy)
                Toast.makeText(context,
                    context.getString(R.string.item_marked_as_found), Toast.LENGTH_SHORT).show()
                    items = databaseHelper.getLostItems() // Refresh the item list
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context,
                    context.getString(R.string.error_marking_item_as_found), Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            // Title of the screen
            Text(
                stringResource(R.string.map),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Show enable location button if permission is not granted, else show the map
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

                Button(onClick = { navController.navigate("home") }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.go_back))
                }

            } else {
                MapContent(
                    navController = navController,
                    items = items,
                    userLocation = userLocation,
                    pastedLocation = pastedLocation,
                    searchGeoPoint = searchGeoPoint,
                    onSearchGeoPointChange = { searchGeoPoint = it },
                    onLocationSelected = { location -> pastedLocation = location },
                    onConfirmFound = { item -> handleItemFound(item) }
                )
            }
        }
    }
}
