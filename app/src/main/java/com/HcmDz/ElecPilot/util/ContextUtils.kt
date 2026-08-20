package com.HcmDz.ElecPilot.util

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

fun resolveLanguage(stored: String): String {
    if (stored != "system") return stored
    return when (Locale.getDefault().language) {
        "fr", "en", "ar" -> Locale.getDefault().language
        else -> "en"
    }
}

fun localizedContext(app: Application): Context {
    val storedLang = app.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .getString("app_language", "system") ?: "system"
    val resolvedLang = resolveLanguage(storedLang)
    val locale = Locale.forLanguageTag(resolvedLang)
    val config = Configuration(app.resources.configuration)
    config.setLocale(locale)
    return app.createConfigurationContext(config)
}
