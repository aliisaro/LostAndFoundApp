package com.example.lostandfoundapp.userInterface

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lostandfoundapp.database.DatabaseHelper
import com.example.lostandfoundapp.model.Item
import kotlinx.coroutines.launch

@Composable
fun SearchItemScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var lostItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }
    var sortOrder by remember { mutableStateOf("Newest") } // or "Oldest"
    val coroutineScope = rememberCoroutineScope()

    // Fetch and filter data based on search query
    LaunchedEffect(searchQuery) {
        coroutineScope.launch {
            val result = DatabaseHelper().getLostItems()
            lostItems = if (searchQuery.isEmpty()) {
                result
            } else {
                result.filter { it.title.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search for item...") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { navController.navigate("home") }) {
                Text("Go Back")
            }
        }


        // Filtering by category
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf("All", "Clothing", "Keys", "Accessories", "Other")
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }

        // Sort dropdown
        var expanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.padding(vertical = 12.dp)) {
            Button(onClick = { expanded = true }) {
                Text("Sort: $sortOrder")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Newest") },
                    onClick = {
                        sortOrder = "Newest"
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Oldest") },
                    onClick = {
                        sortOrder = "Oldest"
                        expanded = false
                    }
                )
            }
        }

        // Display "No results" if there are no items
        val filteredItems = lostItems
            .filter {
                (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)) &&
                        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true))
            }
            .let { list ->
                if (sortOrder == "Oldest") list.sortedBy { it.registeredAt }
                else list.sortedByDescending { it.registeredAt }
            }

        LazyColumn {
            items(items = filteredItems) { item ->
                ItemCard(item = item, onClick = { selectedItem = item })
            }
        }

        selectedItem?.let { item ->
            ItemDetails(item = item, onDismiss = { selectedItem = null })
        }
    }
}

@Composable
fun ItemCard(item: Item, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = "Item Image",
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 12.dp)
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun ItemDetails(
    item: Item,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val locationText = item.location.toString()

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
                    text = "Reported At:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = item.registeredAt.toDate().toString())

                Spacer(modifier = Modifier.height(10.dp))

                if (item.showContactEmail) {
                    Text(
                        text = "Contact: ${item.contactEmail}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = "Location:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = item.location.toString())

                Spacer(modifier = Modifier.height(10.dp))

                // Add copy button for location
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(locationText))
                        Toast.makeText(context, "Location copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text("Copy Location")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
