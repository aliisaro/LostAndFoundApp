package com.example.lostandfoundapp.userInterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lostandfoundapp.R

@Composable
fun LoggedOutScreen(navController: NavController) {
    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Spacer(modifier = androidx.compose.ui.Modifier.height(20.dp))

        // Logout message
        Text(text = stringResource(R.string.logout_message))

        Spacer(modifier = androidx.compose.ui.Modifier.height(20.dp))

        // Go to login page button
        Button(onClick = {
            navController.navigate("login")
        }) {
            Text(text = stringResource(R.string.login))
        }

        Text(text = stringResource(R.string.or)) // Or text

        // Go to register page button
        Button(onClick = {
            navController.navigate("register")
        }) {
            Text(text = stringResource(R.string.register))
        }
    }
}