package com.inovisec.caloriestracker.domain.usecase.goals

import com.inovisec.caloriestracker.domain.model.UserGoals
import com.inovisec.caloriestracker.domain.repository.UserGoalsRepository
import javax.inject.Inject

class SaveUserGoalsUseCase @Inject constructor(private val repository: UserGoalsRepository) {
    suspend operator fun invoke(goals: UserGoals) = repository.saveGoals(goals)
}