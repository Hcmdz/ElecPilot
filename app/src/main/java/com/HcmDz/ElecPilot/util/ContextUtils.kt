package com.HcmDz.ElecPilot.util

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

// The system language is passed in, never read from Locale.getDefault(): attachBaseContext
// calls Locale.setDefault() with the resolved value, so reading it back would make "system"
// resolve to whatever was resolved at first launch instead of the live system locale.
fun resolveLanguage(stored: String, systemLanguage: String): String {
    if (stored != "system") return stored
    return when (systemLanguage) {
        "fr", "en", "ar" -> systemLanguage
        else -> "en"
    }
}

fun localizedContext(app: Application): Context {
    val storedLang = app.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .getString("app_language", "system") ?: "system"
    val resolvedLang = resolveLanguage(storedLang, app.resources.configuration.locales[0].language)
    val locale = Locale.forLanguageTag(resolvedLang)
    val config = Configuration(app.resources.configuration)
    config.setLocale(locale)
    return app.createConfigurationContext(config)
}
