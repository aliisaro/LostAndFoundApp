package com.example.lostandfoundapp.utilities

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

// Helper function to apply a new locale to the app context
fun updateLocale(context: Context, languageCode: String): Context {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.createConfigurationContext(config)
    } else {
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        context
    }
}