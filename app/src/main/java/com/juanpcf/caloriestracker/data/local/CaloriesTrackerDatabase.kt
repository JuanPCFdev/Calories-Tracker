package com.juanpcf.caloriestracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.juanpcf.caloriestracker.data.local.converter.Converters
import com.juanpcf.caloriestracker.data.local.dao.DiaryEntryDao
import com.juanpcf.caloriestracker.data.local.dao.FoodCacheDao
import com.juanpcf.caloriestracker.data.local.dao.UserGoalsDao
import com.juanpcf.caloriestracker.data.local.entity.DiaryEntryEntity
import com.juanpcf.caloriestracker.data.local.entity.FoodCacheEntity
import com.juanpcf.caloriestracker.data.local.entity.UserGoalsEntity

@Database(
    entities = [FoodCacheEntity::class, DiaryEntryEntity::class, UserGoalsEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CaloriesTrackerDatabase : RoomDatabase() {
    abstract fun foodCacheDao(): FoodCacheDao
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun userGoalsDao(): UserGoalsDao
}
