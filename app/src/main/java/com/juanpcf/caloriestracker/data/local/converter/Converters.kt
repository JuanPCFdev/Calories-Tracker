package com.juanpcf.caloriestracker.data.local.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.juanpcf.caloriestracker.domain.model.FoodSource
import com.juanpcf.caloriestracker.domain.model.MealType
import java.time.Instant
import java.time.LocalDate

@ProvidedTypeConverter
class Converters {
    @TypeConverter fun instantToLong(v: Instant?): Long? = v?.toEpochMilli()
    @TypeConverter fun longToInstant(v: Long?): Instant? = v?.let { Instant.ofEpochMilli(it) }

    @TypeConverter fun localDateToLong(v: LocalDate?): Long? = v?.toEpochDay()
    @TypeConverter fun longToLocalDate(v: Long?): LocalDate? = v?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter fun mealTypeToString(v: MealType?): String? = v?.name
    @TypeConverter fun stringToMealType(v: String?): MealType? = v?.let { MealType.valueOf(it) }

    @TypeConverter fun foodSourceToString(v: FoodSource?): String? = v?.name
    @TypeConverter fun stringToFoodSource(v: String?): FoodSource? = v?.let { FoodSource.valueOf(it) }
}
