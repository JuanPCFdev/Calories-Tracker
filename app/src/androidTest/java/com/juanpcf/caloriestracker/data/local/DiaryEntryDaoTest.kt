package com.juanpcf.caloriestracker.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.juanpcf.caloriestracker.data.local.converter.Converters
import com.juanpcf.caloriestracker.data.local.dao.DiaryEntryDao
import com.juanpcf.caloriestracker.data.local.entity.DiaryEntryEntity
import com.juanpcf.caloriestracker.domain.model.MealType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DiaryEntryDaoTest {

    private lateinit var db: CaloriesTrackerDatabase
    private lateinit var dao: DiaryEntryDao

    private val userId = "u1"
    private val date = LocalDate.of(2026, 1, 1)

    private fun entry(id: String) = DiaryEntryEntity(
        id = id, userId = userId, foodId = "f1", foodName = "Pollo",
        caloriesSnapshot = 100.0, proteinSnapshot = 20.0, carbsSnapshot = 0.0, fatSnapshot = 5.0,
        servings = 1.0, mealType = MealType.LUNCH, date = date,
        createdAt = Instant.ofEpochMilli(1_000_000), syncedAt = Instant.ofEpochMilli(2_000_000)
    )

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CaloriesTrackerDatabase::class.java
        ).addTypeConverter(Converters()).build()
        dao = db.diaryEntryDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun softDelete_ocultaDeLasLecturasPeroQuedaPendienteDeSync() = runTest {
        dao.insert(entry("e1"))

        dao.softDeleteById("e1")

        // No aparece en la lectura del día.
        assertTrue(dao.getEntriesForDate(userId, date.toEpochDay()).first().isEmpty())

        // Pero el worker la ve como pendiente (synced_at = NULL) y marcada como borrada.
        val pending = dao.getEntriesNotSynced(userId)
        assertEquals(1, pending.size)
        assertTrue(pending.first().isDeleted)
        assertNull(pending.first().syncedAt)
    }

    @Test
    fun hardDelete_eliminaFisicamente() = runTest {
        dao.insert(entry("e1"))
        dao.softDeleteById("e1")

        dao.hardDeleteById("e1")

        assertNull(dao.getById("e1"))
        assertTrue(dao.getEntriesNotSynced(userId).isEmpty())
    }

    @Test
    fun entradaActiva_apareceEnLecturas() = runTest {
        dao.insert(entry("e1"))
        assertEquals(1, dao.getEntriesForDate(userId, date.toEpochDay()).first().size)
    }
}
