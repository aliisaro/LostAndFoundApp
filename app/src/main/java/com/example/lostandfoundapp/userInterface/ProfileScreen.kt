package com.example.lostandfoundapp.userInterface

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lostandfoundapp.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val userEmail = user?.email ?: "User not logged in"
    val userName = user?.displayName ?: "No name"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profiilikuva
        Image(
            painter = painterResource(id = R.drawable.ic_profile_placeholder), // Kuvan lataaminen drawable-kansiosta
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(120.dp) // Kuvan koko
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) // Reunus kuvassa
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Otsikko
        Text(
            text = stringResource(R.string.welcome, userName),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email
        Text(
            text = stringResource(R.string.email) + ": $userEmail",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Erottimet
        Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))

        Spacer(modifier = Modifier.height(16.dp))

        // Edit Profile Button
        Button(
            onClick = { navController.navigate("editProfile") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.edit_profile), color = MaterialTheme.colorScheme.onPrimary)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Change Password Button
        Button(
            onClick = { changePassword() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(stringResource(R.string.change_password), color = MaterialTheme.colorScheme.onSecondary)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Log Out Button
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

        // Go back to homepage button
        Button(onClick = { navController.navigate("home") }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.go_back))
        }
    }
}

fun changePassword() {
    val user = FirebaseAuth.getInstance().currentUser
    val newPassword = "UusiSalasana123" // Käyttäjän syöttämä uusi salasana

    user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("ProfileScreen", "Password updated successfully")
        } else {
            Log.e("ProfileScreen", "Password update failed", task.exception)
        }
    }
}
