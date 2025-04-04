package com.example.lostandfoundapp.userInterface

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.height(20.dp))

        // Using default Text style
        Text(text = "Login Screen")

        Spacer(modifier = Modifier.height(20.dp))

        // Email input
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Password input
        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Login Button
        Button(
            onClick = {
                val emailStr = email.value
                val passwordStr = password.value

                if (emailStr.isEmpty() || passwordStr.isEmpty()) {
                    // Skip error message here, as we want no errors other than registration failure
                    return@Button
                }

                auth.signInWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // On successful login, navigate to home
                            navController.navigate("home")
                        } else {
                            // Only show error if login fails
                            Toast.makeText(navController.context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Navigate to RegisterScreen
        Button(onClick = { navController.navigate("register") }) {
            Text("Go to Register page")
        }
    }
}
