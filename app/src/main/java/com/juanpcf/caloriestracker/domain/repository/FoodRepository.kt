package com.juanpcf.caloriestracker.domain.repository

import android.graphics.Bitmap
import com.juanpcf.caloriestracker.domain.model.Food
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun searchFoods(query: String): Flow<Result<List<Food>>>
    suspend fun getFoodByBarcode(barcode: String): Result<Food>
    suspend fun recognizeFoodFromImage(bitmap: Bitmap): Result<Food>
    suspend fun getFoodById(id: String): Food?
}