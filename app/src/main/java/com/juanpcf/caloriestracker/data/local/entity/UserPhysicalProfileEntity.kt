package com.juanpcf.caloriestracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.juanpcf.caloriestracker.domain.model.ActivityLevel
import com.juanpcf.caloriestracker.domain.model.Gender
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "user_physical_profile",
    indices = [Index(value = ["user_id"], unique = true)]
)
data class UserPhysicalProfileEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "height_cm") val heightCm: Double,
    @ColumnInfo(name = "weight_kg") val weightKg: Double,
    @ColumnInfo(name = "birth_date") val birthDate: LocalDate,
    @ColumnInfo(name = "gender") val gender: Gender,
    @ColumnInfo(name = "activity_level") val activityLevel: ActivityLevel,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
)
