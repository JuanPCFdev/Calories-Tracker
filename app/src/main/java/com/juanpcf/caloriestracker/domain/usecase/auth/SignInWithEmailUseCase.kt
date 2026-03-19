package com.juanpcf.caloriestracker.domain.usecase.auth

import com.juanpcf.caloriestracker.domain.model.UserProfile
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithEmailUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<UserProfile> =
        repository.signInWithEmail(email, password)
}