package com.juanpcf.caloriestracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juanpcf.caloriestracker.data.local.entity.FoodCacheEntity

@Dao
interface FoodCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FoodCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FoodCacheEntity>)

    @Query("SELECT * FROM food_cache WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): FoodCacheEntity?

    @Query("SELECT * FROM food_cache WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FoodCacheEntity?

    @Query("""
        SELECT * FROM food_cache
        WHERE search_query = :query AND cached_at > :minAge
        ORDER BY cached_at DESC
    """)
    suspend fun getCachedResults(query: String, minAge: Long): List<FoodCacheEntity>

    @Query("DELETE FROM food_cache WHERE cached_at < :cutoffTime")
    suspend fun deleteExpired(cutoffTime: Long)
}
