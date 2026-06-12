package com.juanpcf.caloriestracker.data.repository

import com.juanpcf.caloriestracker.data.firebase.FirestoreUserRepository
import com.juanpcf.caloriestracker.data.local.dao.UserGoalsDao
import com.juanpcf.caloriestracker.data.local.entity.UserGoalsEntity
import com.juanpcf.caloriestracker.domain.model.UserGoals
import com.juanpcf.caloriestracker.domain.repository.UserGoalsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class UserGoalsRepositoryImpl @Inject constructor(
    private val dao: UserGoalsDao,
    private val firestoreUserRepository: FirestoreUserRepository
) : UserGoalsRepository {

    override fun getGoals(userId: String): Flow<UserGoals?> =
        dao.getGoals(userId).map { it?.toDomain() }

    override suspend fun getGoalsOnce(userId: String): UserGoals? =
        dao.getGoalsOnce(userId)?.toDomain()

    override suspend fun saveGoals(goals: UserGoals) {
        // Room es la fuente de verdad: el guardado local manda. La escritura remota es un espejo
        // best-effort; si falla, se loggea (non-fatal a Crashlytics) pero no se rompe el guardado.
        dao.insertOrReplace(goals.toEntity())
        runCatching { firestoreUserRepository.writeUserGoals(goals) }
            .onFailure { Timber.e(it, "No se pudieron escribir los objetivos en Firestore para uid=${goals.userId}") }
    }

    private fun UserGoalsEntity.toDomain() = UserGoals(
        userId = userId, dailyCalories = dailyCalories,
        dailyProtein = dailyProtein, dailyCarbs = dailyCarbs, dailyFat = dailyFat,
        dailySugar = dailySugar
    )

    private fun UserGoals.toEntity() = UserGoalsEntity(
        id = UUID.randomUUID().toString(), userId = userId,
        dailyCalories = dailyCalories, dailyProtein = dailyProtein,
        dailyCarbs = dailyCarbs, dailyFat = dailyFat, dailySugar = dailySugar,
        updatedAt = Instant.now()
    )
}
