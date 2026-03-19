package com.inovisec.caloriestracker.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// TODO Phase 2: add @Binds for all repository implementations after data layer is created
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule