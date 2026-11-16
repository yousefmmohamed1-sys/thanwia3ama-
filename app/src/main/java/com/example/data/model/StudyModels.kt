package com.example.data.model

import java.util.Calendar
import kotlinx.serialization.Serializable

@Serializable
enum class StudyTrack(
    val arabicLabel: String,
    val englishLabel: String,
    val subtitle: String,
    val emoji: String
) {
    SCI_BIO(
        arabicLabel = "علمي علوم",
        englishLabel = "Scientific (Biology)",
        subtitle = "فيزياء، كيمياء، أحياء، جيولوجيا",
        emoji = "🧬"
    ),
    SCI_MATH(
        arabicLabel = "علمي رياضة",
        englishLabel = "Scientific (Math)",
        subtitle = "فيزياء، كيمياء، بحتة، تطبيقية",
        emoji = "📐"
    ),
    LITERARY(
        arabicLabel = "أدبي",
        englishLabel = "Literary",
        subtitle = "تاريخ، جغرافيا، فلسفة، علم نفس، اقتصاد وإحصاء",
        emoji = "🏛️"
    )
}

@Serializable
enum class StudyLanguage(
    val arabicLabel: String,
    val subtitle: String,
    val emoji: String
) {
    ARABIC(
        arabicLabel = "عربي",
        subtitle = "مدارس عربي - مناهج باللغة العربية",
        emoji = "🇪🇬"
    ),
    LANGUAGES(
        arabicLabel = "لغات / English",
        subtitle = "مدارس لغات - المواد العلمية باللغة الإنجليزية",
        emoji = "🌐"
    )
}

@Serializable
enum class ClassPlace(val arabicLabel: String, val emoji: String) {
    CENTER("سنتر", "🏫"),
    ONLINE("أونلاين", "💻"),
    PRIVATE("برايفت", "🏠")
}

@Serializable
enum class DayOfWeekAr(
    val index: Int,
    val arabicName: String,
    val shortName: String,
    val calendarDay: Int
) {
    SATURDAY(0, "السبت", "سبت", Calendar.SATURDAY),
    SUNDAY(1, "الأحد", "أحد", Calendar.SUNDAY),
    MONDAY(2, "الاثنين", "اثنين", Calendar.MONDAY),
    TUESDAY(3, "الثلاثاء", "ثلاثاء", Calendar.TUESDAY),
    WEDNESDAY(4, "الأربعاء", "أربعاء", Calendar.WEDNESDAY),
    THURSDAY(5, "الخميس", "خميس", Calendar.THURSDAY),
    FRIDAY(6, "الجمعة", "جمعة", Calendar.FRIDAY);

    companion object {
        fun fromIndex(index: Int): DayOfWeekAr =
            entries.firstOrNull { it.index == index } ?: SATURDAY

        fun today(): DayOfWeekAr {
            val calDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            return entries.firstOrNull { it.calendarDay == calDay } ?: SATURDAY
        }
    }
}

@Serializable
data class UserProfile(
    val name: String = "",
    val track: StudyTrack = StudyTrack.SCI_BIO,
    val language: StudyLanguage = StudyLanguage.ARABIC,
    val onboardingCompleted: Boolean = false
)

data class CurriculumLesson(
    val id: String,
    val unitId: String,
    val subjectId: String,
    val arabicTitle: String,
    val englishTitle: String,
    val estimatedMinutes: Int
) {
    fun displayTitle(language: StudyLanguage): String =
        if (language == StudyLanguage.LANGUAGES && englishTitle != arabicTitle) {
            "$englishTitle — $arabicTitle"
        } else {
            arabicTitle
        }
}

data class CurriculumUnit(
    val id: String,
    val subjectId: String,
    val arabicTitle: String,
    val englishTitle: String,
    val lessons: List<CurriculumLesson>
) {
    fun displayTitle(language: StudyLanguage): String =
        if (language == StudyLanguage.LANGUAGES && englishTitle != arabicTitle) {
            "$englishTitle ($arabicTitle)"
        } else {
            arabicTitle
        }
}

data class Subject(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val emoji: String,
    val tracks: Set<StudyTrack>,
    val units: List<CurriculumUnit>
) {
    fun displayName(language: StudyLanguage): String =
        if (language == StudyLanguage.LANGUAGES && englishName != arabicName) {
            "$englishName / $arabicName"
        } else {
            arabicName
        }

    fun shortDisplayName(language: StudyLanguage): String =
        if (language == StudyLanguage.LANGUAGES && englishName != arabicName) {
            englishName
        } else {
            arabicName
        }

    val totalLessonsCount: Int
        get() = units.sumOf { it.lessons.size }
}

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSuggestedDistraction: Boolean
)
