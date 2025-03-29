package com.example.lostandfoundapp.userInterface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lostandfoundapp.ui.theme.LostAndFoundAppTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHost

import com.google.firebase.firestore.FirebaseFirestore
val db = FirebaseFirestore.getInstance() // Firestore instance


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LostAndFoundAppTheme {
                Navigation()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    LostAndFoundAppTheme {
        // Your MainContent goes here
        Navigation()
    }
}