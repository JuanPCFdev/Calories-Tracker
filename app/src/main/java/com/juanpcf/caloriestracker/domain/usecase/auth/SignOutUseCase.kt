package com.juanpcf.caloriestracker.domain.usecase.auth

import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.signOut()
}