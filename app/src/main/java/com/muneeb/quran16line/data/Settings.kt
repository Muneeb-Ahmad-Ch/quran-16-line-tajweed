package com.muneeb.quran16line.data

enum class MushafVersion(val assetDir: String) { PLAIN("plain"), COLOR("color") }

enum class AppTheme { LIGHT, DARK, SYSTEM }

enum class PageStyle { ORIGINAL, NIGHT, INVERT, WARM }

data class Settings(
    val version: MushafVersion = MushafVersion.COLOR,
    val theme: AppTheme = AppTheme.SYSTEM,
    val darkPages: Boolean = true,
    val pageStyle: PageStyle = PageStyle.NIGHT,
    val keepScreenOn: Boolean = true,
    /** Stretch pages to fill the screen instead of keeping the printed proportions. */
    val stretchPages: Boolean = false,
    /** Open straight onto the last page read when the app starts. */
    val autoResume: Boolean = false,
    val lastPage: Int = QuranIndex.FIRST_PAGE,
)
