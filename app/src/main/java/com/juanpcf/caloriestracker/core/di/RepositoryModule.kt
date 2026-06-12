package com.juanpcf.caloriestracker.core.di

import com.juanpcf.caloriestracker.data.firebase.FirebaseAuthRepositoryImpl
import com.juanpcf.caloriestracker.data.remote.openrouter.OpenRouterApi
import com.juanpcf.caloriestracker.data.repository.AppPreferencesRepositoryImpl
import com.juanpcf.caloriestracker.data.repository.DiaryRepositoryImpl
import com.juanpcf.caloriestracker.data.repository.FoodRepositoryImpl
import com.juanpcf.caloriestracker.data.repository.UserGoalsRepositoryImpl
import com.juanpcf.caloriestracker.data.repository.UserPhysicalProfileRepositoryImpl
import com.juanpcf.caloriestracker.domain.repository.AppPreferencesRepository
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import com.juanpcf.caloriestracker.domain.repository.DiaryRepository
import com.juanpcf.caloriestracker.domain.repository.FoodRepository
import com.juanpcf.caloriestracker.domain.repository.UserGoalsRepository
import com.juanpcf.caloriestracker.domain.repository.UserPhysicalProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton abstract fun bindAuthRepository(impl: FirebaseAuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindDiaryRepository(impl: DiaryRepositoryImpl): DiaryRepository
    @Binds @Singleton abstract fun bindUserGoalsRepository(impl: UserGoalsRepositoryImpl): UserGoalsRepository
    @Binds @Singleton abstract fun bindUserPhysicalProfileRepository(impl: UserPhysicalProfileRepositoryImpl): UserPhysicalProfileRepository
    @Binds @Singleton abstract fun bindAppPreferencesRepository(impl: AppPreferencesRepositoryImpl): AppPreferencesRepository

    companion object {
        @Provides @Singleton
        fun provideFoodRepository(
            openRouterApi: OpenRouterApi,
            preferencesRepository: AppPreferencesRepository
        ): FoodRepository = FoodRepositoryImpl(openRouterApi, preferencesRepository)
    }
}
