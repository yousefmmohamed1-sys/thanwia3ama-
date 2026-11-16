package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.StudyLanguage
import com.example.data.model.StudyTrack
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rafiq_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val NAME = stringPreferencesKey("user_name")
        val TRACK = stringPreferencesKey("user_track")
        val LANGUAGE = stringPreferencesKey("user_language")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SCHEDULE_PROMPT_DONE = booleanPreferencesKey("schedule_prompt_done")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val MOTIVATIONAL_NOTIFS = booleanPreferencesKey("motivational_notifs_enabled")
        val BLOCKED_APPS = stringSetPreferencesKey("blocked_apps")
        val BLOCKED_APPS_CONFIGURED = booleanPreferencesKey("blocked_apps_configured")
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        val track = runCatching {
            StudyTrack.valueOf(prefs[Keys.TRACK] ?: StudyTrack.SCI_BIO.name)
        }.getOrDefault(StudyTrack.SCI_BIO)

        val language = runCatching {
            StudyLanguage.valueOf(prefs[Keys.LANGUAGE] ?: StudyLanguage.ARABIC.name)
        }.getOrDefault(StudyLanguage.ARABIC)

        UserProfile(
            name = prefs[Keys.NAME] ?: "",
            track = track,
            language = language,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false
        )
    }

    val schedulePromptDoneFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SCHEDULE_PROMPT_DONE] ?: false
    }

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] ?: true // Dark Mode by default
    }

    val motivationalNotificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.MOTIVATIONAL_NOTIFS] ?: true
    }

    val blockedAppsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.BLOCKED_APPS] ?: emptySet()
    }

    val blockedAppsConfiguredFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.BLOCKED_APPS_CONFIGURED] ?: false
    }

    suspend fun saveProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NAME] = profile.name.trim()
            prefs[Keys.TRACK] = profile.track.name
            prefs[Keys.LANGUAGE] = profile.language.name
            prefs[Keys.ONBOARDING_COMPLETED] = profile.onboardingCompleted
        }
    }

    suspend fun markSchedulePromptDone(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SCHEDULE_PROMPT_DONE] = done
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }

    suspend fun setMotivationalNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MOTIVATIONAL_NOTIFS] = enabled
        }
    }

    suspend fun saveBlockedApps(packages: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BLOCKED_APPS] = packages
            prefs[Keys.BLOCKED_APPS_CONFIGURED] = true
        }
    }

    suspend fun getProfileOnce(): UserProfile = userProfileFlow.first()

    suspend fun isMotivationalNotificationsEnabledOnce(): Boolean =
        motivationalNotificationsEnabledFlow.first()

    suspend fun getBlockedAppsOnce(): Set<String> = blockedAppsFlow.first()
}
