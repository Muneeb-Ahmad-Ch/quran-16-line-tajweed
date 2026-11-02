package com.muneeb.quran16line.ui.reader

import com.muneeb.quran16line.data.PageStyle
import com.muneeb.quran16line.data.Settings
import org.junit.Assert.assertEquals
import org.junit.Test

class EffectivePageStyleTest {
    @Test
    fun autoFollowsAppTheme() {
        assertEquals(PageStyle.ORIGINAL, effectivePageStyle(Settings(pageStyle = PageStyle.AUTO), dark = false))
        assertEquals(PageStyle.NIGHT, effectivePageStyle(Settings(pageStyle = PageStyle.AUTO), dark = true))
    }

    @Test
    fun chosenStyleAppliesInLightTheme() {
        assertEquals(PageStyle.WARM, effectivePageStyle(Settings(pageStyle = PageStyle.WARM), dark = false))
    }

    @Test
    fun originalStaysPrintedInDarkTheme() {
        assertEquals(PageStyle.ORIGINAL, effectivePageStyle(Settings(pageStyle = PageStyle.ORIGINAL), dark = true))
    }
}
