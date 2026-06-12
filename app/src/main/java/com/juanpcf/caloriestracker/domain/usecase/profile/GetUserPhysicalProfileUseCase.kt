package com.juanpcf.caloriestracker.domain.usecase.profile

import com.juanpcf.caloriestracker.domain.model.UserPhysicalProfile
import com.juanpcf.caloriestracker.domain.repository.UserPhysicalProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserPhysicalProfileUseCase @Inject constructor(
    private val repository: UserPhysicalProfileRepository
) {
    operator fun invoke(userId: String): Flow<UserPhysicalProfile?> = repository.getProfile(userId)
}
