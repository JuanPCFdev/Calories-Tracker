package com.inovisec.caloriestracker.domain.usecase.auth

import com.inovisec.caloriestracker.domain.model.UserProfile
import com.inovisec.caloriestracker.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterWithEmailUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<UserProfile> =
        repository.registerWithEmail(email, password)
}