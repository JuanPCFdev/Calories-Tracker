package com.juanpcf.caloriestracker.domain.model

import java.time.LocalDate

/**
 * Perfil físico del usuario para calcular el gasto calórico (BMR/TDEE). Solo métrico.
 * Patrón de persistencia espejo de [UserGoals]: Room fuente de verdad + Firestore best-effort.
 */
data class UserPhysicalProfile(
    val userId: String,
    val heightCm: Double,
    val weightKg: Double,
    val birthDate: LocalDate,
    val gender: Gender,
    val activityLevel: ActivityLevel
)
