package com.inovisec.caloriestracker.domain.usecase.food

import com.inovisec.caloriestracker.domain.model.Food
import com.inovisec.caloriestracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchFoodsUseCase @Inject constructor(private val repository: FoodRepository) {
    operator fun invoke(query: String): Flow<Result<List<Food>>> = repository.searchFoods(query)
}