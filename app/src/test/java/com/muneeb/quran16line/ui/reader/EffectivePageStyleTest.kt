package com.muneeb.quran16line.ui.reader

import com.muneeb.quran16line.data.PageStyle
import com.muneeb.quran16line.data.Settings
import org.junit.Assert.assertEquals
import org.junit.Test

class EffectivePageStyleTest {
    @Test
    fun lightThemeAlwaysOriginal() {
        assertEquals(PageStyle.ORIGINAL, effectivePageStyle(Settings(pageStyle = PageStyle.WARM), dark = false))
    }

    @Test
    fun darkWithPagesOnUsesChosenStyle() {
        assertEquals(PageStyle.INVERT, effectivePageStyle(Settings(pageStyle = PageStyle.INVERT, darkPages = true), dark = true))
    }

    @Test
    fun darkWithPagesOffKeepsPrintedPage() {
        assertEquals(PageStyle.ORIGINAL, effectivePageStyle(Settings(pageStyle = PageStyle.NIGHT, darkPages = false), dark = true))
    }
}
