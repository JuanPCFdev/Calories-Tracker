package com.juanpcf.caloriestracker.data.repository

import android.content.Context
import androidx.work.WorkManager
import com.juanpcf.caloriestracker.data.local.dao.DiaryEntryDao
import com.juanpcf.caloriestracker.data.local.entity.DiaryEntryEntity
import com.juanpcf.caloriestracker.data.sync.FirestoreSyncWorker
import com.juanpcf.caloriestracker.domain.model.DiaryEntry
import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.model.FoodSource
import com.juanpcf.caloriestracker.domain.model.MacroTotals
import com.juanpcf.caloriestracker.domain.repository.DiaryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class DiaryRepositoryImpl @Inject constructor(
    private val dao: DiaryEntryDao,
    @ApplicationContext private val context: Context
) : DiaryRepository {

    override fun getEntriesForDate(userId: String, date: LocalDate): Flow<List<DiaryEntry>> =
        dao.getEntriesForDate(userId, date.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override fun getDailyTotals(userId: String, date: LocalDate): Flow<MacroTotals> =
        dao.getDailyTotals(userId, date.toEpochDay()).map { proj ->
            MacroTotals(
                calories = proj?.calories ?: 0.0,
                protein  = proj?.protein  ?: 0.0,
                carbs    = proj?.carbs    ?: 0.0,
                fat      = proj?.fat      ?: 0.0
            )
        }

    override suspend fun addEntry(entry: DiaryEntry) {
        dao.insert(entry.toEntity())
        FirestoreSyncWorker.scheduleImmediateSync(WorkManager.getInstance(context))
    }

    override suspend fun deleteEntry(entryId: String) = dao.deleteById(entryId)

    override suspend fun getEntryById(entryId: String): DiaryEntry? =
        dao.getById(entryId)?.toDomain()

    override suspend fun updateEntry(entry: DiaryEntry) {
        val existing = dao.getById(entry.id)
        val entityToSave = entry.toEntity().copy(
            createdAt = existing?.createdAt ?: entry.createdAt,
            syncedAt = null
        )
        dao.insert(entityToSave)
        FirestoreSyncWorker.scheduleImmediateSync(WorkManager.getInstance(context))
    }

    override suspend fun getEntriesForDateRange(
        userId: String, fromDate: LocalDate, toDate: LocalDate
    ): List<DiaryEntry> = dao.getEntriesForDateRange(userId, fromDate.toEpochDay(), toDate.toEpochDay())
        .map { it.toDomain() }

    private fun DiaryEntryEntity.toDomain() = DiaryEntry(
        id = id, userId = userId,
        food = Food(
            id = foodId, name = foodName, calories = caloriesSnapshot,
            protein = proteinSnapshot, carbs = carbsSnapshot, fat = fatSnapshot,
            servingSize = 1.0, servingUnit = "serving", source = FoodSource.USDA
        ),
        date = date, mealType = mealType, servings = servings,
        caloriesSnapshot = caloriesSnapshot, proteinSnapshot = proteinSnapshot,
        carbsSnapshot = carbsSnapshot, fatSnapshot = fatSnapshot, syncedAt = syncedAt,
        createdAt = createdAt
    )

    private fun DiaryEntry.toEntity() = DiaryEntryEntity(
        id = id, userId = userId, foodId = food.id, foodName = food.name,
        caloriesSnapshot = caloriesSnapshot, proteinSnapshot = proteinSnapshot,
        carbsSnapshot = carbsSnapshot, fatSnapshot = fatSnapshot,
        servings = servings, mealType = mealType, date = date,
        createdAt = createdAt, syncedAt = syncedAt
    )
}
