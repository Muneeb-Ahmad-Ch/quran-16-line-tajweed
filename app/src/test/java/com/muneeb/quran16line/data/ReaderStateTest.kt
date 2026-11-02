package com.muneeb.quran16line.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderStateTest {
    @Test
    fun firstLaunchResumesAtPage2() = runTest {
        val s = SettingsRepository(testStore(backgroundScope)).settings.first()
        assertEquals(2, s.lastPage)
        assertEquals("Al-Fatihah", QuranIndex.surahForPage(s.lastPage).nameEn)
    }
}
