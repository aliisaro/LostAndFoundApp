package com.example.lostandfoundapp.userInterface

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.GeoPoint
import com.example.lostandfoundapp.model.Item

@Composable
fun EditReportItemScreen(
    navController: NavController,
    itemId: String
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val itemState = remember { mutableStateOf<Item?>(null) }

    // Form states
    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("") }
    val showEmail = remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        try {
            val documentSnapshot = db.collection("items").document(itemId).get().await()
            val item = documentSnapshot.toObject(Item::class.java)
            itemState.value = item
            item?.let {
                title.value = it.title
                description.value = it.description
                category.value = it.category
                showEmail.value = it.showContactEmail
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load item: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(text = "Edit Lost Item", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Category", style = MaterialTheme.typography.bodyLarge)
        Row {
            listOf("Clothing", "Accessories", "Keys", "Other").forEach { option ->
                Row(
                    modifier = Modifier.padding(end = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = category.value == option,
                        onClick = { category.value = option }
                    )
                    Text(option)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(
                checked = showEmail.value,
                onCheckedChange = { showEmail.value = it }
            )
            Text("Show my contact email")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val item = itemState.value
            if (item != null) {
                coroutineScope.launch {
                    try {
                        db.collection("items").document(itemId).update(
                            mapOf(
                                "title" to title.value,
                                "description" to description.value,
                                "category" to category.value,
                                "showContactEmail" to showEmail.value
                            )
                        ).await()
                        Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show()
                        navController.navigate("home")
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to update item: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }) {
            Text("Update Item")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Cancel")
        }
    }
}