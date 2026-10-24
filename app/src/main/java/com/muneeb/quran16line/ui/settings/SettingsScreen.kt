package com.muneeb.quran16line.ui.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.muneeb.quran16line.data.Settings
import com.muneeb.quran16line.ui.AppViewModel
import com.muneeb.quran16line.ui.Screen

// Placeholder until Task 9.
@Composable
fun SettingsScreen(vm: AppViewModel, s: Settings, dark: Boolean, onBack: () -> Unit, open: (Screen) -> Unit) {
    Text("Settings")
}
