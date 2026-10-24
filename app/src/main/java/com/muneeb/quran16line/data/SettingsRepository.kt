package com.muneeb.quran16line.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Settings, resume position and bookmarks, all in one Preferences DataStore. */
class SettingsRepository(
    private val store: DataStore<Preferences>,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private object Keys {
        val version = stringPreferencesKey("version")
        val theme = stringPreferencesKey("theme")
        val darkPages = booleanPreferencesKey("dark_pages")
        val pageStyle = stringPreferencesKey("page_style")
        val keepScreenOn = booleanPreferencesKey("keep_screen_on")
        val lastPage = intPreferencesKey("last_page")
        val bookmarks = stringPreferencesKey("bookmarks")
    }

    private inline fun <reified E : Enum<E>> Preferences.enum(key: Preferences.Key<String>, default: E): E =
        this[key]?.let { name -> enumValues<E>().firstOrNull { it.name == name } } ?: default

    private fun Preferences.toSettings(): Settings {
        val d = Settings()
        return Settings(
            version = enum(Keys.version, d.version),
            theme = enum(Keys.theme, d.theme),
            darkPages = this[Keys.darkPages] ?: d.darkPages,
            pageStyle = enum(Keys.pageStyle, d.pageStyle),
            keepScreenOn = this[Keys.keepScreenOn] ?: d.keepScreenOn,
            lastPage = clamp(this[Keys.lastPage] ?: d.lastPage),
        )
    }

    val settings: Flow<Settings> = store.data.map { it.toSettings() }.distinctUntilChanged()

    val bookmarks: Flow<List<Bookmark>> = store.data
        .map { p -> BookmarkCodec.decode(p[Keys.bookmarks]).sortedByDescending { it.createdAt } }
        .distinctUntilChanged()

    suspend fun setLastPage(p: Int) {
        store.edit { it[Keys.lastPage] = clamp(p) }
    }

    suspend fun update(transform: (Settings) -> Settings) {
        store.edit { p ->
            val next = transform(p.toSettings())
            p[Keys.version] = next.version.name
            p[Keys.theme] = next.theme.name
            p[Keys.darkPages] = next.darkPages
            p[Keys.pageStyle] = next.pageStyle.name
            p[Keys.keepScreenOn] = next.keepScreenOn
            p[Keys.lastPage] = clamp(next.lastPage)
        }
    }

    /** One bookmark per page; saving again renames it and moves it to the top. */
    suspend fun upsertBookmark(page: Int, name: String) {
        store.edit { p ->
            val rest = BookmarkCodec.decode(p[Keys.bookmarks]).filter { it.page != page }
            p[Keys.bookmarks] = BookmarkCodec.encode(rest + Bookmark(clamp(page), name, clock()))
        }
    }

    suspend fun removeBookmark(page: Int) {
        store.edit { p ->
            p[Keys.bookmarks] = BookmarkCodec.encode(BookmarkCodec.decode(p[Keys.bookmarks]).filter { it.page != page })
        }
    }

    private fun clamp(p: Int) = p.coerceIn(QuranIndex.FIRST_PAGE, QuranIndex.LAST_PAGE)
}
