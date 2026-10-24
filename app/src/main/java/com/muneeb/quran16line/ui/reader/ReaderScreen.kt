package com.muneeb.quran16line.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.ui.draw.scale
import com.muneeb.quran16line.ui.Screen
import com.muneeb.quran16line.ui.settings.label
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
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
import androidx.compose.ui.graphics.luminance
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

/** The style pages are actually drawn in: AUTO resolves from the app theme. */
fun effectivePageStyle(s: Settings, dark: Boolean): PageStyle =
    when (s.pageStyle) {
        PageStyle.AUTO -> if (dark) PageStyle.NIGHT else PageStyle.ORIGINAL
        else -> s.pageStyle
    }

@Composable
fun ReaderScreen(vm: AppViewModel, s: Settings, dark: Boolean, startPage: Int, onHome: () -> Unit, open: (Screen) -> Unit) {
    val images = vm.images
    val info = images.manifest.page
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
                // Keep pages clear of a punch-hole / notch; the letterbox colour still fills behind it.
                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.displayCutout),
            ) { index ->
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ZoomablePage(PageAssets.page(index + QuranIndex.FIRST_PAGE), images, targetWidth, filter, s.stretchPages)
                }
            }
        }

        LaunchedEffect(pager.settledPage, targetWidth) {
            val page = pager.settledPage + QuranIndex.FIRST_PAGE
            vm.onPageShown(page)
            val around = (page - PREFETCH_RADIUS..page + PREFETCH_RADIUS)
                .filter { it in QuranIndex.FIRST_PAGE..QuranIndex.LAST_PAGE && it != page }
                .sortedBy { kotlin.math.abs(it - page) }
            images.prefetch(around.map { PageAssets.page(it) }, targetWidth)
        }

        val currentPage = pager.currentPage + QuranIndex.FIRST_PAGE
        val marked = bookmarks.any { it.page == currentPage }
        ReaderMenu(
            s = s,
            darkPage = background.luminance() < 0.5f,
            marked = marked,
            onBookmark = { sheetFor = currentPage },
            onIndex = onHome,
            onSettings = { open(Screen.Settings) },
            update = vm::updateSettings,
        )

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

/**
 * The only control on the reading screen: a deliberately faint ⋮ in the corner (soft green when the
 * page is bookmarked), on a soft backdrop matching the page so it reads over busy borders. Touch target 44dp.
 */
@Composable
private fun BoxScope.ReaderMenu(
    s: Settings,
    darkPage: Boolean,
    marked: Boolean,
    onBookmark: () -> Unit,
    onIndex: () -> Unit,
    onSettings: () -> Unit,
    update: ((Settings) -> Settings) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box(
        Modifier
            .align(AbsoluteAlignment.TopLeft)
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
            .padding(10.dp),
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).clickable { open = true },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(when {
                            marked -> MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                            darkPage -> Color.Black.copy(alpha = 0.45f)
                            else -> Color.White.copy(alpha = 0.70f) // soft backdrop so it reads over busy borders
                        }),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Menu",
                    tint = if (marked) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f) else Color.Gray.copy(alpha = 0.75f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(if (marked) "Edit bookmark" else "Bookmark this page") },
                leadingIcon = { Icon(if (marked) AppIcons.Bookmark else AppIcons.BookmarkBorder, contentDescription = null) },
                onClick = { open = false; onBookmark() },
            )
            DropdownMenuItem(
                text = { Text("Surah / Para index") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                onClick = { open = false; onIndex() },
            )
            HorizontalDivider()
            Text(
                "Page style",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 2.dp),
            )
            PageStyle.entries.forEach { style ->
                DropdownMenuItem(
                    text = { Text(style.label()) },
                    leadingIcon = { RadioButton(selected = s.pageStyle == style, onClick = null) },
                    onClick = { update { it.copy(pageStyle = style) } },
                    modifier = Modifier.height(40.dp),
                )
            }
            HorizontalDivider()
            ToggleItem("Fill screen", s.stretchPages) {
                update { it.copy(stretchPages = !it.stretchPages) }
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Settings") },
                leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                onClick = { open = false; onSettings() },
            )
        }
    }
}

@Composable
private fun ToggleItem(label: String, checked: Boolean, onToggle: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        trailingIcon = { Switch(checked = checked, onCheckedChange = null, modifier = Modifier.scale(0.75f)) },
        onClick = onToggle,
    )
}
