package com.juanpcf.caloriestracker.domain.repository

import android.graphics.Bitmap
import com.juanpcf.caloriestracker.domain.model.Food

interface FoodRepository {
    suspend fun recognizeFoodFromImage(bitmap: Bitmap): Result<Food>
    suspend fun recognizeFoodFromText(foodName: String): Result<Food>

    /** Lee la tabla nutricional del reverso de un paquete (valores exactos, por porción). */
    suspend fun recognizeFoodFromLabel(bitmap: Bitmap): Result<Food>
}
