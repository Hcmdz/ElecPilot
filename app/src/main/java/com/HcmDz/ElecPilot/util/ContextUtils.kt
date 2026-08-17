package com.HcmDz.ElecPilot.util

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

fun localizedContext(app: Application): Context {
    val lang = app.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .getString("app_language", "en") ?: "en"
    val locale = Locale(lang)
    val config = Configuration(app.resources.configuration)
    config.setLocale(locale)
    return app.createConfigurationContext(config)
}
