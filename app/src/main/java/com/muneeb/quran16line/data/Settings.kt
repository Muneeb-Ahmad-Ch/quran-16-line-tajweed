package com.muneeb.quran16line.data

enum class AppTheme { LIGHT, DARK, SYSTEM }

/** How Quran pages are drawn. AUTO = printed page in light theme, Night in dark theme. */
enum class PageStyle { AUTO, ORIGINAL, NIGHT, INVERT, WARM }

data class Settings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val pageStyle: PageStyle = PageStyle.AUTO,
    val keepScreenOn: Boolean = true,
    /** Stretch pages to fill the screen instead of keeping the printed proportions. */
    val stretchPages: Boolean = false,
    /** Open straight onto the last page read when the app starts. */
    val autoResume: Boolean = false,
    val lastPage: Int = QuranIndex.FIRST_PAGE,
)
