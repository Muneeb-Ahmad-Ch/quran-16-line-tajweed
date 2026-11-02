package com.muneeb.quran16line.data

import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsRepositoryTest {

    @Test
    fun defaults() = runTest {
        val s = SettingsRepository(testStore(backgroundScope)).settings.first()
        assertEquals(Settings(), s)
        assertEquals(AppTheme.SYSTEM, s.theme)
        assertEquals(PageStyle.AUTO, s.pageStyle)
        assertEquals(true, s.keepScreenOn)
        assertEquals(false, s.stretchPages)
        assertEquals(false, s.autoResume)
    }

    @Test
    fun autoResumePersists() = runTest {
        val store = testStore(backgroundScope)
        SettingsRepository(store).update { it.copy(autoResume = true) }
        assertEquals(true, SettingsRepository(store).settings.first().autoResume)
    }

    @Test
    fun stretchPagesPersists() = runTest {
        val store = testStore(backgroundScope)
        SettingsRepository(store).update { it.copy(stretchPages = true) }
        assertEquals(true, SettingsRepository(store).settings.first().stretchPages)
    }

    @Test
    fun settingChangeKeepsLastPage() = runTest {
        val repo = SettingsRepository(testStore(backgroundScope))
        repo.setLastPage(300)
        repo.update { it.copy(theme = AppTheme.DARK) }
        val s = repo.settings.first()
        assertEquals(300, s.lastPage)
        assertEquals(AppTheme.DARK, s.theme)
    }

    @Test
    fun settingsPersistAcrossInstances() = runTest {
        val store = testStore(backgroundScope)
        SettingsRepository(store).update { it.copy(theme = AppTheme.DARK, pageStyle = PageStyle.WARM, keepScreenOn = false) }
        val s = SettingsRepository(store).settings.first()
        assertEquals(AppTheme.DARK, s.theme)
        assertEquals(PageStyle.WARM, s.pageStyle)
        assertEquals(false, s.keepScreenOn)
    }

    @Test
    fun setLastPageClamps() = runTest {
        val repo = SettingsRepository(testStore(backgroundScope))
        repo.setLastPage(0)
        assertEquals(2, repo.settings.first().lastPage)
        repo.setLastPage(999)
        assertEquals(549, repo.settings.first().lastPage)
    }

    @Test
    fun upsertReplacesSamePage() = runTest {
        var now = 1L
        val repo = SettingsRepository(testStore(backgroundScope)) { now++ }
        repo.upsertBookmark(24, "first")
        repo.upsertBookmark(24, "second")
        val b = repo.bookmarks.first()
        assertEquals(1, b.size)
        assertEquals("second", b[0].name)
    }

    @Test
    fun bookmarksNewestFirst() = runTest {
        var now = 1L
        val repo = SettingsRepository(testStore(backgroundScope)) { now++ }
        repo.upsertBookmark(10, "a")
        repo.upsertBookmark(20, "b")
        repo.upsertBookmark(10, "a2")
        assertEquals(listOf(10, 20), repo.bookmarks.first().map { it.page })
    }

    @Test
    fun removeBookmark() = runTest {
        val repo = SettingsRepository(testStore(backgroundScope))
        repo.upsertBookmark(10, "a")
        repo.upsertBookmark(20, "b")
        repo.removeBookmark(10)
        assertEquals(listOf(20), repo.bookmarks.first().map { it.page })
    }

    @Test
    fun legacyDarkOnlyStyleBecomesAuto() = runTest {
        val store = testStore(backgroundScope)
        store.edit {
            it[androidx.datastore.preferences.core.booleanPreferencesKey("dark_pages")] = true
            it[androidx.datastore.preferences.core.stringPreferencesKey("page_style")] = "NIGHT"
        }
        val repo = SettingsRepository(store)
        assertEquals(PageStyle.AUTO, repo.settings.first().pageStyle)
        repo.update { it.copy(pageStyle = PageStyle.WARM) }
        assertEquals(PageStyle.WARM, repo.settings.first().pageStyle)
    }
}
