package com.juanpcf.caloriestracker.domain.model

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }
enum class FoodSource { USDA, OPEN_FOOD_FACTS, AI }
enum class Theme { LIGHT, DARK, SYSTEM }
enum class Language(val tag: String) { EN("en"), ES("es") }

/** Sexo biológico — requerido por la fórmula Mifflin-St Jeor (constante +5 / −161). */
enum class Gender { MALE, FEMALE }

/** Confianza de la IA en la estimación de porción desde la foto. Dato efímero (no se persiste). */
enum class EstimateConfidence {
    HIGH, MEDIUM, LOW;

    companion object {
        /** Mapea el valor crudo del API (tolerante a mayúsculas/espacios/nulos). */
        fun fromApi(raw: String?): EstimateConfidence? = when (raw?.trim()?.uppercase()) {
            "HIGH" -> HIGH
            "MEDIUM" -> MEDIUM
            "LOW" -> LOW
            else -> null
        }
    }
}

/**
 * Nivel de actividad física con su multiplicador sobre el BMR para obtener el TDEE.
 * Factores estándar de la fórmula de Harris-Benedict/Mifflin.
 */
enum class ActivityLevel(val factor: Double) {
    SEDENTARY(1.2),     // poco o nada de ejercicio
    LIGHT(1.375),       // ejercicio ligero 1-3 días/semana
    MODERATE(1.55),     // ejercicio moderado 3-5 días/semana
    ACTIVE(1.725),      // ejercicio fuerte 6-7 días/semana
    VERY_ACTIVE(1.9)    // ejercicio muy fuerte / trabajo físico
}