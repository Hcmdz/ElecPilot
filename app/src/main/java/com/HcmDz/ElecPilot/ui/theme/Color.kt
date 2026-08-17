package com.HcmDz.ElecPilot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.HcmDz.ElecPilot.util.MatchType

val IndustrialBlue = Color(0xFF1B3A5C)
val IndustrialBlueLight = Color(0xFF2C5F8A)
val IndustrialBlueDark = Color(0xFF0F2440)
val LightGray = Color(0xFFF5F5F5)
val MediumGray = Color(0xFFE0E0E0)
val DarkGray = Color(0xFF616161)
val White = Color(0xFFFFFFFF)
val ErrorRed = Color(0xFFD32F2F)
val AccentBlue = Color(0xFF1976D2)

@Composable
fun matchTypeColor(type: MatchType): Color = when (type) {
    MatchType.EXACT -> MaterialTheme.colorScheme.primary
    MatchType.NORMALIZED -> MaterialTheme.colorScheme.secondary
    MatchType.STRIPPED -> MaterialTheme.colorScheme.tertiary
    MatchType.VOICE_NORMALIZED -> MaterialTheme.colorScheme.error
    MatchType.SUBSTRING -> MaterialTheme.colorScheme.onSurface
    MatchType.FUZZY -> MaterialTheme.colorScheme.onSurfaceVariant
}

val AmoledBlack = Color(0xFF000000)
val AmoledSurface = Color(0xFF121212)
val AmoledGray = Color(0xFF2C2C2C)
val AmoledText = Color(0xFFFFFFFF)
val AmoledTextSecondary = Color(0xFF9E9E9E)
