package com.example.lostandfoundapp.userInterface

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lostandfoundapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

@Composable
fun ProfileScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val userEmail = user?.email ?: "User not logged in"
    val userName = user?.displayName ?: "No name"

    var isEditing by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(userName) }

    var isChangingPassword by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_profile_placeholder),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(120.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.welcome, userName),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.email) + ": $userEmail",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(30.dp))
        Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))

        // Muokkaa profiilia
        if (isEditing) {
            TextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text(stringResource(R.string.new_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                val updates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                user?.updateProfile(updates)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isEditing = false
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.save_changes))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { isEditing = false }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cancel))
            }
        } else {
            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.edit_profile), color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Vaihda salasana
        if (isChangingPassword) {
            TextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text(stringResource(R.string.new_password)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("ProfileScreen", "Password updated")
                        isChangingPassword = false
                        newPassword = ""
                    } else {
                        Log.e("ProfileScreen", "Password change failed", task.exception)
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Vaihda salasana")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { isChangingPassword = false }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cancel))
            }
        } else {
            Button(
                onClick = { isChangingPassword = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(stringResource(R.string.change_password), color = MaterialTheme.colorScheme.onSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Kirjaudu ulos
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.log_out), color = MaterialTheme.colorScheme.onError)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Palaa kotiin
        Button(onClick = { navController.navigate("home") }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.go_back))
        }
    }
}
