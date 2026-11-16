package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.curriculum.CurriculumData
import com.example.data.local.AppDatabase
import com.example.data.local.FocusSessionEntity
import com.example.data.local.ImportResult
import com.example.data.local.LessonProgressEntity
import com.example.data.local.ScheduleClassEntity
import com.example.data.local.StudyRepository
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.ClassPlace
import com.example.data.model.CurriculumLesson
import com.example.data.model.DayOfWeekAr
import com.example.data.model.InstalledAppInfo
import com.example.data.model.StudyLanguage
import com.example.data.model.StudyTrack
import com.example.data.model.Subject
import com.example.data.model.UserProfile
import com.example.data.quotes.QuotesRepository
import com.example.focus.FocusMonitorService
import com.example.focus.FocusRuntimeState
import com.example.focus.FocusStateHolder
import com.example.focus.InstalledAppsHelper
import com.example.notifications.ClassAlarmScheduler
import com.example.notifications.MotivationalQuoteWorker
import com.example.notifications.NotificationHelper
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SubjectProgress(
    val completedCount: Int,
    val totalCount: Int
) {
    val percent: Int
        get() = if (totalCount == 0) 0 else ((completedCount * 100f) / totalCount).toInt()
}

data class DayBarData(
    val epochDay: Long,
    val label: String,
    val minutes: Int
)

data class StatsSummary(
    val weeklyFocusMinutes: Int = 0,
    val monthlyFocusMinutes: Int = 0,
    val completedLessonsCount: Int = 0,
    val totalLessonsCount: Int = 0,
    val currentStreakDays: Int = 0,
    val bestDayLabel: String = "لا يوجد بعد",
    val bestDayMinutes: Int = 0,
    val last7Days: List<DayBarData> = emptyList()
)

