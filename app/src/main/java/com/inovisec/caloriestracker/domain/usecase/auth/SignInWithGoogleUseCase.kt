package com.inovisec.caloriestracker.domain.usecase.auth

import com.inovisec.caloriestracker.domain.model.UserProfile
import com.inovisec.caloriestracker.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<UserProfile> =
        repository.signInWithGoogle(idToken)
}