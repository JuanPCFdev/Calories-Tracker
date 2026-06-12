package com.juanpcf.caloriestracker.data.repository

import com.juanpcf.caloriestracker.data.firebase.FirestoreUserRepository
import com.juanpcf.caloriestracker.data.local.dao.UserPhysicalProfileDao
import com.juanpcf.caloriestracker.data.local.entity.UserPhysicalProfileEntity
import com.juanpcf.caloriestracker.domain.model.UserPhysicalProfile
import com.juanpcf.caloriestracker.domain.repository.UserPhysicalProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class UserPhysicalProfileRepositoryImpl @Inject constructor(
    private val dao: UserPhysicalProfileDao,
    private val firestoreUserRepository: FirestoreUserRepository
) : UserPhysicalProfileRepository {

    override fun getProfile(userId: String): Flow<UserPhysicalProfile?> =
        dao.getProfile(userId).map { it?.toDomain() }

    override suspend fun getProfileOnce(userId: String): UserPhysicalProfile? =
        dao.getProfileOnce(userId)?.toDomain()

    override suspend fun saveProfile(profile: UserPhysicalProfile) {
        // Room es la fuente de verdad; la escritura remota es un espejo best-effort.
        dao.insertOrReplace(profile.toEntity())
        runCatching { firestoreUserRepository.writeUserPhysicalProfile(profile) }
            .onFailure { Timber.e(it, "No se pudo escribir el perfil físico en Firestore para uid=${profile.userId}") }
    }

    private fun UserPhysicalProfileEntity.toDomain() = UserPhysicalProfile(
        userId = userId, heightCm = heightCm, weightKg = weightKg,
        birthDate = birthDate, gender = gender, activityLevel = activityLevel
    )

    private fun UserPhysicalProfile.toEntity() = UserPhysicalProfileEntity(
        id = UUID.randomUUID().toString(), userId = userId,
        heightCm = heightCm, weightKg = weightKg, birthDate = birthDate,
        gender = gender, activityLevel = activityLevel, updatedAt = Instant.now()
    )
}
