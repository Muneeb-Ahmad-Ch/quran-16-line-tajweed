package com.muneeb.quran16line.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muneeb.quran16line.data.PageStyle
import com.muneeb.quran16line.data.QuranIndex
import com.muneeb.quran16line.data.Settings
import com.muneeb.quran16line.pages.PageAssets
import com.muneeb.quran16line.pages.letterboxColor
import com.muneeb.quran16line.pages.readerTargetWidth
import com.muneeb.quran16line.pages.pageColorFilter
import com.muneeb.quran16line.ui.AppIcons
import com.muneeb.quran16line.ui.AppViewModel
import kotlinx.coroutines.delay

private const val PREFETCH_RADIUS = 2

/** Effective page style: dark styles only when the app is dark and "dark mode on pages" is on. */
fun effectivePageStyle(s: Settings, dark: Boolean): PageStyle =
    if (dark && s.darkPages) s.pageStyle else PageStyle.ORIGINAL

@Composable
fun ReaderScreen(vm: AppViewModel, s: Settings, dark: Boolean, startPage: Int) {
    val images = vm.images
    val info = images.manifest.of(s.version)
    val style = effectivePageStyle(s, dark)
    val filter = pageColorFilter(style)
    val background = letterboxColor(Color(info.bgArgb), style)
    val bookmarks by vm.bookmarks.collectAsStateWithLifecycle()
    val pager = rememberPagerState(initialPage = startPage - QuranIndex.FIRST_PAGE) { QuranIndex.PAGE_COUNT }
    var sheetFor by remember { mutableStateOf<Int?>(null) }
    var savedName by remember { mutableStateOf<String?>(null) }

    ImmersiveMode()
    KeepScreenOn(s.keepScreenOn)

    Box(Modifier.fillMaxSize().background(background)) {
        val targetWidth = readerTargetWidth(LocalWindowInfo.current.containerSize.width, info)

        // Right-to-left like a printed Mushaf: swiping right brings the next page.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            HorizontalPager(
                state = pager,
                beyondViewportPageCount = 1,
                key = { it },
                modifier = Modifier.fillMaxSize(),
            ) { index ->
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ZoomablePage(PageAssets.page(s.version, index + QuranIndex.FIRST_PAGE), images, targetWidth, filter)
                }
            }
        }

        LaunchedEffect(pager.settledPage, s.version, targetWidth) {
            val page = pager.settledPage + QuranIndex.FIRST_PAGE
            vm.onPageShown(page)
            val around = (page - PREFETCH_RADIUS..page + PREFETCH_RADIUS)
                .filter { it in QuranIndex.FIRST_PAGE..QuranIndex.LAST_PAGE && it != page }
                .sortedBy { kotlin.math.abs(it - page) }
            images.prefetch(around.map { PageAssets.page(s.version, it) }, targetWidth)
        }

        val currentPage = pager.currentPage + QuranIndex.FIRST_PAGE
        val marked = bookmarks.any { it.page == currentPage }
        Box(
            Modifier
                .align(AbsoluteAlignment.TopLeft)
                .windowInsetsPadding(WindowInsets.displayCutout)
                .padding(12.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(if (marked) MaterialTheme.colorScheme.primary.copy(alpha = 0.75f) else Color.Black.copy(alpha = 0.28f))
                .clickable { sheetFor = currentPage },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (marked) AppIcons.Bookmark else AppIcons.BookmarkBorder,
                contentDescription = if (marked) "Edit bookmark" else "Bookmark this page",
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }

        AnimatedVisibility(
            visible = savedName != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 28.dp),
        ) {
            Text(
                "Saved: ${savedName.orEmpty()}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xD91E1E1E))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }
    }

    LaunchedEffect(savedName) {
        if (savedName != null) {
            delay(1500)
            savedName = null
        }
    }

    sheetFor?.let { page ->
        BookmarkSheet(
            page = page,
            existing = bookmarks.firstOrNull { it.page == page },
            onSave = { name ->
                vm.saveBookmark(page, name)
                savedName = name
                sheetFor = null
            },
            onRemove = {
                vm.removeBookmark(page)
                sheetFor = null
            },
            onDismiss = { sheetFor = null },
        )
    }
}
