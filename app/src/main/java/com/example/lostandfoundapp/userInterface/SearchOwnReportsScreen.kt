package com.example.lostandfoundapp.userInterface

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostandfoundapp.R
import com.example.lostandfoundapp.database.DatabaseHelper
import com.example.lostandfoundapp.model.Item
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun SearchItemEditScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var lostItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("All") }
    var sortOrderResId by remember { mutableStateOf(R.string.newest) }
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
                label = { Text(stringResource(R.string.search_your_reports)) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { navController.navigate("home") }) {
                Text(stringResource(R.string.go_back))
            }
        }

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

        val filteredItems = lostItems
            .filter {
                it.contactEmail == currentUserEmail &&
                        (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)) &&
                        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true))
            }
            .let { list ->
                if (sortOrderResId == R.string.oldest) list.sortedBy { it.registeredAt }
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