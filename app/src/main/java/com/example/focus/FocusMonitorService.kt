package com.example.focus

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.local.FocusSessionEntity
import com.example.notifications.NotificationHelper
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusMonitorService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var timerJob: Job? = null
    private var overlayView: View? = null
    private var overlayCountdownText: TextView? = null
    private var lastOverlayShownAtMillis: Long = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FOCUS -> {
                startAsForeground()
                startTimerLoop()
            }
            ACTION_STOP_FOCUS -> {
                removeOverlay()
                timerJob?.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_SHOW_BLOCK_OVERLAY -> {
                showBlockOverlay()
            }
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        val state = FocusStateHolder.state.value
        val notification = NotificationHelper.buildFocusForegroundNotification(
            context = this,
            lessonName = state.lessonTitle,
            remainingFormatted = formatSeconds(state.remainingSeconds)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NotificationHelper.FOCUS_SERVICE_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NotificationHelper.FOCUS_SERVICE_NOTIFICATION_ID, notification)
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive && FocusStateHolder.state.value.isActive) {
                delay(1000L)
                val remaining = FocusStateHolder.tickSecond()
                val current = FocusStateHolder.state.value

                // Update foreground notification
                val notification = NotificationHelper.buildFocusForegroundNotification(
                    context = this@FocusMonitorService,
                    lessonName = current.lessonTitle,
                    remainingFormatted = formatSeconds(remaining)
                )
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                nm.notify(NotificationHelper.FOCUS_SERVICE_NOTIFICATION_ID, notification)

                // Update overlay remaining minutes if currently visible
                overlayCountdownText?.text =
                    "لسه فاضل ${current.remainingMinutesCeil} دقيقة - كمل يا بطل، حلمك مستنيك"

                // Backup foreground detection via UsageStatsManager
                checkForegroundViaUsageStats(current.blockedPackages)

                if (remaining <= 0) {
                    onTimerFinished(current)
                    break
                }
            }
        }
    }

    private fun checkForegroundViaUsageStats(blockedPackages: Set<String>) {
        if (blockedPackages.isEmpty()) return
        if (!InstalledAppsHelper.hasUsageStatsPermission(this)) return

        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val begin = end - 3500L
        val events = usm.queryEvents(begin, end) ?: return
        val event = UsageEvents.Event()
        var latestPkg: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                latestPkg = event.packageName
            }
        }
        if (latestPkg != null && latestPkg != packageName && latestPkg in blockedPackages) {
            val now = System.currentTimeMillis()
            if (now - lastOverlayShownAtMillis > 1200L) {
                showBlockOverlay()
            }
        }
    }

    private fun showBlockOverlay() {
        val current = FocusStateHolder.state.value
        if (!current.isActive) return
        lastOverlayShownAtMillis = System.currentTimeMillis()

        if (Settings.canDrawOverlays(this)) {
            Handler(Looper.getMainLooper()).post {
                if (overlayView != null) {
                    overlayCountdownText?.text =
                        "لسه فاضل ${current.remainingMinutesCeil} دقيقة - كمل يا بطل، حلمك مستنيك"
                    return@post
                }
                val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    type,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER
                }

                val container = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(64, 96, 64, 96)
                    setBackgroundColor(Color.parseColor("#FA0B132B"))
                }

                val badge = TextView(this).apply {
                    text = "🚫 وضع التركيز شغال"
                    textSize = 18f
                    setTextColor(Color.parseColor("#FFB703"))
                    gravity = Gravity.CENTER
                    setTypeface(null, Typeface.BOLD)
                }

                val mainMsg = TextView(this).apply {
                    text = "لسه فاضل ${current.remainingMinutesCeil} دقيقة - كمل يا بطل، حلمك مستنيك"
                    textSize = 24f
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, 36, 0, 24)
                }
                overlayCountdownText = mainMsg

                val lessonHint = TextView(this).apply {
                    text = "درسك الحالي: ${current.lessonTitle}"
                    textSize = 15f
                    setTextColor(Color.parseColor("#CBD5E1"))
                    gravity = Gravity.CENTER
                    setPadding(0, 0, 0, 48)
                }

                val buttonBg = GradientDrawable().apply {
                    cornerRadius = 40f
                    setColor(Color.parseColor("#FF6B2B"))
                }
                val backBtn = Button(this).apply {
                    text = "ارجع للمذاكرة"
                    textSize = 18f
                    setTextColor(Color.WHITE)
                    setTypeface(null, Typeface.BOLD)
                    background = buttonBg
                    setPadding(64, 32, 64, 32)
                    setOnClickListener {
                        removeOverlay()
                        val launchMain = Intent(this@FocusMonitorService, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        }
                        startActivity(launchMain)
                    }
                }

                container.addView(badge)
                container.addView(mainMsg)
                container.addView(lessonHint)
                container.addView(backBtn)

                runCatching {
                    wm.addView(container, params)
                    overlayView = container
                }.onFailure {
                    launchFallbackOverlayActivity()
                }
            }
        } else {
            launchFallbackOverlayActivity()
        }
    }

    private fun launchFallbackOverlayActivity() {
        val intent = Intent(this, BlockedAppOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        runCatching { startActivity(intent) }
    }

    private fun removeOverlay() {
        val view = overlayView ?: return
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        runCatching { wm.removeViewImmediate(view) }
        overlayView = null
        overlayCountdownText = null
    }

    private fun onTimerFinished(completedState: FocusRuntimeState) {
        removeOverlay()
        FocusStateHolder.completeSession()

        serviceScope.launch(Dispatchers.IO) {
            val epochDay = System.currentTimeMillis() / 86_400_000L
            AppDatabase.getInstance(applicationContext).focusSessionDao().insertSession(
                FocusSessionEntity(
                    dateEpochDay = epochDay,
                    startedAtMillis = completedState.startedAtMillis,
                    plannedDurationMinutes = completedState.plannedMinutes,
                    actualFocusedSeconds = completedState.totalSeconds,
                    lessonId = completedState.lessonId,
                    lessonName = completedState.lessonTitle,
                    subjectName = completedState.subjectName,
                    isCompleted = true,
                    quitReason = null
                )
            )
            NotificationHelper.showMotivationalQuote(
                applicationContext,
                "عاش يا بطل! خلصت ${completedState.plannedMinutes} دقيقة تركيز في ${completedState.lessonTitle} 🎉",
                notificationId = 9002
            )
        }

        val launchMain = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        runCatching { startActivity(launchMain) }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        removeOverlay()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun formatSeconds(totalSeconds: Int): String {
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", mins, secs)
    }

    companion object {
        const val ACTION_START_FOCUS = "com.example.focus.ACTION_START_FOCUS"
        const val ACTION_STOP_FOCUS = "com.example.focus.ACTION_STOP_FOCUS"
        const val ACTION_SHOW_BLOCK_OVERLAY = "com.example.focus.ACTION_SHOW_BLOCK_OVERLAY"
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"

        fun startFocus(context: Context) {
            val intent = Intent(context, FocusMonitorService::class.java).apply {
                action = ACTION_START_FOCUS
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopFocus(context: Context) {
            val intent = Intent(context, FocusMonitorService::class.java).apply {
                action = ACTION_STOP_FOCUS
            }
            context.startService(intent)
        }
    }
}
