package com.juanpcf.caloriestracker.domain.usecase.goals

import com.juanpcf.caloriestracker.domain.model.UserGoals
import com.juanpcf.caloriestracker.domain.repository.UserGoalsRepository
import javax.inject.Inject

class SaveUserGoalsUseCase @Inject constructor(private val repository: UserGoalsRepository) {
    suspend operator fun invoke(goals: UserGoals) = repository.saveGoals(goals)
}