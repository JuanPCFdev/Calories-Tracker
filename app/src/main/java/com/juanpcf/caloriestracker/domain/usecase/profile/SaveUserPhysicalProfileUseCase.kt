package com.juanpcf.caloriestracker.domain.usecase.profile

import com.juanpcf.caloriestracker.domain.model.UserPhysicalProfile
import com.juanpcf.caloriestracker.domain.repository.UserPhysicalProfileRepository
import javax.inject.Inject

class SaveUserPhysicalProfileUseCase @Inject constructor(
    private val repository: UserPhysicalProfileRepository
) {
    suspend operator fun invoke(profile: UserPhysicalProfile) = repository.saveProfile(profile)
}
