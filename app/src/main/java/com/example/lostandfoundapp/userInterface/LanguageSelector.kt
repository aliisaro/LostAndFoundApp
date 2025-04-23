package com.example.lostandfoundapp.userInterface

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.lostandfoundapp.R
import com.example.lostandfoundapp.updateLocale

@Composable
fun LanguageSelector() {
    val languages = listOf("en", "fi")
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(sharedPrefs.getString("language", "en") ?: "en") }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(stringResource(R.string.language, selectedLanguage.uppercase()))
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            languages.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang.uppercase()) },
                    onClick = {
                        selectedLanguage = lang
                        expanded = false

                        sharedPrefs.edit().putString("language", lang).apply()
                        updateLocale(context, lang)
                        (context as? Activity)?.recreate() // restart to apply changes
                    }
                )
            }
        }
    }
}
