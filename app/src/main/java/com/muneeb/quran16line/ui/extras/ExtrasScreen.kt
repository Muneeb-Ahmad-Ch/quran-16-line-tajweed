package com.muneeb.quran16line.ui.extras

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.muneeb.quran16line.data.Settings
import com.muneeb.quran16line.pages.PageAssets
import com.muneeb.quran16line.pages.letterboxColor
import com.muneeb.quran16line.pages.pageColorFilter
import com.muneeb.quran16line.ui.AppViewModel
import com.muneeb.quran16line.ui.reader.ImmersiveMode
import com.muneeb.quran16line.ui.reader.ZoomablePage
import com.muneeb.quran16line.ui.reader.effectivePageStyle

/** The coloured edition's extra pages (Dua, stop signs, tajweed rules...). No bookmarks, no resume. */
@Composable
fun ExtrasScreen(vm: AppViewModel, s: Settings, dark: Boolean, startIndex: Int) {
    val count = vm.images.manifest.extras.count
    val style = effectivePageStyle(s, dark)
    val filter = pageColorFilter(style)
    val pager = rememberPagerState(initialPage = startIndex) { count }
    ImmersiveMode()
    BoxWithConstraints(Modifier.fillMaxSize().background(letterboxColor(Color.White, style))) {
        val targetWidth = with(LocalDensity.current) { maxWidth.roundToPx() }.coerceAtMost(1200)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            HorizontalPager(state = pager, beyondViewportPageCount = 1, modifier = Modifier.fillMaxSize()) { i ->
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ZoomablePage(PageAssets.extra(i + 1), vm.images, targetWidth, filter)
                }
            }
        }
    }
}
