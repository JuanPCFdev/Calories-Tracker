package com.juanpcf.caloriestracker.ui.theme

import androidx.compose.ui.graphics.Color

// Brand colors
val Green40  = Color(0xFF4CAF50)
val Green80  = Color(0xFF81C784)
val GreenContainer40 = Color(0xFFC8E6C9)
val Blue40   = Color(0xFF4A90E2)
val Blue80   = Color(0xFF90CAF9)
val BlueContainer40  = Color(0xFFBBDEFB)
val Orange40 = Color(0xFFFF9800)
val Orange80 = Color(0xFFFFCC80)
val OrangeContainer40 = Color(0xFFFFE0B2)

val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary             = Green40,
    onPrimary           = Color.White,
    primaryContainer    = GreenContainer40,
    onPrimaryContainer  = Color(0xFF1B5E20),
    secondary           = Blue40,
    onSecondary         = Color.White,
    secondaryContainer  = BlueContainer40,
    onSecondaryContainer = Color(0xFF0D47A1),
    tertiary            = Orange40,
    onTertiary          = Color.White,
    tertiaryContainer   = OrangeContainer40,
    onTertiaryContainer = Color(0xFF4E2600),
    background          = Color(0xFFF5F7F5),
    onBackground        = Color(0xFF1A1C19),
    surface             = Color(0xFFF5F7F5),
    onSurface           = Color(0xFF1A1C19),
    surfaceVariant      = Color(0xFFE0E8E0),
    onSurfaceVariant    = Color(0xFF404840),
    outline             = Color(0xFF70796F),
    error               = Color(0xFFBA1A1A),
    onError             = Color.White,
)

val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary             = Green80,
    onPrimary           = Color(0xFF1B5E20),
    primaryContainer    = Color(0xFF2E7D32),
    onPrimaryContainer  = GreenContainer40,
    secondary           = Blue80,
    onSecondary         = Color(0xFF0D47A1),
    secondaryContainer  = Color(0xFF1565C0),
    onSecondaryContainer = BlueContainer40,
    tertiary            = Orange80,
    onTertiary          = Color(0xFF4E2600),
    tertiaryContainer   = Color(0xFFE65100),
    onTertiaryContainer = OrangeContainer40,
    background          = Color(0xFF1A1C19),
    onBackground        = Color(0xFFE1E3DE),
    surface             = Color(0xFF1A1C19),
    onSurface           = Color(0xFFE1E3DE),
    surfaceVariant      = Color(0xFF404840),
    onSurfaceVariant    = Color(0xFFBEC9BC),
    outline             = Color(0xFF889186),
    error               = Color(0xFFFFB4AB),
    onError             = Color(0xFF690005),
)
