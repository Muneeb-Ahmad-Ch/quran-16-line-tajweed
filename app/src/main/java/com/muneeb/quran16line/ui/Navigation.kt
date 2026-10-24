package com.muneeb.quran16line.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muneeb.quran16line.data.AppTheme
import com.muneeb.quran16line.ui.extras.ExtrasScreen
import com.muneeb.quran16line.ui.home.HomeScreen
import com.muneeb.quran16line.ui.reader.ReaderScreen
import com.muneeb.quran16line.ui.settings.AboutScreen
import com.muneeb.quran16line.ui.settings.SettingsScreen
import com.muneeb.quran16line.ui.theme.QuranTheme

sealed interface Screen {
    data object Home : Screen
    data class Reader(val page: Int) : Screen
    data object Settings : Screen
    data object About : Screen
    data class Extras(val index: Int) : Screen
}

@Composable
fun App(vm: AppViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val s = settings
    val dark = when (s?.theme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        else -> isSystemInDarkTheme()
    }
    val saveable = rememberSaveableStateHolder()
    val view = LocalView.current
    SideEffect {
        // The activity isn't recreated on theme changes, so keep bar icon contrast in sync here.
        WindowCompat.getInsetsController((view.context as Activity).window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
    QuranTheme(dark = dark) {
        // Surface also sets the default content (text/icon) colour for the theme.
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (s == null) return@Surface // first disk read still in flight: show only the themed background
            BackHandler(enabled = vm.backStack.size > 1) { vm.back() }
            val screen = vm.backStack.last()
            // Keeps each screen's saved UI state (Home's tab and scroll positions) while it's in the back stack.
            saveable.SaveableStateProvider(screen.toString()) {
                when (screen) {
                    Screen.Home -> HomeScreen(vm, s, dark, open = vm::open)
                    is Screen.Reader -> ReaderScreen(vm, s, dark, startPage = screen.page)
                    Screen.Settings -> SettingsScreen(vm, s, dark, onBack = { vm.back() }, open = vm::open)
                    Screen.About -> AboutScreen(onBack = { vm.back() })
                    is Screen.Extras -> ExtrasScreen(vm, s, dark, startIndex = screen.index)
                }
            }
        }
    }
}
