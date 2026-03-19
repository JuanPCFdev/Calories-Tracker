package com.juanpcf.caloriestracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "user_goals",
    indices = [Index(value = ["user_id"], unique = true)]
)
data class UserGoalsEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "daily_calories") val dailyCalories: Int,
    @ColumnInfo(name = "daily_protein") val dailyProtein: Int,
    @ColumnInfo(name = "daily_carbs") val dailyCarbs: Int,
    @ColumnInfo(name = "daily_fat") val dailyFat: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
)