data class MainUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile = UserProfile(),
    val schedulePromptDone: Boolean = false,
    val isDarkMode: Boolean = true,
    val motivationalNotificationsEnabled: Boolean = true,
    val blockedApps: Set<String> = emptySet(),
    val blockedAppsConfigured: Boolean = false,
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val scheduleClasses: List<ScheduleClassEntity> = emptyList(),
    val todayClasses: List<ScheduleClassEntity> = emptyList(),
    val progressMap: Map<String, LessonProgressEntity> = emptyMap(),
    val subjectProgressMap: Map<String, SubjectProgress> = emptyMap(),
    val totalCurriculumPercent: Int = 0,
    val reviewLessons: List<Pair<Subject, CurriculumLesson>> = emptyList(),
    val focusSessions: List<FocusSessionEntity> = emptyList(),
    val stats: StatsSummary = StatsSummary(),
    val dailyQuote: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = UserPreferencesRepository(application)
    private val db = AppDatabase.getInstance(application)
    private val repository = StudyRepository(db, prefsRepo)

    private val installedAppsFlow = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    private val quoteSeedFlow = MutableStateFlow((System.currentTimeMillis() / 3_600_000L).toInt())

    val focusRuntimeState: StateFlow<FocusRuntimeState> = FocusStateHolder.state

    private val settingsTupleFlow = combine(
        repository.userProfileFlow,
        repository.schedulePromptDoneFlow,
        repository.isDarkModeFlow,
        repository.motivationalNotificationsEnabledFlow,
        repository.blockedAppsConfiguredFlow
    ) { profile, scheduleDone, darkMode, notifEnabled, appsConfigured ->
        SettingsTuple(profile, scheduleDone, darkMode, notifEnabled, appsConfigured)
    }

    private val dataTupleFlow = combine(
        repository.scheduleFlow,
        repository.progressFlow,
        repository.focusSessionsFlow,
        repository.blockedAppsFlow,
        installedAppsFlow
    ) { schedule, progress, sessions, blockedApps, installedApps ->
        DataTuple(schedule, progress, sessions, blockedApps, installedApps)
    }

    val uiState: StateFlow<MainUiState> = combine(
        settingsTupleFlow,
        dataTupleFlow,
        quoteSeedFlow
    ) { settings, data, quoteSeed ->
        val profile = settings.profile
        val subjects = CurriculumData.getSubjectsFor(profile.track, profile.language)
        val progressMap = data.progress.associateBy { it.lessonId }
        val todayIdx = DayOfWeekAr.today().index
        val todayClasses = data.schedule.filter { it.dayOfWeekIndex == todayIdx }

        val subjectProgressMap = subjects.associate { subject ->
            val allLessons = subject.units.flatMap { it.lessons }
            val completed = allLessons.count { progressMap[it.id]?.isCompleted == true }
            subject.id to SubjectProgress(completed, allLessons.size)
        }

        val totalLessons = subjects.sumOf { it.totalLessonsCount }
        val totalCompleted = subjects.sumOf { subject ->
            subjectProgressMap[subject.id]?.completedCount ?: 0
        }
        val totalPercent = if (totalLessons == 0) 0 else ((totalCompleted * 100f) / totalLessons).toInt()

        val reviewLessons = subjects.flatMap { subject ->
            subject.units.flatMap { it.lessons }
                .filter { progressMap[it.id]?.needsReview == true }
                .map { subject to it }
        }

        val stats = computeStats(
            sessions = data.sessions,
            progress = data.progress,
            completedLessonsCount = totalCompleted,
            totalLessonsCount = totalLessons
        )

        MainUiState(
            isLoading = false,
            profile = profile,
            schedulePromptDone = settings.schedulePromptDone,
            isDarkMode = settings.isDarkMode,
            motivationalNotificationsEnabled = settings.motivationalNotifsEnabled,
            blockedApps = data.blockedApps,
            blockedAppsConfigured = settings.blockedAppsConfigured,
            installedApps = data.installedApps,
            subjects = subjects,
            scheduleClasses = data.schedule,
            todayClasses = todayClasses,
            progressMap = progressMap,
            subjectProgressMap = subjectProgressMap,
            totalCurriculumPercent = totalPercent,
            reviewLessons = reviewLessons,
            focusSessions = data.sessions,
            stats = stats,
            dailyQuote = QuotesRepository.getRandomQuote(profile.name, quoteSeed)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MainUiState(isLoading = true)
    )

    init {
        NotificationHelper.ensureChannels(application)
        refreshInstalledApps()
        viewModelScope.launch {
            val enabled = prefsRepo.isMotivationalNotificationsEnabledOnce()
            if (enabled) {
                MotivationalQuoteWorker.scheduleHourly(application)
            }
            val classes = repository.getAllClassesOnce()
            if (classes.isNotEmpty()) {
                ClassAlarmScheduler.rescheduleAll(application, classes)
            }
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = InstalledAppsHelper.loadLaunchableUserApps(getApplication())
            installedAppsFlow.value = apps
        }
    }

    fun refreshDailyQuote() {
        quoteSeedFlow.value = (System.currentTimeMillis() and 0x7FFFFFFFL).toInt()
    }

    fun saveOnboardingProfile(
        name: String,
        track: StudyTrack,
        language: StudyLanguage,
        isEditingFromSettings: Boolean
    ) {
        viewModelScope.launch {
            repository.saveProfile(
                UserProfile(
                    name = name.trim(),
                    track = track,
                    language = language,
                    onboardingCompleted = true
                )
            )
            if (isEditingFromSettings) {
                repository.markSchedulePromptDone(true)
            }
        }
    }

    fun reopenOnboardingForEdit() {
        viewModelScope.launch {
            val current = uiState.value.profile
            repository.saveProfile(current.copy(onboardingCompleted = false))
        }
    }

    fun finishInitialScheduleSetup() {
        viewModelScope.launch {
            repository.markSchedulePromptDone(true)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(enabled)
        }
    }

    fun setMotivationalNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setMotivationalNotificationsEnabled(enabled)
            if (enabled) {
                MotivationalQuoteWorker.scheduleHourly(getApplication())
            } else {
                MotivationalQuoteWorker.cancel(getApplication())
            }
        }
    }

    fun sendTestMotivationalNotification() {
        val name = uiState.value.profile.name
        val quote = QuotesRepository.getRandomQuote(name)
        NotificationHelper.showMotivationalQuote(getApplication(), quote)
    }

    fun addScheduleClass(
        dayOfWeekIndex: Int,
        subject: Subject,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        place: ClassPlace,
        teacherName: String
    ) {
        viewModelScope.launch {
            val entity = ScheduleClassEntity(
                dayOfWeekIndex = dayOfWeekIndex,
                subjectId = subject.id,
                subjectName = subject.shortDisplayName(uiState.value.profile.language),
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute,
                place = place.name,
                teacherName = teacherName.trim()
            )
            repository.addClass(entity)
            val allClasses = repository.getAllClassesOnce()
            ClassAlarmScheduler.rescheduleAll(getApplication(), allClasses)
        }
    }

    fun deleteScheduleClass(id: Long) {
        viewModelScope.launch {
            ClassAlarmScheduler.cancelForClass(getApplication(), id)
            repository.deleteClass(id)
            val allClasses = repository.getAllClassesOnce()
            ClassAlarmScheduler.rescheduleAll(getApplication(), allClasses)
        }
    }

    fun toggleLessonCompleted(lesson: CurriculumLesson) {
        viewModelScope.launch {
            val current = uiState.value.progressMap[lesson.id]?.isCompleted ?: false
            repository.setLessonCompleted(lesson.id, lesson.subjectId, !current)
        }
    }

    fun toggleLessonNeedsReview(lesson: CurriculumLesson) {
        viewModelScope.launch {
            val current = uiState.value.progressMap[lesson.id]?.needsReview ?: false
            repository.setLessonNeedsReview(lesson.id, lesson.subjectId, !current)
        }
    }

    fun saveBlockedApps(packages: Set<String>) {
        viewModelScope.launch {
            repository.saveBlockedApps(packages)
        }
    }

    fun startFocusSession(lesson: CurriculumLesson, subjectName: String, durationMinutes: Int) {
        val blocked = uiState.value.blockedApps
        val title = lesson.displayTitle(uiState.value.profile.language)
        FocusStateHolder.startSession(
            lessonId = lesson.id,
            lessonTitle = title,
            subjectName = subjectName,
            durationMinutes = durationMinutes,
            blockedPackages = blocked
        )
        FocusMonitorService.startFocus(getApplication())
    }

    fun quitFocusSessionEarly(reason: String) {
        val current = FocusStateHolder.state.value
        val elapsedSeconds = (current.totalSeconds - current.remainingSeconds).coerceAtLeast(0)
        FocusMonitorService.stopFocus(getApplication())
        FocusStateHolder.cancelEarly()

        viewModelScope.launch {
            val epochDay = System.currentTimeMillis() / 86_400_000L
            repository.saveFocusSession(
                FocusSessionEntity(
                    dateEpochDay = epochDay,
                    startedAtMillis = current.startedAtMillis,
                    plannedDurationMinutes = current.plannedMinutes,
                    actualFocusedSeconds = elapsedSeconds,
                    lessonId = current.lessonId,
                    lessonName = current.lessonTitle,
                    subjectName = current.subjectName,
                    isCompleted = false,
                    quitReason = reason.trim().ifEmpty { "توقف مبكر" }
                )
            )
        }
    }

    fun handleCelebrationLessonChoice(markCompleted: Boolean, markNeedsReview: Boolean) {
        val lessonId = FocusStateHolder.state.value.lessonId
        val lesson = CurriculumData.findLessonById(lessonId)
        FocusStateHolder.dismissCelebration()
        if (lesson != null) {
            viewModelScope.launch {
                if (markCompleted) {
                    repository.setLessonCompleted(lesson.id, lesson.subjectId, true)
                }
                if (markNeedsReview) {
                    repository.setLessonNeedsReview(lesson.id, lesson.subjectId, true)
                }
            }
        }
    }

    fun dismissCelebration() {
        FocusStateHolder.dismissCelebration()
    }

    suspend fun buildExportJson(): String = withContext(Dispatchers.IO) {
        repository.exportScheduleAndProgressJson()
    }

    suspend fun writeExportToUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val json = repository.exportScheduleAndProgressJson()
            getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
            }
            true
        }.getOrDefault(false)
    }

    suspend fun importScheduleFromUri(uri: Uri, replaceExisting: Boolean): Result<ImportResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val raw = getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader(Charsets.UTF_8).readText()
                } ?: error("تعذر قراءة الملف")
                val result = repository.importScheduleFromJson(raw, replaceExisting)
                val allClasses = repository.getAllClassesOnce()
                ClassAlarmScheduler.rescheduleAll(getApplication(), allClasses)
                result
            }
        }

    private fun computeStats(
        sessions: List<FocusSessionEntity>,
        progress: List<LessonProgressEntity>,
        completedLessonsCount: Int,
        totalLessonsCount: Int
    ): StatsSummary {
        val todayEpoch = System.currentTimeMillis() / 86_400_000L
        val weekStartEpoch = todayEpoch - 6L
        val monthStartEpoch = todayEpoch - 29L

        val completedSessions = sessions.filter { it.isCompleted }
        val weeklySecs = completedSessions
            .filter { it.dateEpochDay in weekStartEpoch..todayEpoch }
            .sumOf { it.actualFocusedSeconds }
        val monthlySecs = completedSessions
            .filter { it.dateEpochDay in monthStartEpoch..todayEpoch }
            .sumOf { it.actualFocusedSeconds }

        // Group minutes per epochDay
        val minutesByDay = completedSessions
            .groupBy { it.dateEpochDay }
            .mapValues { (_, list) -> (list.sumOf { it.actualFocusedSeconds } / 60).coerceAtLeast(1) }

        val cal = Calendar.getInstance()
        val last7Days = (6 downTo 0).map { offset ->
            val dayEpoch = todayEpoch - offset
            cal.timeInMillis = dayEpoch * 86_400_000L + 43_200_000L
            val dayLabel = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY -> "سبت"
                Calendar.SUNDAY -> "أحد"
                Calendar.MONDAY -> "اثنين"
                Calendar.TUESDAY -> "ثلاثاء"
                Calendar.WEDNESDAY -> "أربعاء"
                Calendar.THURSDAY -> "خميس"
                Calendar.FRIDAY -> "جمعة"
                else -> ""
            }
            DayBarData(
                epochDay = dayEpoch,
                label = dayLabel,
                minutes = minutesByDay[dayEpoch] ?: 0
            )
        }

        // Streak calculation: consecutive days with a completed focus session OR completed lesson
        val activeDays = HashSet<Long>()
        completedSessions.forEach { activeDays.add(it.dateEpochDay) }
        progress.filter { it.isCompleted }.forEach {
            activeDays.add(it.updatedAtMillis / 86_400_000L)
        }

        var streak = 0
        var checkDay = if (todayEpoch in activeDays) todayEpoch else todayEpoch - 1L
        while (checkDay in activeDays) {
            streak++
            checkDay--
        }

        val bestEntry = minutesByDay.maxByOrNull { it.value }
        val bestDayLabel = if (bestEntry != null) {
            cal.timeInMillis = bestEntry.key * 86_400_000L + 43_200_000L
            val dayName = DayOfWeekAr.entries.firstOrNull {
                it.calendarDay == cal.get(Calendar.DAY_OF_WEEK)
            }?.arabicName ?: "يوم"
            "$dayName (${bestEntry.value} د)"
        } else {
            "لا يوجد بعد"
        }

        return StatsSummary(
            weeklyFocusMinutes = weeklySecs / 60,
            monthlyFocusMinutes = monthlySecs / 60,
            completedLessonsCount = completedLessonsCount,
            totalLessonsCount = totalLessonsCount,
            currentStreakDays = streak,
            bestDayLabel = bestDayLabel,
            bestDayMinutes = bestEntry?.value ?: 0,
            last7Days = last7Days
        )
    }

    private data class SettingsTuple(
        val profile: UserProfile,
        val schedulePromptDone: Boolean,
        val isDarkMode: Boolean,
        val motivationalNotifsEnabled: Boolean,
        val blockedAppsConfigured: Boolean
    )

    private data class DataTuple(
        val schedule: List<ScheduleClassEntity>,
        val progress: List<LessonProgressEntity>,
        val sessions: List<FocusSessionEntity>,
        val blockedApps: Set<String>,
        val installedApps: List<InstalledAppInfo>
    )
}
