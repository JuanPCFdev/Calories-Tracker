package com.juanpcf.caloriestracker.ui.theme

import androidx.compose.ui.graphics.Color

// Light scheme
val Green40  = Color(0xFF006E1C)
val Green80  = Color(0xFF72DC6C)
val GreenContainer40 = Color(0xFF95F98D)
val Teal40   = Color(0xFF006874)
val Teal80   = Color(0xFF4DD8E8)
val TealContainer40  = Color(0xFF97F0FF)
val Orange40 = Color(0xFF9E4300)
val Orange80 = Color(0xFFFFB693)
val OrangeContainer40 = Color(0xFFFFDBCC)

val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary         = Green40,
    onPrimary       = Color.White,
    primaryContainer    = GreenContainer40,
    onPrimaryContainer  = Color(0xFF002204),
    secondary       = Teal40,
    onSecondary     = Color.White,
    secondaryContainer  = TealContainer40,
    onSecondaryContainer = Color(0xFF001F24),
    tertiary        = Orange40,
    onTertiary      = Color.White,
    tertiaryContainer   = OrangeContainer40,
    onTertiaryContainer = Color(0xFF341100),
    background      = Color(0xFFF8FDF4),
    onBackground    = Color(0xFF1A1C19),
    surface         = Color(0xFFF8FDF4),
    onSurface       = Color(0xFF1A1C19),
    surfaceVariant  = Color(0xFFDEE5D8),
    onSurfaceVariant = Color(0xFF424940),
    outline         = Color(0xFF72796F),
    error           = Color(0xFFBA1A1A),
    onError         = Color.White,
)

val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary         = Green80,
    onPrimary       = Color(0xFF003909),
    primaryContainer    = Color(0xFF005313),
    onPrimaryContainer  = GreenContainer40,
    secondary       = Teal80,
    onSecondary     = Color(0xFF00363D),
    secondaryContainer  = Color(0xFF004F57),
    onSecondaryContainer = TealContainer40,
    tertiary        = Orange80,
    onTertiary      = Color(0xFF552100),
    tertiaryContainer   = Color(0xFF783100),
    onTertiaryContainer = OrangeContainer40,
    background      = Color(0xFF1A1C19),
    onBackground    = Color(0xFFE1E3DE),
    surface         = Color(0xFF1A1C19),
    onSurface       = Color(0xFFE1E3DE),
    surfaceVariant  = Color(0xFF424940),
    onSurfaceVariant = Color(0xFFC2C9BC),
    outline         = Color(0xFF8C9388),
    error           = Color(0xFFFFB4AB),
    onError         = Color(0xFF690005),
)
