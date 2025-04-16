package com.example.lostandfoundapp.userInterface

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostandfoundapp.database.DatabaseHelper
import com.example.lostandfoundapp.model.Item
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun SearchItemEditScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var lostItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("All") }
    var sortOrder by remember { mutableStateOf("Newest") }
    val coroutineScope = rememberCoroutineScope()
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

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
                label = { Text("Search your reports...") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { navController.navigate("home") }) {
                Text("Go Back")
            }
        }

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

        var expanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.padding(vertical = 12.dp)) {
            Button(onClick = { expanded = true }) {
                Text("Sort: $sortOrder")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(text = { Text("Newest") }, onClick = {
                    sortOrder = "Newest"
                    expanded = false
                })
                DropdownMenuItem(text = { Text("Oldest") }, onClick = {
                    sortOrder = "Oldest"
                    expanded = false
                })
            }
        }

        val filteredItems = lostItems
            .filter {
                it.contactEmail == currentUserEmail &&
                        (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)) &&
                        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true))
            }
            .let { list ->
                if (sortOrder == "Oldest") list.sortedBy { it.registeredAt }
                else list.sortedByDescending { it.registeredAt }
            }

        LazyColumn {
            items(items = filteredItems) { item ->
                UserItemCard(item = item) {
                    navController.navigate("editReportItem/${item.id}")
                }
            }
        }
    }
}

@Composable
fun UserItemCard(item: Item, onClick: () -> Unit) {
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