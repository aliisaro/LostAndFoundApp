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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lostandfoundapp.R
import com.example.lostandfoundapp.database.DatabaseHelper
import com.example.lostandfoundapp.model.Item
import kotlinx.coroutines.launch

@Composable
fun SearchItemScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var lostItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }
    var sortOrderResId by remember { mutableStateOf(R.string.newest) }
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
                label = { Text(stringResource(R.string.search_for_item)) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { navController.navigate("home") }) {
                Text(text = stringResource(R.string.go_back))
            }
        }


        // Filtering by category
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categoryMap = mapOf(
                "All" to stringResource(R.string.all),
                "Clothing" to stringResource(R.string.clothing),
                "Keys" to stringResource(R.string.keys),
                "Accessories" to stringResource(R.string.accessories),
                "Other" to stringResource(R.string.other)
            )

            categoryMap.forEach { (key, label) ->
                FilterChip(
                    selected = selectedCategory == key,
                    onClick = { selectedCategory = key },
                    label = { Text(label) }
                )
            }
        }

        // Sort dropdown
        var expanded by remember { mutableStateOf(false) }
        val currentSortLabel = stringResource(id = sortOrderResId)

        Box(modifier = Modifier.padding(vertical = 12.dp)) {
            Button(onClick = { expanded = true }) {
                Text(stringResource(R.string.sort, currentSortLabel))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.newest)) },
                    onClick = {
                        sortOrderResId = R.string.newest
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.oldest)) },
                    onClick = {
                        sortOrderResId = R.string.oldest
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
                if (sortOrderResId == R.string.oldest) list.sortedBy { it.registeredAt }
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
                    text = stringResource(R.string.description),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = item.description)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.reported_at),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = item.registeredAt.toDate().toString())

                Spacer(modifier = Modifier.height(10.dp))

                // Display days since reported
                Text(stringResource(R.string.days_since_reported, daysSinceReported), fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(10.dp))
                
                if (item.showContactEmail) {
                    Text(
                        text = stringResource(R.string.contact, item.contactEmail),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = stringResource(R.string.location),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = item.location.toString())

                Spacer(modifier = Modifier.height(10.dp))

                // Add copy button for location
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(locationText))
                        Toast.makeText(context,
                            context.getString(R.string.location_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text(stringResource(R.string.copy_location))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
