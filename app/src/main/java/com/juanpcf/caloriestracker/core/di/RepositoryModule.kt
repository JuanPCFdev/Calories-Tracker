package com.juanpcf.caloriestracker.core.di

import com.juanpcf.caloriestracker.data.firebase.FirebaseAuthRepositoryImpl
import com.juanpcf.caloriestracker.data.local.dao.FoodCacheDao
import com.juanpcf.caloriestracker.data.remote.openfoodfacts.OpenFoodFactsApi
import com.juanpcf.caloriestracker.data.remote.openrouter.OpenRouterApi
import com.juanpcf.caloriestracker.data.remote.usda.UsdaApi
import com.juanpcf.caloriestracker.data.repository.AppPreferencesRepositoryImpl
import com.juanpcf.caloriestracker.data.repository.DiaryRepositoryImpl
import com.juanpcf.caloriestracker.data.repository.FoodRepositoryImpl
import com.juanpcf.caloriestracker.data.repository.UserGoalsRepositoryImpl
import com.juanpcf.caloriestracker.domain.repository.AppPreferencesRepository
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import com.juanpcf.caloriestracker.domain.repository.DiaryRepository
import com.juanpcf.caloriestracker.domain.repository.FoodRepository
import com.juanpcf.caloriestracker.domain.repository.UserGoalsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton abstract fun bindAuthRepository(impl: FirebaseAuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindDiaryRepository(impl: DiaryRepositoryImpl): DiaryRepository
    @Binds @Singleton abstract fun bindUserGoalsRepository(impl: UserGoalsRepositoryImpl): UserGoalsRepository
    @Binds @Singleton abstract fun bindAppPreferencesRepository(impl: AppPreferencesRepositoryImpl): AppPreferencesRepository

    companion object {
        @Provides @Singleton
        fun provideFoodRepository(
            foodCacheDao: FoodCacheDao,
            usdaApi: UsdaApi,
            openFoodFactsApi: OpenFoodFactsApi,
            openRouterApi: OpenRouterApi,
            @Named("usda_api_key") usdaApiKey: String
        ): FoodRepository = FoodRepositoryImpl(foodCacheDao, usdaApi, openFoodFactsApi, openRouterApi, usdaApiKey)
    }
}
