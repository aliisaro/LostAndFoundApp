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
fun AdminSearchItemScreen(navController: NavController) {
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

            Button(onClick = { navController.navigate("adminPanel") }) {
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
                AdminItemCard(item = item, onClick = { selectedItem = item })
            }
        }

        selectedItem?.let { item ->
            AdminItemDetails(
                item = item,
                onDismiss = { selectedItem = null },
                onItemDeleted = {
                    selectedItem = null
                    coroutineScope.launch {
                        lostItems = DatabaseHelper().getLostItems()
                    }
                }
            )
        }
    }
}

@Composable
fun AdminItemCard(item: Item, onClick: () -> Unit) {
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
fun AdminItemDetails(
    item: Item,
    onDismiss: () -> Unit,
    onItemDeleted: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val locationText = item.location.toString()
    var showConfirmDialog by remember { mutableStateOf(false) }

    val daysSinceReported = remember(item) {
        val currentDate = System.currentTimeMillis()
        val registeredDateMillis = item.registeredAt.seconds * 1000
        (currentDate - registeredDateMillis) / (1000 * 60 * 60 * 24)
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
                Text(text = item.description)

                Spacer(modifier = Modifier.height(10.dp))

                Text(stringResource(R.string.reported_at), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = item.registeredAt.toDate().toString())

                Spacer(modifier = Modifier.height(10.dp))

                Text(stringResource(R.string.days_since_reported, daysSinceReported), fontWeight = FontWeight.Bold, fontSize = 16.sp)

                if (item.showContactEmail) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(stringResource(R.string.contact_email, item.contactEmail), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(stringResource(R.string.location), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = locationText)

                Spacer(modifier = Modifier.height(10.dp))

                Button(onClick = {
                    clipboardManager.setText(AnnotatedString(locationText))
                    Toast.makeText(context, context.getString(R.string.location_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                }) {
                    Text(stringResource(R.string.copy_location))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete_report))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.confirm_deletion)) },
            text = { Text(stringResource(R.string.action_cannot_be_undone)) },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        coroutineScope.launch {
                            try {
                                DatabaseHelper().deleteItem(item.id)
                                Toast.makeText(context, context.getString(R.string.item_deleted_successfully), Toast.LENGTH_SHORT).show()
                                onDismiss()
                                onItemDeleted()
                            } catch (e: Exception) {
                                Toast.makeText(context, context.getString(R.string.failed_to_delete_item), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
