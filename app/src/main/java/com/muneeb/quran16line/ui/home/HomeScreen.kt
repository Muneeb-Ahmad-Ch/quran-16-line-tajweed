package com.muneeb.quran16line.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import com.muneeb.quran16line.data.Surah
import com.muneeb.quran16line.data.SurahSearch
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muneeb.quran16line.data.Bookmark
import com.muneeb.quran16line.data.QuranIndex
import com.muneeb.quran16line.data.Settings
import com.muneeb.quran16line.pages.PageAssets
import com.muneeb.quran16line.pages.pageColorFilter
import com.muneeb.quran16line.pages.readerTargetWidth
import com.muneeb.quran16line.ui.AppViewModel
import com.muneeb.quran16line.ui.Screen
import com.muneeb.quran16line.ui.reader.BookmarkSheet
import com.muneeb.quran16line.ui.reader.effectivePageStyle
import kotlinx.coroutines.launch
import com.muneeb.quran16line.ui.safeBottomPadding
import com.muneeb.quran16line.ui.safeTopPadding

private val tabs = listOf("Surah", "Para", "Manzil", "Bookmarks")

@Composable
fun HomeScreen(vm: AppViewModel, s: Settings, dark: Boolean, open: (Screen) -> Unit) {
    val bookmarks by vm.bookmarks.collectAsStateWithLifecycle()
    var renaming by remember { mutableStateOf<Bookmark?>(null) }
    val pager = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().safeTopPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Quran 16 Line",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { open(Screen.Settings) }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }

        PrimaryTabRow(selectedTabIndex = pager.currentPage, containerColor = MaterialTheme.colorScheme.background) {
            tabs.forEachIndexed { i, title ->
                Tab(
                    selected = pager.currentPage == i,
                    onClick = { scope.launch { pager.animateScrollToPage(i) } },
                    text = {
                        // Shrinks to fit when the phone uses a large font size, instead of cutting the word off.
                        BasicText(
                            title,
                            maxLines = 1,
                            style = LocalTextStyle.current.copy(color = LocalContentColor.current),
                            autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = LocalTextStyle.current.fontSize),
                        )
                    },
                )
            }
        }

        // Swipe left/right between the lists, or tap a tab.
        HorizontalPager(pager, Modifier.weight(1f), beyondViewportPageCount = 1, key = { it }) { tab ->
            if (tab == 0) {
                SurahTab(open)
            } else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 8.dp)) {
                    when (tab) {
                        1 -> paraList(open)
                        2 -> manzilList(open)
                        else -> bookmarkList(bookmarks, open, onRename = { renaming = it }, onDelete = { vm.removeBookmark(it.page) })
                    }
                }
            }
        }

        // At the bottom, where a thumb reaches easily.
        ResumeCard(vm, s, dark) { open(Screen.Reader(s.lastPage)) }
    }

    renaming?.let { b ->
        BookmarkSheet(
            page = b.page,
            existing = b,
            onSave = { vm.saveBookmark(b.page, it); renaming = null },
            onRemove = { vm.removeBookmark(b.page); renaming = null },
            onDismiss = { renaming = null },
        )
    }
}

