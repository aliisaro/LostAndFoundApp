package com.example.lostandfoundapp.utilities

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng


// Fetch user location using fused location provider
@SuppressLint("MissingPermission")
fun fetchUserLocation(context: Context, onLocationFetched: (LatLng) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        location?.let {
            onLocationFetched(LatLng(it.latitude, it.longitude))
        }
    }
}

// Calculate distance between two points
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

// Parse GeoPoint string to LatLng
fun parseGeoPoint(geoPointString: String): LatLng? {
    val regex = """GeoPoint \{ latitude=([-\d.]+), longitude=([-\d.]+) \}""".toRegex()
    val matchResult = regex.find(geoPointString)
    return if (matchResult != null) {
        val latitude = matchResult.groupValues[1].toDouble()
        val longitude = matchResult.groupValues[2].toDouble()
        LatLng(latitude, longitude)
    } else {
        null
    }
}
