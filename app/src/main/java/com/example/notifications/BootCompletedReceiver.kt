package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val classes = AppDatabase.getInstance(context).scheduleDao().getAllClassesOnce()
                ClassAlarmScheduler.rescheduleAll(context, classes)

                val prefs = UserPreferencesRepository(context)
                if (prefs.isMotivationalNotificationsEnabledOnce()) {
                    MotivationalQuoteWorker.scheduleHourly(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
