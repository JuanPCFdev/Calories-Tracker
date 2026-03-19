package com.inovisec.caloriestracker.domain.repository

import com.inovisec.caloriestracker.domain.model.UserGoals
import kotlinx.coroutines.flow.Flow

interface UserGoalsRepository {
    fun getGoals(userId: String): Flow<UserGoals?>
    suspend fun getGoalsOnce(userId: String): UserGoals?
    suspend fun saveGoals(goals: UserGoals)
}