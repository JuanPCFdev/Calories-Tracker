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

    /**
     * Soft-delete: marca la entrada como borrada y pendiente de sincronizar. El [com.juanpcf.caloriestracker.data.sync.FirestoreSyncWorker]
     * la borra del remoto y recién ahí hace [hardDeleteById]. No usar DELETE directo desde la UI:
     * un borrado local sin propagar se resucita en el próximo pull.
     */
    @Query("UPDATE diary_entry SET is_deleted = 1, synced_at = NULL WHERE id = :id")
    suspend fun softDeleteById(id: String)

    /** Borrado físico definitivo. Solo el worker lo llama tras confirmar el borrado remoto. */
    @Query("DELETE FROM diary_entry WHERE id = :id")
    suspend fun hardDeleteById(id: String)

    @Query("SELECT * FROM diary_entry WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): DiaryEntryEntity?

    @Query("""
        SELECT * FROM diary_entry
        WHERE user_id = :userId AND date = :dateEpochDay AND is_deleted = 0
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
        WHERE user_id = :userId AND date = :dateEpochDay AND is_deleted = 0
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
        WHERE user_id = :userId AND date >= :fromDateEpochDay AND date <= :toDateEpochDay AND is_deleted = 0
        ORDER BY date ASC, created_at ASC
    """)
    suspend fun getEntriesForDateRange(userId: String, fromDateEpochDay: Long, toDateEpochDay: Long): List<DiaryEntryEntity>
}
