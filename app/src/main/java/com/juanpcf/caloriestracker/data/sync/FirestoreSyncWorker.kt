package com.juanpcf.caloriestracker.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.juanpcf.caloriestracker.data.firebase.FirestoreDiaryRepository
import com.juanpcf.caloriestracker.data.local.dao.DiaryEntryDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.Instant
import java.util.concurrent.TimeUnit

@HiltWorker
class FirestoreSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val diaryEntryDao: DiaryEntryDao,
    private val firestoreRepository: FirestoreDiaryRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME_PERIODIC  = "firestore_sync_periodic"
        const val WORK_NAME_IMMEDIATE = "firestore_sync_immediate"
        private const val MAX_RETRY_ATTEMPTS = 3

        private val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun schedulePeriodicSync(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<FirestoreSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(networkConstraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request
            )
        }

        fun scheduleImmediateSync(workManager: WorkManager) {
            val request = OneTimeWorkRequestBuilder<FirestoreSyncWorker>()
                .setConstraints(networkConstraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            workManager.enqueueUniqueWork(
                WORK_NAME_IMMEDIATE, ExistingWorkPolicy.REPLACE, request
            )
        }

        fun cancelAll(workManager: WorkManager) {
            workManager.cancelUniqueWork(WORK_NAME_PERIODIC)
            workManager.cancelUniqueWork(WORK_NAME_IMMEDIATE)
        }
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRY_ATTEMPTS) return Result.failure()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()
        return try {
            pushPendingEntries(userId)
            pullRemoteEntries(userId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun pushPendingEntries(userId: String) {
        val pending = diaryEntryDao.getEntriesNotSynced(userId)
        pending.forEach { entity ->
            firestoreRepository.writeEntry(userId, entity)
            diaryEntryDao.markAsSynced(entity.id, Instant.now().toEpochMilli())
        }
    }

    private suspend fun pullRemoteEntries(userId: String) {
        val cutoff = LocalDate.now().minusDays(30)
        val localIds = diaryEntryDao.getLocalEntryIdsForRange(userId, cutoff.toEpochDay()).toSet()
        val remote = firestoreRepository.getEntriesSince(userId, cutoff)
        remote.filter { it.id !in localIds }.forEach { diaryEntryDao.insert(it) }
    }
}
