package com.inovisec.caloriestracker.domain.model

data class UserGoals(
    val userId: String,
    val dailyCalories: Int,
    val dailyProtein: Int,
    val dailyCarbs: Int,
    val dailyFat: Int
)