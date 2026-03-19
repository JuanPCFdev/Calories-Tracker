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
    val source: FoodSource
)