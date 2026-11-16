package com.example

import com.example.data.curriculum.CurriculumData
import com.example.data.model.StudyLanguage
import com.example.data.model.StudyTrack
import com.example.data.quotes.QuotesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyAppUnitTest {

    @Test
    fun quotesRepository_has250UniqueBaseQuotes_and1000Combinations() {
        assertEquals(250, QuotesRepository.baseQuotes.size)
        assertEquals(250, QuotesRepository.baseQuotes.distinct().size)
        assertEquals(1000, QuotesRepository.totalCombinationsCount)

        val quote = QuotesRepository.getRandomQuote("يوسف", seed = 42)
        assertTrue(quote.contains("يوسف"))
    }

    @Test
    fun curriculum_tracksAndLessonsAreValidAndUnique() {
        val allLessons = CurriculumData.allSubjects.flatMap { it.units }.flatMap { it.lessons }
        assertEquals(allLessons.size, allLessons.map { it.id }.distinct().size)

        val sciBio = CurriculumData.getSubjectsFor(StudyTrack.SCI_BIO, StudyLanguage.ARABIC)
        assertEquals(7, sciBio.size)
        assertTrue(sciBio.any { it.id == "biology" })
        assertTrue(sciBio.any { it.id == "geology" })

        val sciMath = CurriculumData.getSubjectsFor(StudyTrack.SCI_MATH, StudyLanguage.LANGUAGES)
        assertEquals(7, sciMath.size)
        assertTrue(sciMath.any { it.id == "pure_math" })
        assertTrue(sciMath.any { it.id == "applied_math" })

        val literary = CurriculumData.getSubjectsFor(StudyTrack.LITERARY, StudyLanguage.ARABIC)
        assertEquals(8, literary.size)
        assertTrue(literary.any { it.id == "history" })
        assertTrue(literary.any { it.id == "geography" })
        assertTrue(literary.any { it.id == "philosophy" })
        assertTrue(literary.any { it.id == "psychology" })
        assertTrue(literary.any { it.id == "econ_stats" })
    }
}
