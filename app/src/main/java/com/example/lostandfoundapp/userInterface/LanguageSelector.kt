package com.example.lostandfoundapp.userInterface

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.lostandfoundapp.R
import com.example.lostandfoundapp.utilities.updateLocale

@Composable
fun LanguageSelector() {
    // Available language options
    val languages = listOf("en", "fi")

    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // UI state for menu expansion and currently selected language
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember {
        mutableStateOf(sharedPrefs.getString("language", "en") ?: "en")
    }

    Box {
        // Button that shows current language and opens dropdown
        OutlinedButton(onClick = { expanded = true }) {
            Text(stringResource(R.string.language, selectedLanguage.uppercase()))
        }

        // Dropdown menu for selecting language
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            languages.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang.uppercase()) },
                    onClick = {
                        // Update selected language
                        selectedLanguage = lang
                        expanded = false

                        // Save new language in preferences
                        sharedPrefs.edit().putString("language", lang).apply()

                        // Apply locale change
                        updateLocale(context, lang)

                        // Restart activity to apply changes immediately
                        (context as? Activity)?.recreate()
                    }
                )
            }
        }
    }
}


