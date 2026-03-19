package com.juanpcf.caloriestracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juanpcf.caloriestracker.data.local.entity.UserGoalsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserGoalsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(goals: UserGoalsEntity)

    @Query("SELECT * FROM user_goals WHERE user_id = :userId LIMIT 1")
    fun getGoals(userId: String): Flow<UserGoalsEntity?>

    @Query("SELECT * FROM user_goals WHERE user_id = :userId LIMIT 1")
    suspend fun getGoalsOnce(userId: String): UserGoalsEntity?
}
