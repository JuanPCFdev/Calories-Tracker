package com.inovisec.caloriestracker.domain.usecase.food

import android.graphics.Bitmap
import com.inovisec.caloriestracker.domain.model.Food
import com.inovisec.caloriestracker.domain.repository.FoodRepository
import javax.inject.Inject

class RecognizeFoodFromImageUseCase @Inject constructor(private val repository: FoodRepository) {
    suspend operator fun invoke(bitmap: Bitmap): Result<Food> = repository.recognizeFoodFromImage(bitmap)
}