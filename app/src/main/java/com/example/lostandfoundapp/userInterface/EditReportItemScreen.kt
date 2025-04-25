package com.example.lostandfoundapp.userInterface

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.lostandfoundapp.R
import com.example.lostandfoundapp.database.DatabaseHelper
import com.example.lostandfoundapp.model.Item
import com.google.android.gms.location.LocationServices
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun EditReportItemScreen(navController: NavHostController, itemId: String) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var item by remember { mutableStateOf<Item?>(null) }
    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("") }
    val latitude = remember { mutableStateOf("") }
    val longitude = remember { mutableStateOf("") }
    val checked = remember { mutableStateOf(false) }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val imageChanged = remember { mutableStateOf(false) }
    val showDeleteConfirmation = remember { mutableStateOf(false) }

    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri.value = uri
        imageChanged.value = true
    }

    val englishToLocalizedCategoryMap = mapOf(
        "clothing" to context.getString(R.string.clothing),
        "accessories" to context.getString(R.string.accessories),
        "keys" to context.getString(R.string.keys),
        "other" to context.getString(R.string.other)
    )

    val localizedToEnglishCategoryMap = englishToLocalizedCategoryMap.entries.associate { (eng, local) -> local to eng }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            item = DatabaseHelper().getItemById(itemId)
            item?.let {
                title.value = it.title
                description.value = it.description
                category.value = englishToLocalizedCategoryMap[it.category] ?: context.getString(R.string.other)
                latitude.value = it.location?.latitude?.toString() ?: ""
                longitude.value = it.location?.longitude?.toString() ?: ""
                checked.value = it.showContactEmail
            }
        }
    }

    fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                latitude.value = it.latitude.toString()
                longitude.value = it.longitude.toString()
            }
        }
    }

    fun updateItemButtonAction() {
        if (title.value.isEmpty() || description.value.isEmpty() || category.value.isEmpty() ||
            latitude.value.isEmpty() || longitude.value.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.required_fields_error), Toast.LENGTH_SHORT).show()
            return
        }

        fun updateToDatabase(imageUrl: String?) {
            coroutineScope.launch {
                try {
                    DatabaseHelper().updateItem(
                        itemId = itemId,
                        title = title.value,
                        description = description.value,
                        category = localizedToEnglishCategoryMap[category.value] ?: "other",
                        imageUrl = imageUrl ?: item?.imageUrl ?: "",
                        latitude = latitude.value.toDouble(),
                        longitude = longitude.value.toDouble(),
                        showContactEmail = checked.value
                    )
                    Toast.makeText(context,
                        context.getString(R.string.item_updated_successfully), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context,
                        context.getString(R.string.failed_to_update_item, e.message), Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (imageChanged.value && imageUri.value != null) {
            val storageRef: StorageReference = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("items/${UUID.randomUUID()}.jpg")
            imageRef.putFile(imageUri.value!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        updateToDatabase(uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context,
                        context.getString(R.string.failed_to_upload_image), Toast.LENGTH_SHORT).show()
                }
        } else {
            updateToDatabase(null)
        }
    }

    fun deleteItem() {
        coroutineScope.launch {
            try {
                DatabaseHelper().deleteItem(itemId)
                Toast.makeText(context,
                    context.getString(R.string.item_deleted_successfully), Toast.LENGTH_SHORT).show()
                navController.navigate("home")
            } catch (e: Exception) {
                Toast.makeText(context,
                    context.getString(R.string.failed_to_delete_item, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
    if (showDeleteConfirmation.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation.value = false },
            title = { Text(stringResource(R.string.confirm_deletion)) },
            text = { Text(stringResource(R.string.action_cannot_be_undone)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation.value = false
                    coroutineScope.launch {
                        try {
                            DatabaseHelper().deleteItem(itemId)
                            Toast.makeText(context,
                                context.getString(R.string.item_deleted_successfully), Toast.LENGTH_SHORT).show()
                            navController.navigate("home")
                        } catch (e: Exception) {
                            Toast.makeText(context,
                                context.getString(R.string.failed_to_delete_item, e.message), Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    item?.let {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = stringResource(R.string.edit_reported_item), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(20.dp))

            TextField(value = title.value, onValueChange = { title.value = it }, label = { Text(stringResource(R.string.title)) }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            TextField(value = description.value, onValueChange = { description.value = it }, label = { Text(stringResource(R.string.description)) }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            Text(stringResource(R.string.select_a_category), fontSize = 18.sp)

            val categories = listOf(stringResource(R.string.clothing), stringResource(R.string.accessories), stringResource(R.string.keys), stringResource(R.string.other))
            categories.forEach { cat ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = category.value == cat, onClick = { category.value = cat })
                    Text(cat)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { getContent.launch("image/*") }) {
                    Text(stringResource(R.string.select_image))
                }
                Spacer(modifier = Modifier.width(10.dp))
                if (imageUri.value != null || it.imageUrl.isNotEmpty()) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                    Text(stringResource(R.string.image_selected))
                } else {
                    Text(stringResource(R.string.no_image_selected), color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            TextField(value = latitude.value, onValueChange = { latitude.value = it }, label = { Text("Latitude") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            TextField(value = longitude.value, onValueChange = { longitude.value = it }, label = { Text("Longitude") }, modifier = Modifier.fillMaxWidth())
            Text(stringResource(R.string.autofill_coordinates), fontSize = 13.sp, color = Color.Gray)

            Button(onClick = { getCurrentLocation() }, modifier = Modifier.padding(vertical = 10.dp)) {
                Text(stringResource(R.string.use_current_location))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.show_contact_email))
                Checkbox(checked = checked.value, onCheckedChange = { checked.value = it })
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { updateItemButtonAction() }) {
                    Text(stringResource(R.string.update_item))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = { showDeleteConfirmation.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(stringResource(R.string.delete_report), color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = { navController.navigate("home") }) {
                Text(stringResource(R.string.go_back))
            }
        }
    }
}