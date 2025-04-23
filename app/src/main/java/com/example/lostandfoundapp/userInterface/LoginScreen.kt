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
fun LoginScreen(navController: NavController) {
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

        Text(text = stringResource(R.string.login), style = MaterialTheme.typography.titleLarge)

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

        // Login Button
        Button(
            onClick = {
                val emailStr = email.value
                val passwordStr = password.value

                if (emailStr.isEmpty() || passwordStr.isEmpty()) {
                    // Error message if both fields are empty
                    Toast.makeText(
                        navController.context,
                        navController.context.getString(R.string.login_failed_empty_fields),
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }

                auth.signInWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // On successful login, navigate to home
                            navController.navigate("home")
                        } else {
                            // Only show error if login fails
                            Toast.makeText(
                                navController.context,
                                navController.context.getString(R.string.login_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        ) {
            Text(stringResource(R.string.login))
        }


        // Navigate to RegisterScreen
        Text(
            text = stringResource(R.string.register_instead),
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable {
                    navController.navigate("register")
                }
        )

        Spacer(modifier = Modifier.height(20.dp))

        LanguageSelector() // LanguageSelector at the bottom
    }
}
