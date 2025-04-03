package com.example.lostandfoundapp.userInterface

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.lostandfoundapp.viewmodel.ItemViewModel

@Composable
fun Navigation() {
    val navController = rememberNavController()

    val user = FirebaseAuth.getInstance().currentUser
    val reportItemViewModel: ItemViewModel = viewModel()  // ViewModel instance

    NavHost(navController = navController, startDestination = if (user != null) "home" else "login") {
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable("home") {
            HomeScreen(navController)
        }
        composable("map") {
            MapScreen(navController)
        }
        composable("reportItem") {
            ReportItemScreen(navController, reportItemViewModel)
        }
        composable("loggedOutScreen") {
            LoggedOutScreen(navController)
        }
    }
}
