package com.juanpcf.caloriestracker.domain.usecase.food

import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.repository.FoodRepository
import javax.inject.Inject

class GetFoodByBarcodeUseCase @Inject constructor(private val repository: FoodRepository) {
    suspend operator fun invoke(barcode: String): Result<Food> = repository.getFoodByBarcode(barcode)
}