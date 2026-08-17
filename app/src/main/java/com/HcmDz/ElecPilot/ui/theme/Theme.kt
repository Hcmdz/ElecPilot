package com.HcmDz.ElecPilot.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = IndustrialBlue,
    onPrimary = White,
    primaryContainer = IndustrialBlueLight,
    secondary = AccentBlue,
    onSecondary = White,
    background = LightGray,
    onBackground = IndustrialBlueDark,
    surface = White,
    onSurface = IndustrialBlueDark,
    surfaceVariant = MediumGray,
    onSurfaceVariant = DarkGray,
    error = ErrorRed,
    onError = White
)

private val DarkColorScheme = darkColorScheme(
    primary = IndustrialBlueLight,
    onPrimary = White,
    primaryContainer = IndustrialBlue,
    secondary = AccentBlue,
    onSecondary = White,
    background = AmoledBlack,
    onBackground = AmoledText,
    surface = AmoledSurface,
    onSurface = AmoledText,
    surfaceVariant = AmoledGray,
    onSurfaceVariant = AmoledTextSecondary,
    error = ErrorRed,
    onError = White
)

@Composable
fun ElecPilotTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        themeMode == "materialYou" && Build.VERSION.SDK_INT >= 31 -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
