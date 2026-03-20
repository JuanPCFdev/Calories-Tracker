package com.juanpcf.caloriestracker.core.di

import com.juanpcf.caloriestracker.BuildConfig
import com.juanpcf.caloriestracker.data.remote.openfoodfacts.OpenFoodFactsApi
import com.juanpcf.caloriestracker.data.remote.openrouter.OpenRouterApi
import com.juanpcf.caloriestracker.data.remote.usda.UsdaApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Provides @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

    @Provides @Singleton @Named("base")
    fun provideBaseOkHttpClient(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

    @Provides @Singleton @Named("openrouter")
    fun provideOpenRouterOkHttpClient(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer ${BuildConfig.OPENROUTER_API_KEY}")
                        .addHeader("HTTP-Referer", "https://inovisec.com/caloriestracker")
                        .addHeader("X-Title", "CaloriesTracker")
                        .build()
                )
            }
            .build()

    @Provides @Singleton
    fun provideOpenFoodFactsApi(@Named("base") client: OkHttpClient): OpenFoodFactsApi =
        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OpenFoodFactsApi::class.java)

    @Provides @Singleton
    fun provideUsdaApi(@Named("base") client: OkHttpClient): UsdaApi =
        Retrofit.Builder()
            .baseUrl("https://api.nal.usda.gov/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UsdaApi::class.java)

    @Provides @Singleton
    fun provideOpenRouterApi(@Named("openrouter") client: OkHttpClient): OpenRouterApi =
        Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OpenRouterApi::class.java)

    @Provides @Singleton
    @Named("usda_api_key")
    fun provideUsdaApiKey(): String = BuildConfig.USDA_API_KEY
}