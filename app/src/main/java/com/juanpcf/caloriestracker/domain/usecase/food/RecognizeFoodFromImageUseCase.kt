package com.juanpcf.caloriestracker.domain.usecase.food

import android.graphics.Bitmap
import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.repository.FoodRepository
import javax.inject.Inject

class RecognizeFoodFromImageUseCase @Inject constructor(private val repository: FoodRepository) {
    suspend operator fun invoke(bitmap: Bitmap): Result<Food> = repository.recognizeFoodFromImage(bitmap)
}