package com.example.data.local

import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ExportBundle(
    val formatVersion: Int = 1,
    val exportedAtMillis: Long = System.currentTimeMillis(),
    val trackHint: String = "",
    val schedule: List<ScheduleClassEntity> = emptyList(),
    val progress: List<LessonProgressEntity> = emptyList()
)

data class ImportResult(
    val importedClassesCount: Int,
    val sourceTrackHint: String
)

class StudyRepository(
    private val db: AppDatabase,
    private val prefs: UserPreferencesRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val userProfileFlow: Flow<UserProfile> = prefs.userProfileFlow
    val schedulePromptDoneFlow: Flow<Boolean> = prefs.schedulePromptDoneFlow
    val isDarkModeFlow: Flow<Boolean> = prefs.isDarkModeFlow
    val motivationalNotificationsEnabledFlow: Flow<Boolean> = prefs.motivationalNotificationsEnabledFlow
    val blockedAppsFlow: Flow<Set<String>> = prefs.blockedAppsFlow
    val blockedAppsConfiguredFlow: Flow<Boolean> = prefs.blockedAppsConfiguredFlow

    val scheduleFlow: Flow<List<ScheduleClassEntity>> = db.scheduleDao().observeAllClasses()
    val progressFlow: Flow<List<LessonProgressEntity>> = db.lessonProgressDao().observeAllProgress()
    val focusSessionsFlow: Flow<List<FocusSessionEntity>> = db.focusSessionDao().observeAllSessions()

    suspend fun saveProfile(profile: UserProfile) = prefs.saveProfile(profile)
    suspend fun markSchedulePromptDone(done: Boolean) = prefs.markSchedulePromptDone(done)
    suspend fun setDarkMode(enabled: Boolean) = prefs.setDarkMode(enabled)
    suspend fun setMotivationalNotificationsEnabled(enabled: Boolean) =
        prefs.setMotivationalNotificationsEnabled(enabled)
    suspend fun saveBlockedApps(packages: Set<String>) = prefs.saveBlockedApps(packages)

    suspend fun getAllClassesOnce(): List<ScheduleClassEntity> = db.scheduleDao().getAllClassesOnce()

    suspend fun addClass(item: ScheduleClassEntity): Long = db.scheduleDao().insertClass(item)

    suspend fun deleteClass(id: Long) = db.scheduleDao().deleteClassById(id)

    suspend fun setLessonCompleted(lessonId: String, subjectId: String, completed: Boolean) {
        val existing = db.lessonProgressDao().getByLessonId(lessonId)
        val updated = LessonProgressEntity(
            lessonId = lessonId,
            subjectId = subjectId,
            isCompleted = completed,
            needsReview = existing?.needsReview ?: false,
            updatedAtMillis = System.currentTimeMillis()
        )
        db.lessonProgressDao().upsertProgress(updated)
    }

    suspend fun setLessonNeedsReview(lessonId: String, subjectId: String, needsReview: Boolean) {
        val existing = db.lessonProgressDao().getByLessonId(lessonId)
        val updated = LessonProgressEntity(
            lessonId = lessonId,
            subjectId = subjectId,
            isCompleted = existing?.isCompleted ?: false,
            needsReview = needsReview,
            updatedAtMillis = System.currentTimeMillis()
        )
        db.lessonProgressDao().upsertProgress(updated)
    }

    suspend fun saveFocusSession(session: FocusSessionEntity): Long =
        db.focusSessionDao().insertSession(session)

    suspend fun exportScheduleAndProgressJson(): String {
        val profile = prefs.getProfileOnce()
        val schedule = db.scheduleDao().getAllClassesOnce()
        val progress = db.lessonProgressDao().getAllProgressOnce()
        val bundle = ExportBundle(
            formatVersion = 1,
            exportedAtMillis = System.currentTimeMillis(),
            trackHint = profile.track.arabicLabel,
            schedule = schedule,
            progress = progress
        )
        return json.encodeToString(bundle)
    }

    /**
     * Imports a friend's schedule from a shared .json file.
     * Each user's identity and own progress remain completely separate:
     * importing NEVER overwrites the current user's name, track, language, or personal progress.
     */
    suspend fun importScheduleFromJson(rawJson: String, replaceExistingSchedule: Boolean): ImportResult {
        val bundle = json.decodeFromString<ExportBundle>(rawJson)
        if (replaceExistingSchedule) {
            db.scheduleDao().clearAll()
        }
        val sanitized = bundle.schedule.map { item ->
            item.copy(id = 0)
        }
        if (sanitized.isNotEmpty()) {
            db.scheduleDao().insertAll(sanitized)
        }
        return ImportResult(
            importedClassesCount = sanitized.size,
            sourceTrackHint = bundle.trackHint
        )
    }
}
