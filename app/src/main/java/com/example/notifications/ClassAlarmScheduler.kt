package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.ScheduleClassEntity
import com.example.data.model.DayOfWeekAr
import java.util.Calendar
import java.util.Locale

object ClassAlarmScheduler {

    const val EXTRA_SUBJECT_NAME = "extra_subject_name"
    const val EXTRA_FORMATTED_TIME = "extra_formatted_time"
    const val EXTRA_IS_START_ALARM = "extra_is_start_alarm"
    const val EXTRA_CLASS_ID = "extra_class_id"

    fun rescheduleAll(context: Context, classes: List<ScheduleClassEntity>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel previously scheduled slots for safety (up to class ids)
        classes.forEach { item ->
            cancelPair(context, alarmManager, item.id)
        }

        classes.forEach { item ->
            scheduleClassAlarms(context, alarmManager, item)
        }
    }

    fun cancelForClass(context: Context, classId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancelPair(context, alarmManager, classId)
    }

    private fun cancelPair(context: Context, alarmManager: AlarmManager, classId: Long) {
        val sixtyReq = (classId * 10 + 1).toInt()
        val startReq = (classId * 10 + 2).toInt()
        alarmManager.cancel(buildPendingIntent(context, sixtyReq, classId, "", "", false))
        alarmManager.cancel(buildPendingIntent(context, startReq, classId, "", "", true))
    }

    private fun scheduleClassAlarms(
        context: Context,
        alarmManager: AlarmManager,
        item: ScheduleClassEntity
    ) {
        val formattedTime = formatArabicTime(item.startHour, item.startMinute)
        val startTrigger = nextOccurrenceMillis(
            dayOfWeekCalendar = DayOfWeekAr.fromIndex(item.dayOfWeekIndex).calendarDay,
            hour = item.startHour,
            minute = item.startMinute
        )

        val sixtyMinBeforeTrigger = startTrigger - 60L * 60L * 1000L
        val now = System.currentTimeMillis()
        val effectiveSixtyTrigger = if (sixtyMinBeforeTrigger <= now) {
            sixtyMinBeforeTrigger + 7L * 24L * 60L * 60L * 1000L
        } else {
            sixtyMinBeforeTrigger
        }

        val sixtyPi = buildPendingIntent(
            context = context,
            requestCode = (item.id * 10 + 1).toInt(),
            classId = item.id,
            subjectName = item.subjectName,
            formattedTime = formattedTime,
            isStartAlarm = false
        )

        val startPi = buildPendingIntent(
            context = context,
            requestCode = (item.id * 10 + 2).toInt(),
            classId = item.id,
            subjectName = item.subjectName,
            formattedTime = formattedTime,
            isStartAlarm = true
        )

        scheduleExactOrFallback(alarmManager, effectiveSixtyTrigger, sixtyPi)
        scheduleExactOrFallback(alarmManager, startTrigger, startPi)
    }

    private fun scheduleExactOrFallback(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun buildPendingIntent(
        context: Context,
        requestCode: Int,
        classId: Long,
        subjectName: String,
        formattedTime: String,
        isStartAlarm: Boolean
    ): PendingIntent {
        val intent = Intent(context, ClassReminderReceiver::class.java).apply {
            putExtra(EXTRA_CLASS_ID, classId)
            putExtra(EXTRA_SUBJECT_NAME, subjectName)
            putExtra(EXTRA_FORMATTED_TIME, formattedTime)
            putExtra(EXTRA_IS_START_ALARM, isStartAlarm)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun nextOccurrenceMillis(dayOfWeekCalendar: Int, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeekCalendar)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    fun formatArabicTime(hour24: Int, minute: Int): String {
        val period = if (hour24 < 12) "ص" else "م"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return String.format(Locale("ar"), "%d:%02d %s", hour12, minute, period)
    }
}
