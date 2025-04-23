package com.example.lostandfoundapp.userInterface

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.lostandfoundapp.ui.theme.LostAndFoundAppTheme
import com.example.lostandfoundapp.updateLocale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val langCode = newBase.getSharedPreferences("settings", MODE_PRIVATE)
            .getString("language", "en") ?: "en"
        val updatedContext = updateLocale(newBase, langCode)
        super.attachBaseContext(updatedContext)
    }

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