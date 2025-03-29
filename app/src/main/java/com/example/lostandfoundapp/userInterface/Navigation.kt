package com.example.lostandfoundapp.userInterface

import androidx.compose.runtime.Composable
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lostandfoundapp.userInterface.HomeScreen
import com.example.lostandfoundapp.userInterface.MapScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("map") { MapScreen(navController) }
    }
}