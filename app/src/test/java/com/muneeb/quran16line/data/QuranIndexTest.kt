package com.muneeb.quran16line.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranIndexTest {

    @Test
    fun surahCountAndOrder() {
        val s = QuranIndex.surahs
        assertEquals(114, s.size)
        assertEquals((1..114).toList(), s.map { it.number })
        assertTrue(s.zipWithNext().all { (a, b) -> a.startPage <= b.startPage })
        assertEquals(2, s.first().startPage)
        assertEquals(3, s[1].startPage)
        assertEquals("Al-Baqarah", s[1].nameEn)
        assertEquals(549, s.last().startPage)
    }

    @Test
    fun paraRanges() {
        val p = QuranIndex.paras
        assertEquals(30, p.size)
        assertEquals(2 to 20, p[0].startPage to p[0].endPage)
        for (n in 2..27) {
            assertEquals(21 + 18 * (n - 2), p[n - 1].startPage)
            assertEquals(38 + 18 * (n - 2), p[n - 1].endPage)
        }
        assertEquals(489 to 508, p[27].startPage to p[27].endPage)
        assertEquals(509 to 528, p[28].startPage to p[28].endPage)
        assertEquals(529 to 549, p[29].startPage to p[29].endPage)
        assertEquals("Sayaqool", p[1].nameEn)
    }

    @Test
    fun manzils() {
        val m = QuranIndex.manzils
        assertEquals(listOf(1, 5, 10, 17, 26, 37, 50), m.map { it.startSurah })
        assertEquals(m.map { QuranIndex.surahs[it.startSurah - 1].startPage }, m.map { it.startPage })
        assertEquals(7, QuranIndex.manzilForPage(549).number)
        assertEquals(1, QuranIndex.manzilForPage(2).number)
    }

    @Test
    fun pageBounds() {
        assertEquals(1, QuranIndex.paraForPage(2).number)
        assertEquals(30, QuranIndex.paraForPage(549).number)
        assertEquals(2, QuranIndex.paraForPage(21).number)
        assertEquals(1, QuranIndex.paraForPage(20).number)
        for (bad in listOf(1, 0, 550)) {
            try {
                QuranIndex.paraForPage(bad)
                throw AssertionError("expected IllegalArgumentException for $bad")
            } catch (_: IllegalArgumentException) {
            }
        }
    }

    @Test
    fun surahForPageMultiSurahPage() {
        // page 549 holds Al-Falaq and An-Nas: the first Surah starting on the page wins
        assertEquals(113, QuranIndex.surahForPage(549).number)
        // page 24 is inside Al-Baqarah
        assertEquals(2, QuranIndex.surahForPage(24).number)
        // page 2 is Al-Fatihah
        assertEquals(1, QuranIndex.surahForPage(2).number)
    }

    @Test
    fun defaultName() {
        assertEquals("Sayaqool – Al-Baqarah – Pg 24", QuranIndex.defaultBookmarkName(24))
    }

    @Test
    fun ayahCounts() {
        val s = QuranIndex.surahs
        assertEquals(6236, s.sumOf { it.ayahs })
        assertEquals(7, s[0].ayahs)
        assertEquals(286, s[1].ayahs)
        assertEquals(83, s[35].ayahs)
        assertEquals(6, s[113].ayahs)
    }

    @Test
    fun surahPageRanges() {
        val s = QuranIndex.surahs
        assertEquals(2 to 2, s[0].startPage to s[0].endPage)       // Al-Fatihah alone on page 2
        assertEquals(3 to 46, s[1].startPage to s[1].endPage)      // Aal-e-Imran starts mid-page 46
        assertEquals(44, s[1].pageCount)
        assertEquals(265 to 275, s[17].startPage to s[17].endPage) // Maryam's banner opens page 276
        assertEquals(509 to 511, s[66].startPage to s[66].endPage) // Al-Mulk
        assertEquals(532, s[79].endPage)                           // Abasa ends before At-Takwir at top of 533
        assertEquals(1, s[80].pageCount)                           // At-Takwir shares page 533
        assertEquals(549 to 549, s[113].startPage to s[113].endPage)
        assertTrue(s.zipWithNext().all { (a, b) -> a.endPage in b.startPage - 1..b.startPage })
    }

    @Test
    fun paraAndManzilPageCounts() {
        assertEquals(19, QuranIndex.paras[0].pageCount)
        assertEquals(18, QuranIndex.paras[1].pageCount)
        assertEquals(21, QuranIndex.paras[29].pageCount)
        assertEquals(548, QuranIndex.paras.sumOf { it.pageCount })
        val m = QuranIndex.manzils
        assertEquals(255 to 331, m[3].startPage to m[3].endPage)   // Bani Isra'il .. Ash-Shu'ara starts mid-page 331
        assertEquals(254, m[2].endPage)                             // Bani Isra'il opens page 255
        assertEquals(549, m[6].endPage)
        assertEquals(77, m[3].pageCount)
    }
}
