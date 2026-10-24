package com.muneeb.quran16line.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.muneeb.quran16line.data.Bookmark
import com.muneeb.quran16line.data.Settings
import com.muneeb.quran16line.data.SettingsRepository
import com.muneeb.quran16line.pages.PageImages
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.store by preferencesDataStore(name = "quran16line")

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app.store)

    val images = PageImages(app)

    /** Null until the first read from disk finishes (a few ms); screens wait for it instead of flashing defaults. */
    val settings: StateFlow<Settings?> = repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val bookmarks: StateFlow<List<Bookmark>> = repo.bookmarks.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val backStack = mutableStateListOf<Screen>(Screen.Home)

    /** False until the first settings read has decided the start screen (Home, or the last page). */
    var ready by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val first = repo.settings.first()
            backStack.clear()
            backStack.addAll(initialBackStack(first))
            ready = true
        }
    }

    fun open(screen: Screen) {
        backStack.add(screen)
    }

    /** Pops the top screen and returns it, or null when already at Home. */
    fun back(): Screen? {
        if (backStack.size <= 1) return null
        return backStack.removeAt(backStack.lastIndex)
    }

    fun onPageShown(page: Int) {
        viewModelScope.launch { repo.setLastPage(page) }
    }

    fun saveBookmark(page: Int, name: String) {
        viewModelScope.launch { repo.upsertBookmark(page, name) }
    }

    fun removeBookmark(page: Int) {
        viewModelScope.launch { repo.removeBookmark(page) }
    }

    fun updateSettings(transform: (Settings) -> Settings) {
        viewModelScope.launch { repo.update(transform) }
    }
}
