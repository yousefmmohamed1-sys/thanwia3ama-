package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ClassPlace
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "schedule_classes")
data class ScheduleClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeekIndex: Int, // 0 = Saturday .. 6 = Friday
    val subjectId: String,
    val subjectName: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val place: String = ClassPlace.CENTER.name,
    val teacherName: String = ""
)

@Serializable
@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val subjectId: String,
    val isCompleted: Boolean = false,
    val needsReview: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val startedAtMillis: Long,
    val plannedDurationMinutes: Int,
    val actualFocusedSeconds: Int,
    val lessonId: String,
    val lessonName: String,
    val subjectName: String,
    val isCompleted: Boolean,
    val quitReason: String? = null
)
