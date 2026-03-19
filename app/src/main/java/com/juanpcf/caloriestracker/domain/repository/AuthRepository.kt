package com.juanpcf.caloriestracker.domain.repository

import com.juanpcf.caloriestracker.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: UserProfile?
    val authState: Flow<UserProfile?>
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun signInWithGoogle(idToken: String): Result<UserProfile>
    suspend fun registerWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun signOut()
}