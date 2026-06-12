package com.juanpcf.caloriestracker.domain.model

data class Food(
    val id: String,
    val name: String,
    val brand: String? = null,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val servingSize: Double,
    val servingUnit: String,
    val barcode: String? = null,
    val source: FoodSource,
    // Confianza de la estimación de porción por IA. Efímero: solo viaja del repo a la pantalla de
    // resultado, NO se persiste en el diario.
    val confidence: EstimateConfidence? = null
)