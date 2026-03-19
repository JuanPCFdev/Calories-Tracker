package com.juanpcf.caloriestracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juanpcf.caloriestracker.data.local.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiaryEntryEntity)

    @Query("DELETE FROM diary_entry WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("""
        SELECT * FROM diary_entry
        WHERE user_id = :userId AND date = :dateEpochDay
        ORDER BY created_at ASC
    """)
    fun getEntriesForDate(userId: String, dateEpochDay: Long): Flow<List<DiaryEntryEntity>>

    @Query("""
        SELECT
            SUM(calories_snapshot) AS calories,
            SUM(protein_snapshot)  AS protein,
            SUM(carbs_snapshot)    AS carbs,
            SUM(fat_snapshot)      AS fat
        FROM diary_entry
        WHERE user_id = :userId AND date = :dateEpochDay
    """)
    fun getDailyTotals(userId: String, dateEpochDay: Long): Flow<MacroTotalsProjection?>

    @Query("""
        SELECT * FROM diary_entry
        WHERE user_id = :userId AND synced_at IS NULL
        ORDER BY created_at ASC
    """)
    suspend fun getEntriesNotSynced(userId: String): List<DiaryEntryEntity>

    @Query("UPDATE diary_entry SET synced_at = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: String, syncedAt: Long)

    @Query("""
        SELECT id FROM diary_entry
        WHERE user_id = :userId AND date >= :fromDateEpochDay
    """)
    suspend fun getLocalEntryIdsForRange(userId: String, fromDateEpochDay: Long): List<String>

    @Query("""
        SELECT * FROM diary_entry
        WHERE user_id = :userId AND date >= :fromDateEpochDay AND date <= :toDateEpochDay
        ORDER BY date ASC, created_at ASC
    """)
    suspend fun getEntriesForDateRange(userId: String, fromDateEpochDay: Long, toDateEpochDay: Long): List<DiaryEntryEntity>
}
