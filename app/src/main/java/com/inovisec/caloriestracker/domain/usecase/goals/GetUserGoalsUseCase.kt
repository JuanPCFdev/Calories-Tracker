package com.inovisec.caloriestracker.domain.usecase.goals

import com.inovisec.caloriestracker.domain.model.UserGoals
import com.inovisec.caloriestracker.domain.repository.UserGoalsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserGoalsUseCase @Inject constructor(private val repository: UserGoalsRepository) {
    operator fun invoke(userId: String): Flow<UserGoals?> = repository.getGoals(userId)
}