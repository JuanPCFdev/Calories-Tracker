package com.juanpcf.caloriestracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.juanpcf.caloriestracker.domain.model.MealType
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "diary_entry",
    indices = [
        Index(value = ["user_id", "date"]),
        Index(value = ["user_id", "synced_at"])
    ]
)
data class DiaryEntryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "food_id") val foodId: String,
    @ColumnInfo(name = "food_name") val foodName: String,
    @ColumnInfo(name = "calories_snapshot") val caloriesSnapshot: Double,
    @ColumnInfo(name = "protein_snapshot") val proteinSnapshot: Double,
    @ColumnInfo(name = "carbs_snapshot") val carbsSnapshot: Double,
    @ColumnInfo(name = "fat_snapshot") val fatSnapshot: Double,
    @ColumnInfo(name = "servings") val servings: Double,
    @ColumnInfo(name = "meal_type") val mealType: MealType,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "synced_at") val syncedAt: Instant?
)
