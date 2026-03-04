package io.inventor.opelet.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.inventor.opelet.data.AppRepository
import io.inventor.opelet.data.OpeletDatabase
import io.inventor.opelet.network.GitHubApi
import java.util.concurrent.TimeUnit

/**
 * Periodically checks for updates to all tracked apps.
 * Updates the database; the UI observes via Flow and reflects changes.
 * No notifications in MVP.
 */
class UpdateWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = OpeletDatabase.get(applicationContext)
        val repo = AppRepository(db.trackedAppDao(), GitHubApi())

        val results = repo.refreshAll()
        val anyFailed = results.any { it.isFailure }

        return if (anyFailed) Result.retry() else Result.success()
    }

    companion object {
        private const val WORK_NAME = "opelet_update_check"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<UpdateWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
            ).setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request,
                )
        }
    }
}
