package com.example.lostandfoundapp.userInterface

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lostandfoundapp.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Spacer(modifier = Modifier.height(20.dp))

        // Using default Text style
        Text(text = stringResource(R.string.register), style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(20.dp))

        // Email input
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Password input
        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Register Button
        Button(
            onClick = {
                val emailStr = email.value
                val passwordStr = password.value

                if (emailStr.isEmpty() || passwordStr.isEmpty()) {
                    // Error message if both fields are empty
                    Toast.makeText(
                        navController.context,
                        navController.context.getString(R.string.registration_failed_empty_fields),
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }

                auth.createUserWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // On successful registration, navigate to login page
                            Toast.makeText(
                                navController.context,
                                navController.context.getString(R.string.registration_succesfull),
                                Toast.LENGTH_SHORT
                            ).show()

                            navController.navigate("login")
                        } else {
                            // Show error message if registration fails
                            Toast.makeText(
                                navController.context,
                                navController.context.getString(R.string.registration_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        ) {
            Text(stringResource(R.string.register))
        }


        // Navigate to LoginScreen
        Text(
            text = stringResource(R.string.login_instead),
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable {
                    navController.navigate("login")
                }
        )

        Spacer(modifier = Modifier.height(20.dp))

        LanguageSelector() // LanguageSelector at the bottom
    }
}

