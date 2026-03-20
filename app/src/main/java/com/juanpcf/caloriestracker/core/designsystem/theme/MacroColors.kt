package com.juanpcf.caloriestracker.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Canonical color assignments for macro nutrients.
 * Uses plain Color values (NOT MaterialTheme.colorScheme.*) so they are safe
 * to reference from Canvas DrawScope (non-@Composable context).
 *
 * Hex values match the brand palette defined in ui/theme/Color.kt:
 *   Green40  = Primary   (#4CAF50)
 *   Blue40   = Secondary (#4A90E2)
 *   Orange40 = Tertiary  (#FF9800)
 */
object MacroColors {
    val protein: Color = Color(0xFF4CAF50)   // Green — Primary
    val carbs: Color   = Color(0xFF4A90E2)   // Blue  — Secondary
    val fat: Color     = Color(0xFFFF9800)   // Orange — Tertiary

    // Track colors (low-opacity versions for ring/bar backgrounds)
    val proteinTrack: Color = Color(0xFF4CAF50).copy(alpha = 0.15f)
    val carbsTrack: Color   = Color(0xFF4A90E2).copy(alpha = 0.15f)
    val fatTrack: Color     = Color(0xFFFF9800).copy(alpha = 0.15f)
}
