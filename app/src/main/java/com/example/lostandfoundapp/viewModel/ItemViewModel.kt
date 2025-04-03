package com.example.lostandfoundapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostandfoundapp.database.DatabaseHelper
import kotlinx.coroutines.launch

class ItemViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper()

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
}
