package com.juanpcf.caloriestracker.data.repository

import com.juanpcf.caloriestracker.data.firebase.FirestoreUserRepository
import com.juanpcf.caloriestracker.data.local.dao.UserGoalsDao
import com.juanpcf.caloriestracker.data.local.entity.UserGoalsEntity
import com.juanpcf.caloriestracker.domain.model.UserGoals
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UserGoalsRepositoryImplTest {

    private val dao = mockk<UserGoalsDao>(relaxed = true)
    private val firestore = mockk<FirestoreUserRepository>()
    private val repo = UserGoalsRepositoryImpl(dao, firestore)

    private val goals = UserGoals(
        userId = "u1", dailyCalories = 2000, dailyProtein = 150, dailyCarbs = 200, dailyFat = 60
    )

    @Test
    fun `saveGoals guarda local aunque falle la escritura remota`() = runTest {
        // Room es la fuente de verdad: el fallo remoto no debe romper el guardado local.
        coEvery { firestore.writeUserGoals(any()) } throws RuntimeException("network down")

        repo.saveGoals(goals) // no debe lanzar

        coVerify(exactly = 1) { dao.insertOrReplace(any<UserGoalsEntity>()) }
    }

    @Test
    fun `saveGoals escribe local y remoto en el camino feliz`() = runTest {
        coEvery { firestore.writeUserGoals(any()) } just Runs

        repo.saveGoals(goals)

        coVerify(exactly = 1) { dao.insertOrReplace(any<UserGoalsEntity>()) }
        coVerify(exactly = 1) { firestore.writeUserGoals(goals) }
    }
}
