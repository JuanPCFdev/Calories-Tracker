package com.juanpcf.caloriestracker.domain.usecase.auth

import com.juanpcf.caloriestracker.domain.model.UserProfile
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<UserProfile> =
        repository.signInWithGoogle(idToken)
}