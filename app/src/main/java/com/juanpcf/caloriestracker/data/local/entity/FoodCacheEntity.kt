package com.juanpcf.caloriestracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.model.FoodSource
import java.time.Instant

@Entity(
    tableName = "food_cache",
    indices = [
        Index(value = ["barcode"]),
        Index(value = ["source", "external_id"], unique = true),
        Index(value = ["cached_at"])
    ]
)
data class FoodCacheEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "external_id") val externalId: String,
    @ColumnInfo(name = "source") val source: FoodSource,
    @ColumnInfo(name = "barcode") val barcode: String?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "brand") val brand: String?,
    @ColumnInfo(name = "calories") val calories: Double,
    @ColumnInfo(name = "protein") val protein: Double,
    @ColumnInfo(name = "carbs") val carbs: Double,
    @ColumnInfo(name = "fat") val fat: Double,
    @ColumnInfo(name = "fiber") val fiber: Double?,
    @ColumnInfo(name = "sugar") val sugar: Double?,
    @ColumnInfo(name = "serving_size") val servingSize: Double,
    @ColumnInfo(name = "serving_unit") val servingUnit: String,
    @ColumnInfo(name = "search_query") val searchQuery: String?,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
) {
    fun toDomain() = Food(
        id = id, name = name, brand = brand, calories = calories,
        protein = protein, carbs = carbs, fat = fat, fiber = fiber, sugar = sugar,
        servingSize = servingSize, servingUnit = servingUnit, barcode = barcode, source = source
    )

    companion object {
        fun fromDomain(food: Food, searchQuery: String?, cachedAt: Long) = FoodCacheEntity(
            id = food.id, externalId = food.id, source = food.source, barcode = food.barcode,
            name = food.name, brand = food.brand, calories = food.calories, protein = food.protein,
            carbs = food.carbs, fat = food.fat, fiber = food.fiber, sugar = food.sugar,
            servingSize = food.servingSize, servingUnit = food.servingUnit,
            searchQuery = searchQuery, cachedAt = Instant.ofEpochMilli(cachedAt)
        )
    }
}
