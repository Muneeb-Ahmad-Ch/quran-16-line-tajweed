package com.muneeb.quran16line.ui

import com.muneeb.quran16line.data.Settings
import org.junit.Assert.assertEquals
import org.junit.Test

class StartScreenTest {
    @Test
    fun opensHomeWhenAutoResumeOff() {
        assertEquals(listOf(Screen.Home), initialBackStack(Settings(autoResume = false, lastPage = 77)))
    }

    @Test
    fun opensLastPageOverHomeWhenAutoResumeOn() {
        assertEquals(listOf(Screen.Home, Screen.Reader(77)), initialBackStack(Settings(autoResume = true, lastPage = 77)))
    }
}
