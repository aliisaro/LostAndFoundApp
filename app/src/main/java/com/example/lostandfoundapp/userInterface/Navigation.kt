package com.example.lostandfoundapp.userInterface

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.lostandfoundapp.userInterface.ReportItemScreen


@Composable
fun Navigation() {
    val navController = rememberNavController()

    val user = FirebaseAuth.getInstance().currentUser

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
            ReportItemScreen(navController)
        }
        composable("loggedOutScreen") {
            LoggedOutScreen(navController)
        }
        composable("camera") {
            CameraScreen(navController)
        }
        composable("profile") {
            ProfileScreen(navController)
        }
    }
}
