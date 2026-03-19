package com.juanpcf.caloriestracker.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.juanpcf.caloriestracker.data.local.entity.DiaryEntryEntity
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject

class FirestoreDiaryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun userDiary(userId: String) =
        firestore.collection("users").document(userId).collection("diary")

    suspend fun writeEntry(userId: String, entity: DiaryEntryEntity) {
        val data = mapOf(
            "foodId" to entity.foodId,
            "foodName" to entity.foodName,
            "caloriesSnapshot" to entity.caloriesSnapshot,
            "proteinSnapshot" to entity.proteinSnapshot,
            "carbsSnapshot" to entity.carbsSnapshot,
            "fatSnapshot" to entity.fatSnapshot,
            "servings" to entity.servings,
            "mealType" to entity.mealType.name,
            "date" to entity.date.toEpochDay(),
            "createdAt" to entity.createdAt.toEpochMilli()
        )
        userDiary(userId).document(entity.id).set(data).await()
    }

    suspend fun getEntriesSince(userId: String, fromDate: LocalDate): List<DiaryEntryEntity> {
        val snapshot = userDiary(userId)
            .whereGreaterThanOrEqualTo("date", fromDate.toEpochDay())
            .get().await()

        return snapshot.documents.mapNotNull { doc ->
            runCatching {
                DiaryEntryEntity(
                    id = doc.id,
                    userId = userId,
                    foodId = doc.getString("foodId") ?: return@mapNotNull null,
                    foodName = doc.getString("foodName") ?: return@mapNotNull null,
                    caloriesSnapshot = doc.getDouble("caloriesSnapshot") ?: 0.0,
                    proteinSnapshot = doc.getDouble("proteinSnapshot") ?: 0.0,
                    carbsSnapshot = doc.getDouble("carbsSnapshot") ?: 0.0,
                    fatSnapshot = doc.getDouble("fatSnapshot") ?: 0.0,
                    servings = doc.getDouble("servings") ?: 1.0,
                    mealType = com.juanpcf.caloriestracker.domain.model.MealType.valueOf(
                        doc.getString("mealType") ?: "LUNCH"
                    ),
                    date = LocalDate.ofEpochDay(doc.getLong("date") ?: 0L),
                    createdAt = java.time.Instant.ofEpochMilli(doc.getLong("createdAt") ?: 0L),
                    syncedAt = java.time.Instant.now()
                )
            }.getOrNull()
        }
    }
}
