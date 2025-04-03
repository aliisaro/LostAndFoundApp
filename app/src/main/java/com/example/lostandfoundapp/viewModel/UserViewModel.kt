package com.example.lostandfoundapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lostandfoundapp.database.DatabaseHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper()

    // Function to register a user
    fun registerUser(email: String, password: String, username: String) {
        viewModelScope.launch {
            dbHelper.registerUser(email, password, username) { success, error ->
                // Log registration result
                if (success) {
                    Log.d("UserViewModel", "Registration Successful")
                } else {
                    Log.e("UserViewModel", "Registration Failed: $error")
                }
            }
        }
    }

    // Function to log in a user
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Attempt to sign in the user
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
                // Log success status
                Log.d("UserViewModel", "Login Successful")
            } catch (e: Exception) {
                // Log failure status
                Log.e("UserViewModel", "Login Failed: ${e.message}")
            }
        }
    }
}
