package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClassReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subjectName = intent.getStringExtra(ClassAlarmScheduler.EXTRA_SUBJECT_NAME) ?: return
        val formattedTime = intent.getStringExtra(ClassAlarmScheduler.EXTRA_FORMATTED_TIME) ?: ""
        val isStartAlarm = intent.getBooleanExtra(ClassAlarmScheduler.EXTRA_IS_START_ALARM, false)
        val classId = intent.getLongExtra(ClassAlarmScheduler.EXTRA_CLASS_ID, 0L)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesRepository(context)
                val profile = prefs.getProfileOnce()
                val notifId = ((classId * 10) + if (isStartAlarm) 2 else 1).toInt()
                if (isStartAlarm) {
                    NotificationHelper.showClassStartReminder(
                        context = context,
                        notificationId = notifId,
                        subjectName = subjectName
                    )
                } else {
                    NotificationHelper.showClassSixtyMinReminder(
                        context = context,
                        notificationId = notifId,
                        subjectName = subjectName,
                        formattedTime = formattedTime,
                        studentName = profile.name
                    )
                }

                // Re-schedule next week's occurrence
                val classes = AppDatabase.getInstance(context).scheduleDao().getAllClassesOnce()
                ClassAlarmScheduler.rescheduleAll(context, classes)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
