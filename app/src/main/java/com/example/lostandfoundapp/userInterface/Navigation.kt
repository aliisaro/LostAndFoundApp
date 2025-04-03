package com.example.lostandfoundapp.userInterface

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.lostandfoundapp.viewmodel.ItemViewModel
import com.example.lostandfoundapp.viewmodel.UserViewModel

@Composable
fun Navigation() {
    val navController = rememberNavController()

    val user = FirebaseAuth.getInstance().currentUser
    val itemViewModel: ItemViewModel = viewModel()  // ViewModel instance
    val userViewModel: UserViewModel = viewModel()  // ViewModel instance

    NavHost(navController = navController, startDestination = if (user != null) "home" else "login") {
        composable("login") {
            LoginScreen(navController, userViewModel)
        }
        composable("register") {
            RegisterScreen(navController, userViewModel)
        }
        composable("home") {
            HomeScreen(navController)
        }
        composable("map") {
            MapScreen(navController, itemViewModel)
        }
        composable("reportItem") {
            ReportItemScreen(navController, itemViewModel)
        }
        composable("loggedOutScreen") {
            LoggedOutScreen(navController)
        }
    }
}
