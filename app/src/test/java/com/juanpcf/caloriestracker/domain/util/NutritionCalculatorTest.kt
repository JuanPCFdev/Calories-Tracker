package com.juanpcf.caloriestracker.domain.util

import com.juanpcf.caloriestracker.domain.model.ActivityLevel
import com.juanpcf.caloriestracker.domain.model.Gender
import com.juanpcf.caloriestracker.domain.model.UserPhysicalProfile
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class NutritionCalculatorTest {

    private val today = LocalDate.of(2026, 6, 12)

    @Test
    fun `edad calculada desde fecha de nacimiento`() {
        assertEquals(30, NutritionCalculator.ageYears(LocalDate.of(1996, 6, 12), today))
        // cumpleaños aún no llegó este año
        assertEquals(29, NutritionCalculator.ageYears(LocalDate.of(1996, 6, 13), today))
    }

    @Test
    fun `BMR Mifflin-St Jeor hombre`() {
        // 10*80 + 6.25*180 - 5*30 + 5 = 1780
        val bmr = NutritionCalculator.bmr(weightKg = 80.0, heightCm = 180.0, ageYears = 30, gender = Gender.MALE)
        assertEquals(1780.0, bmr, 0.001)
    }

    @Test
    fun `BMR Mifflin-St Jeor mujer`() {
        // 10*60 + 6.25*165 - 5*25 - 161 = 1345.25
        val bmr = NutritionCalculator.bmr(weightKg = 60.0, heightCm = 165.0, ageYears = 25, gender = Gender.FEMALE)
        assertEquals(1345.25, bmr, 0.001)
    }

    @Test
    fun `TDEE aplica el factor de actividad`() {
        val profile = UserPhysicalProfile(
            userId = "u1", heightCm = 180.0, weightKg = 80.0,
            birthDate = LocalDate.of(1996, 6, 12), // 30 años a hoy
            gender = Gender.MALE, activityLevel = ActivityLevel.MODERATE // 1.55
        )
        // BMR 1780 * 1.55 = 2759.0
        assertEquals(2759.0, NutritionCalculator.tdee(profile, today), 0.001)
    }

    @Test
    fun `TDEE sedentario es menor que muy activo`() {
        fun profile(level: ActivityLevel) = UserPhysicalProfile(
            "u1", 175.0, 70.0, LocalDate.of(1996, 6, 12), Gender.MALE, level
        )
        val sedentary = NutritionCalculator.tdee(profile(ActivityLevel.SEDENTARY), today)
        val veryActive = NutritionCalculator.tdee(profile(ActivityLevel.VERY_ACTIVE), today)
        assert(sedentary < veryActive)
    }
}
