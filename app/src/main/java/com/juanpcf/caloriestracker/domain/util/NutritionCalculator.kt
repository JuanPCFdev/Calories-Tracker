package com.juanpcf.caloriestracker.domain.util

import com.juanpcf.caloriestracker.domain.model.Gender
import com.juanpcf.caloriestracker.domain.model.UserPhysicalProfile
import java.time.LocalDate
import java.time.Period

/**
 * Cálculo de gasto calórico. Función pura, sin dependencias de Android — testeable.
 *
 * BMR por **Mifflin-St Jeor** (el estándar actual, más preciso que Harris-Benedict):
 *   BMR = 10*kg + 6.25*cm − 5*edad + s   (s = +5 hombre, −161 mujer)
 * TDEE = BMR * factor de actividad.
 */
object NutritionCalculator {

    /** Edad en años a partir de la fecha de nacimiento, relativa a [today]. */
    fun ageYears(birthDate: LocalDate, today: LocalDate): Int =
        Period.between(birthDate, today).years

    /** Metabolismo basal (kcal/día) por Mifflin-St Jeor. */
    fun bmr(weightKg: Double, heightCm: Double, ageYears: Int, gender: Gender): Double {
        val sex = if (gender == Gender.MALE) 5 else -161
        return 10 * weightKg + 6.25 * heightCm - 5 * ageYears + sex
    }

    /** Gasto energético total diario (kcal/día) = BMR * factor de actividad. */
    fun tdee(profile: UserPhysicalProfile, today: LocalDate): Double {
        val age = ageYears(profile.birthDate, today)
        val basal = bmr(profile.weightKg, profile.heightCm, age, profile.gender)
        return basal * profile.activityLevel.factor
    }
}
