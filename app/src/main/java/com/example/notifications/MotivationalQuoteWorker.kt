package com.example.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.UserPreferencesRepository
import com.example.data.quotes.QuotesRepository
import java.util.concurrent.TimeUnit

class MotivationalQuoteWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = UserPreferencesRepository(applicationContext)
        val enabled = prefs.isMotivationalNotificationsEnabledOnce()
        if (!enabled) return Result.success()

        val profile = prefs.getProfileOnce()
        val quote = QuotesRepository.getRandomQuote(profile.name)
        NotificationHelper.showMotivationalQuote(applicationContext, quote)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "rafiq_hourly_motivational_quote"

        fun scheduleHourly(context: Context) {
            val request = PeriodicWorkRequestBuilder<MotivationalQuoteWorker>(
                60, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }
}
