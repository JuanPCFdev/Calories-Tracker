package com.inovisec.caloriestracker.core.di

import android.content.Context
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

// TODO Phase 2: provide Room database and DAOs after entities are created
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule