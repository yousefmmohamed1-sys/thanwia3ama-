package com.example.focus

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FocusRuntimeState(
    val isActive: Boolean = false,
    val showCelebration: Boolean = false,
    val lessonId: String = "",
    val lessonTitle: String = "",
    val subjectName: String = "",
    val plannedMinutes: Int = 25,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val startedAtMillis: Long = 0L,
    val blockedPackages: Set<String> = emptySet()
) {
    val remainingMinutesCeil: Int
        get() = ((remainingSeconds + 59) / 60).coerceAtLeast(1)
}

object FocusStateHolder {
    private val _state = MutableStateFlow(FocusRuntimeState())
    val state: StateFlow<FocusRuntimeState> = _state.asStateFlow()

    fun startSession(
        lessonId: String,
        lessonTitle: String,
        subjectName: String,
        durationMinutes: Int,
        blockedPackages: Set<String>
    ) {
        val totalSecs = (durationMinutes * 60).coerceAtLeast(60)
        _state.value = FocusRuntimeState(
            isActive = true,
            showCelebration = false,
            lessonId = lessonId,
            lessonTitle = lessonTitle,
            subjectName = subjectName,
            plannedMinutes = durationMinutes,
            totalSeconds = totalSecs,
            remainingSeconds = totalSecs,
            startedAtMillis = System.currentTimeMillis(),
            blockedPackages = blockedPackages
        )
    }

    fun tickSecond(): Int {
        var nextRemaining = 0
        _state.update { current ->
            if (!current.isActive) return@update current
            nextRemaining = (current.remainingSeconds - 1).coerceAtLeast(0)
            current.copy(remainingSeconds = nextRemaining)
        }
        return nextRemaining
    }

    fun completeSession() {
        _state.update { current ->
            current.copy(
                isActive = false,
                showCelebration = true,
                remainingSeconds = 0
            )
        }
    }

    fun cancelEarly() {
        _state.update {
            FocusRuntimeState()
        }
    }

    fun dismissCelebration() {
        _state.update {
            it.copy(showCelebration = false)
        }
    }
}
