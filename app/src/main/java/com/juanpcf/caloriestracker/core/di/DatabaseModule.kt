package com.juanpcf.caloriestracker.core.di

import android.content.Context
import androidx.room.Room
import com.juanpcf.caloriestracker.data.local.CaloriesTrackerDatabase
import com.juanpcf.caloriestracker.data.local.converter.Converters
import com.juanpcf.caloriestracker.data.local.dao.DiaryEntryDao
import com.juanpcf.caloriestracker.data.local.dao.FoodCacheDao
import com.juanpcf.caloriestracker.data.local.dao.UserGoalsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CaloriesTrackerDatabase =
        Room.databaseBuilder(context, CaloriesTrackerDatabase::class.java, "calories_tracker.db")
            .addTypeConverter(Converters())
            .build()

    @Provides @Singleton
    fun provideFoodCacheDao(db: CaloriesTrackerDatabase): FoodCacheDao = db.foodCacheDao()

    @Provides @Singleton
    fun provideDiaryEntryDao(db: CaloriesTrackerDatabase): DiaryEntryDao = db.diaryEntryDao()

    @Provides @Singleton
    fun provideUserGoalsDao(db: CaloriesTrackerDatabase): UserGoalsDao = db.userGoalsDao()
}
