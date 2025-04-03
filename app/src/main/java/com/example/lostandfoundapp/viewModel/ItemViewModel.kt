package com.example.lostandfoundapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lostandfoundapp.database.DatabaseHelper
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

class ItemViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData to hold success/failure status for adding items
    val itemAddStatus = MutableLiveData<Pair<Boolean, String?>>()

    private val dbHelper = DatabaseHelper()

    // Function to add a lost item
    fun addItem(title: String, description: String, category: String, imageUrl: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            dbHelper.addItem(title, description, category, imageUrl, latitude, longitude) { success, error ->
                // Post the result to LiveData to observe it in the UI
                itemAddStatus.postValue(Pair(success, error))
            }
        }
    }
}
