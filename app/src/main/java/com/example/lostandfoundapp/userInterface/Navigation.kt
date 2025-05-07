package com.example.lostandfoundapp.userInterface

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Navigation() {
    // Initialize NavController
    val navController = rememberNavController()

    // Check if user is logged in, navigate to home screen if logged in, otherwise to login
    val user = FirebaseAuth.getInstance().currentUser

    // Define navigation routes
    NavHost(
        navController = navController,
        startDestination = if (user != null) "home" else "login"
    ) {
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
        composable("searchReports") {
            SearchReportsScreen(navController)
        }
        composable("statistics") {
            StatisticsScreen(navController)
        }
        composable("editReportItem/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            EditReportItemScreen(navController, itemId)
        }
        composable("searchOwnReports") {
            SearchOwnReportsScreen(navController)
        }
        composable("adminPanel") {
            AdminPanel(navController)
        }
        composable("adminSearch") {
            AdminSearchItemScreen(navController)
        }
    }
}
