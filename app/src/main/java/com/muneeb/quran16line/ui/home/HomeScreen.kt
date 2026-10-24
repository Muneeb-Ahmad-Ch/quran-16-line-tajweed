package com.muneeb.quran16line.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import com.muneeb.quran16line.ui.AppViewModel
import com.muneeb.quran16line.ui.Screen
import com.muneeb.quran16line.ui.reader.BookmarkSheet
import com.muneeb.quran16line.ui.reader.effectivePageStyle

private val tabs = listOf("Surah", "Para", "Manzil", "Bookmarks")

@Composable
fun HomeScreen(vm: AppViewModel, s: Settings, dark: Boolean, open: (Screen) -> Unit) {
    val bookmarks by vm.bookmarks.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var renaming by remember { mutableStateOf<Bookmark?>(null) }
    val listStates = tabs.map { rememberLazyListState() }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
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

        ResumeCard(vm, s, dark) { open(Screen.Reader(s.lastPage)) }

        PrimaryTabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.background) {
            tabs.forEachIndexed { i, title ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title, maxLines = 1) })
            }
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            state = listStates[tab],
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            when (tab) {
                0 -> surahList(open)
                1 -> paraList(open)
                2 -> manzilList(open)
                else -> bookmarkList(bookmarks, open, onRename = { renaming = it }, onDelete = { vm.removeBookmark(it.page) })
            }
            item { Spacer(Modifier.navigationBarsPadding()) }
        }
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
    val path = PageAssets.page(s.version, page)
    var thumb by remember(path) { mutableStateOf<ImageBitmap?>(vm.images.cached(path, THUMB_WIDTH)) }
    LaunchedEffect(path) { if (thumb == null) thumb = vm.images.load(path, THUMB_WIDTH) }
    val surah = QuranIndex.surahForPage(page)
    val para = QuranIndex.paraForPage(page)
    val aspect = vm.images.manifest.of(s.version).aspect

    Row(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .clickable(onClick = onResume)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(64.dp)
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
            Text(surah.nameEn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Para ${para.number} · ${para.nameEn} · Page $page",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Button(onClick = onResume, modifier = Modifier.padding(top = 8.dp).height(36.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Resume")
            }
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

private fun LazyListScope.surahList(open: (Screen) -> Unit) {
    items(QuranIndex.surahs, key = { "s${it.number}" }) { s ->
        IndexRow(s.number, s.nameEn, "Page ${s.startPage}", s.nameAr) { open(Screen.Reader(s.startPage)) }
    }
    item(key = "extras") {
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

private fun LazyListScope.bookmarkList(
    bookmarks: List<Bookmark>,
    open: (Screen) -> Unit,
    onRename: (Bookmark) -> Unit,
    onDelete: (Bookmark) -> Unit,
) {
    if (bookmarks.isEmpty()) {
        item(key = "empty") {
            Text(
                "No bookmarks yet.\nWhile reading, tap the bookmark button in the corner to save a page.",
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
            Modifier.fillMaxWidth().clickable { open(Screen.Reader(b.page)) }.padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
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
