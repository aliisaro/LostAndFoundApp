package com.example.lostandfoundapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lostandfoundapp.database.DatabaseHelper
import com.example.lostandfoundapp.model.Item
import kotlinx.coroutines.launch

class ItemViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper()

    // LiveData to hold the list of items
    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> = _items // Expose LiveData as read-only

    // Function to add a lost item
    fun addItem(title: String, description: String, category: String, imageUrl: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            dbHelper.addItem(title, description, category, imageUrl, latitude, longitude) { success, error ->
                // Log the result in Logcat instead of using LiveData
                if (success) {
                    Log.d("ItemViewModel", "Item added successfully")
                } else {
                    Log.e("ItemViewModel", "Failed to add item: $error")
                }
            }
        }
    }

    // Function to get all items and update LiveData
    fun getItems() {
        viewModelScope.launch {
            val fetchedItems = dbHelper.getItems() // Fetch items from the database
            _items.postValue(fetchedItems) // Update LiveData with the fetched items
        }
    }
}