@Composable
private fun ResumeCard(vm: AppViewModel, s: Settings, dark: Boolean, onResume: () -> Unit) {
    val page = s.lastPage
    val path = PageAssets.page(page)
    var thumb by remember(path) { mutableStateOf<ImageBitmap?>(vm.images.cached(path, THUMB_WIDTH)) }
    LaunchedEffect(path) { if (thumb == null) thumb = vm.images.load(path, THUMB_WIDTH) }
    // Decode the resume page (and its neighbours) at reader size now, so Resume opens with no blank frame.
    val readerWidth = readerTargetWidth(LocalWindowInfo.current.containerSize.width, vm.images.manifest.page)
    LaunchedEffect(page, readerWidth) {
        val pages = listOf(page, page + 1, page - 1).filter { it in QuranIndex.FIRST_PAGE..QuranIndex.LAST_PAGE }
        vm.images.prefetch(pages.map { PageAssets.page(it) }, readerWidth)
    }
    val surah = QuranIndex.surahForPage(page)
    val para = QuranIndex.paraForPage(page)
    val aspect = vm.images.manifest.page.aspect

    Row(
        Modifier
            .safeBottomPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .clickable(onClick = onResume)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(46.dp)
                .aspectRatio(aspect)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            thumb?.let {
                Image(
                    it, contentDescription = null, contentScale = ContentScale.Fit,
                    colorFilter = pageColorFilter(effectivePageStyle(s, dark)),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                "CONTINUE READING",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
            )
            Text(surah.nameEn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "Para ${para.number} · Page $page",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Button(onClick = onResume, modifier = Modifier.padding(start = 8.dp).height(48.dp), contentPadding = PaddingValues(horizontal = 18.dp)) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Resume")
        }
    }
}

private const val THUMB_WIDTH = 200

@Composable
private fun IndexRow(number: Int, title: String, subtitle: String, arabic: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text("$number", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(arabic, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
    HorizontalDivider(Modifier.padding(start = 64.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun SurahTab(open: (Screen) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(query) { SurahSearch.search(query) }
    val keyboard = LocalSoftwareKeyboardController.current
    Column(Modifier.fillMaxSize()) {
        SearchField(query, onChange = { query = it }, onDone = { keyboard?.hide() })
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 8.dp)) {
            surahList(results, showExtras = query.isBlank(), open)
        }
    }
}

/** Slim one-line search pill; keeps its height at any font size. */
@Composable
private fun SearchField(query: String, onChange: (String) -> Unit, onDone: () -> Unit) {
    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
    BasicTextField(
        value = query,
        onValueChange = onChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onDone() }),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        decorationBox = { field ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(start = 14.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Search, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp),
                )
                Box(Modifier.weight(1f).padding(start = 10.dp), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            "Search Surah",
                            style = textStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    field()
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onChange("") }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
    )
}

private fun LazyListScope.surahList(surahs: List<Surah>, showExtras: Boolean, open: (Screen) -> Unit) {
    items(surahs, key = { "s${it.number}" }) { s ->
        IndexRow(s.number, s.nameEn, "Page ${s.startPage}", s.nameAr) { open(Screen.Reader(s.startPage)) }
    }
    if (showExtras) item(key = "extras") {
        Row(
            Modifier.fillMaxWidth().clickable { open(Screen.Extras(0)) }.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Extras", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(
                "Dua, stop signs, tajweed rules",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun LazyListScope.paraList(open: (Screen) -> Unit) {
    items(QuranIndex.paras, key = { "p${it.number}" }) { p ->
        IndexRow(p.number, p.nameEn, "Pages ${p.startPage}–${p.endPage}", p.nameAr) { open(Screen.Reader(p.startPage)) }
    }
}

private fun LazyListScope.manzilList(open: (Screen) -> Unit) {
    items(QuranIndex.manzils, key = { "m${it.number}" }) { m ->
        val surah = QuranIndex.surahs[m.startSurah - 1]
        IndexRow(m.number, "Manzil ${m.number}", "From ${surah.nameEn} · Page ${m.startPage}", surah.nameAr) {
            open(Screen.Reader(m.startPage))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.bookmarkList(
    bookmarks: List<Bookmark>,
    open: (Screen) -> Unit,
    onRename: (Bookmark) -> Unit,
    onDelete: (Bookmark) -> Unit,
) {
    if (bookmarks.isEmpty()) {
        item(key = "empty") {
            Text(
                "No bookmarks yet.\nWhile reading, tap ⋮ in the top corner and choose \"Bookmark this page\".",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(32.dp),
            )
        }
        return
    }
    items(bookmarks, key = { "b${it.page}" }) { b ->
        var menu by remember { mutableStateOf(false) }
        Row(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = { open(Screen.Reader(b.page)) }, onLongClick = { menu = true })
                .padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(b.name, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    "Page ${b.page} · ${QuranIndex.surahForPage(b.page).nameEn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "Bookmark options") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Rename") }, onClick = { menu = false; onRename(b) })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; onDelete(b) })
                }
            }
        }
        HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
    }
}
