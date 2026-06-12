package com.juanpcf.caloriestracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juanpcf.caloriestracker.data.local.entity.UserPhysicalProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPhysicalProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(profile: UserPhysicalProfileEntity)

    @Query("SELECT * FROM user_physical_profile WHERE user_id = :userId LIMIT 1")
    fun getProfile(userId: String): Flow<UserPhysicalProfileEntity?>

    @Query("SELECT * FROM user_physical_profile WHERE user_id = :userId LIMIT 1")
    suspend fun getProfileOnce(userId: String): UserPhysicalProfileEntity?
}
