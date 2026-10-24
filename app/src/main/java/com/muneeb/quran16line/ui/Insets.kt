package com.muneeb.quran16line.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Space for whichever is bigger, the system bars or the camera cutout. Some phones hide the
// status bar after full-screen reading, or report it shorter than the punch-hole, so status-bar
// padding alone can leave content under the hole.
private val safeArea: WindowInsets
    @Composable get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)

@Composable
fun Modifier.safeTopPadding(): Modifier =
    windowInsetsPadding(safeArea.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))

@Composable
fun Modifier.safeBottomPadding(): Modifier = windowInsetsPadding(safeArea.only(WindowInsetsSides.Bottom))
