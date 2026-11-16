package com.example.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_CLASS_REMINDERS = "class_reminders"
    const val CHANNEL_MOTIVATIONAL = "motivational_quotes"
    const val CHANNEL_FOCUS_SERVICE = "focus_session"

    const val FOCUS_SERVICE_NOTIFICATION_ID = 9001

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val classChannel = NotificationChannel(
                CHANNEL_CLASS_REMINDERS,
                "مواعيد الدروس",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات قبل موعد الدرس بـ 60 دقيقة وعند بداية الحصة"
            }

            val quoteChannel = NotificationChannel(
                CHANNEL_MOTIVATIONAL,
                "إشعارات تحفيزية",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "رسائل تشجيعية كل ساعة لطلاب الثانوية العامة"
            }

            val focusChannel = NotificationChannel(
                CHANNEL_FOCUS_SERVICE,
                "وضع التركيز",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "مؤقت جلسة التركيز وحظر التطبيقات المشتتة"
            }

            manager.createNotificationChannels(listOf(classChannel, quoteChannel, focusChannel))
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showClassSixtyMinReminder(
        context: Context,
        notificationId: Int,
        subjectName: String,
        formattedTime: String,
        studentName: String
    ) {
        ensureChannels(context)
        if (!hasNotificationPermission(context)) return

        val name = studentName.ifBlank { "بطل" }
        val text = "عندك درس $subjectName الساعة $formattedTime - جهز نفسك يا $name"

        val notification = NotificationCompat.Builder(context, CHANNEL_CLASS_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("تذكير قبل الدرس بساعة ⏰")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent(context, notificationId))
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun showClassStartReminder(
        context: Context,
        notificationId: Int,
        subjectName: String
    ) {
        ensureChannels(context)
        if (!hasNotificationPermission(context)) return

        val text = "يلا قوم، درس $subjectName بدأ"

        val notification = NotificationCompat.Builder(context, CHANNEL_CLASS_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("معاد الدرس دلوقتي 🔔")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent(context, notificationId))
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun showMotivationalQuote(context: Context, quote: String, notificationId: Int = 5501) {
        ensureChannels(context)
        if (!hasNotificationPermission(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_MOTIVATIONAL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("رفيق الثانوية 💪")
            .setContentText(quote)
            .setStyle(NotificationCompat.BigTextStyle().bigText(quote))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent(context, notificationId))
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun buildFocusForegroundNotification(
        context: Context,
        lessonName: String,
        remainingFormatted: String
    ): Notification {
        ensureChannels(context)
        return NotificationCompat.Builder(context, CHANNEL_FOCUS_SERVICE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("وضع التركيز شغال — $remainingFormatted")
            .setContentText("بتذاكر: $lessonName")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(mainPendingIntent(context, FOCUS_SERVICE_NOTIFICATION_ID))
            .build()
    }

    private fun mainPendingIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
