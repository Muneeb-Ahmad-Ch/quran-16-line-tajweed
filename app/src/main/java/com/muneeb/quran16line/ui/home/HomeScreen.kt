package com.muneeb.quran16line.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.muneeb.quran16line.data.Settings
import com.muneeb.quran16line.ui.AppViewModel
import com.muneeb.quran16line.ui.Screen

// Placeholder until Task 8.
@Composable
fun HomeScreen(vm: AppViewModel, s: Settings, open: (Screen) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { open(Screen.Reader(s.lastPage)) }) { Text("Resume p. ${s.lastPage}") }
    }
}
