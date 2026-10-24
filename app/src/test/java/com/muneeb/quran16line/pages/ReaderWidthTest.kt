package com.muneeb.quran16line.pages

import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderWidthTest {
    private val info = VersionInfo(1200, 1838, "#ffffff")

    @Test
    fun usesWindowWidthWhenSmaller() {
        assertEquals(1080, readerTargetWidth(1080, info))
    }

    @Test
    fun neverUpscalesBeyondSource() {
        assertEquals(1200, readerTargetWidth(1440, info))
    }
}
