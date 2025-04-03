package com.example.lostandfoundapp.userInterface

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lostandfoundapp.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController, userViewModel: UserViewModel) {
    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp)
    )
    {
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Login screen not done yet")

        Spacer(modifier = Modifier.height(20.dp))

        /////// Temporary login button (tämän voi poistaa kun login form on tehty)////
        Button(onClick = {
            val email = "testi@example.com"
            val password = "Password123"

            userViewModel.loginUser(email, password)
            // Navigate to home after login
            navController.navigate("home")
        }) {
            Text("Login with test user")
        }
        ////////////////////////////////////////////////////////////////////////

        Button(onClick = { navController.navigate("register") }) {
            Text("Go to Register page")
        }
    }
}