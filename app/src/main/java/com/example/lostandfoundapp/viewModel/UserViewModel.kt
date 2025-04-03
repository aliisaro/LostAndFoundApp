package com.example.lostandfoundapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lostandfoundapp.database.DatabaseHelper
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData to hold success/failure status
    val userRegistrationStatus = MutableLiveData<Pair<Boolean, String?>>()

    private val dbHelper = DatabaseHelper()

    // Function to register a user
    fun registerUser(email: String, password: String, username: String) {
        viewModelScope.launch {
            dbHelper.registerUser(email, password, username) { success, error ->
                // Post the result to LiveData to observe it in the UI
                userRegistrationStatus.postValue(Pair(success, error))
            }
        }
    }
}
