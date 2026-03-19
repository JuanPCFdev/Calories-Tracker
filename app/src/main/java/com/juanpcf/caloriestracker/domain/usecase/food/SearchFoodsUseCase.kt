package com.juanpcf.caloriestracker.domain.usecase.food

import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchFoodsUseCase @Inject constructor(private val repository: FoodRepository) {
    operator fun invoke(query: String): Flow<Result<List<Food>>> = repository.searchFoods(query)
}