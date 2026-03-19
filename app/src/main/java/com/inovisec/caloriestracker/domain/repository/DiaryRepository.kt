package com.inovisec.caloriestracker.domain.repository

import com.inovisec.caloriestracker.domain.model.DiaryEntry
import com.inovisec.caloriestracker.domain.model.MacroTotals
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DiaryRepository {
    fun getEntriesForDate(userId: String, date: LocalDate): Flow<List<DiaryEntry>>
    fun getDailyTotals(userId: String, date: LocalDate): Flow<MacroTotals>
    suspend fun addEntry(entry: DiaryEntry)
    suspend fun deleteEntry(entryId: String)
    suspend fun getEntriesForDateRange(userId: String, fromDate: LocalDate, toDate: LocalDate): List<DiaryEntry>
}