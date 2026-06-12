package com.juanpcf.caloriestracker.domain.repository

import com.juanpcf.caloriestracker.domain.model.UserPhysicalProfile
import kotlinx.coroutines.flow.Flow

interface UserPhysicalProfileRepository {
    fun getProfile(userId: String): Flow<UserPhysicalProfile?>
    suspend fun getProfileOnce(userId: String): UserPhysicalProfile?
    suspend fun saveProfile(profile: UserPhysicalProfile)
}
