package com.inovisec.caloriestracker.domain.model

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }
enum class FoodSource { USDA, OPEN_FOOD_FACTS, AI }
enum class Theme { LIGHT, DARK, SYSTEM }
enum class Language(val tag: String) { EN("en"), ES("es") }