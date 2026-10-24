package com.muneeb.quran16line.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muneeb.quran16line.data.AppTheme
import com.muneeb.quran16line.data.PageStyle
import com.muneeb.quran16line.data.Settings
import com.muneeb.quran16line.pages.PageAssets
import com.muneeb.quran16line.pages.letterboxColor
import com.muneeb.quran16line.pages.pageColorFilter
import com.muneeb.quran16line.ui.AppViewModel
import com.muneeb.quran16line.ui.Screen
import com.muneeb.quran16line.ui.safeBottomPadding
import com.muneeb.quran16line.ui.safeTopPadding

private const val THUMB_WIDTH = 240

@Composable
fun SettingsScreen(vm: AppViewModel, s: Settings, dark: Boolean, onBack: () -> Unit, open: (Screen) -> Unit) {
    Column(Modifier.fillMaxSize().safeTopPadding()) {
        TopBar("Settings", onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp).safeBottomPadding()) {
            Section("Appearance") {
                Item("App theme") {
                    Segmented(
                        options = listOf(AppTheme.LIGHT to "Light", AppTheme.DARK to "Dark", AppTheme.SYSTEM to "System"),
                        selected = s.theme,
                    ) { t -> vm.updateSettings { it.copy(theme = t) } }
                }
                Divider()
                SwitchItem(
                    "Dark mode on Quran pages too",
                    "Off = only menus go dark, pages stay as printed",
                    s.darkPages,
                ) { on -> vm.updateSettings { it.copy(darkPages = on) } }
                Divider()
                Item("Quran page style in dark mode") {
                    StyleThumbs(vm, s) { st -> vm.updateSettings { it.copy(pageStyle = st) } }
                }
            }
            Section("Reading") {
                SwitchItem(
                    "Open last page on start",
                    "Skip the home screen and continue reading straight away",
                    s.autoResume,
                ) { on -> vm.updateSettings { it.copy(autoResume = on) } }
                Divider()
                SwitchItem(
                    "Stretch page to fill screen",
                    "Uses the empty space above and below the page. Text looks taller than in print.",
                    s.stretchPages,
                ) { on -> vm.updateSettings { it.copy(stretchPages = on) } }
                Divider()
                SwitchItem("Keep screen on while reading", null, s.keepScreenOn) { on ->
                    vm.updateSettings { it.copy(keepScreenOn = on) }
                }
            }
            Section("About") {
                Row(
                    Modifier.fillMaxWidth().clickable { open(Screen.About) }.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Credits & sources", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            "Pak Company · Qamar Apps · myonlinestudent",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 26.dp, top = 18.dp, bottom = 6.dp),
    )
    Column(
        Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        content = content,
    )
}

@Composable
private fun Divider() = HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

@Composable
private fun Item(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun SwitchItem(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun <T> Segmented(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { i, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(i, options.size),
                label = { Text(label, maxLines = 1) },
            )
        }
    }
}

@Composable
private fun StyleThumbs(vm: AppViewModel, s: Settings, onSelect: (PageStyle) -> Unit) {
    val path = PageAssets.page(s.lastPage)
    var thumb by remember(path) { mutableStateOf<ImageBitmap?>(vm.images.cached(path, THUMB_WIDTH)) }
    LaunchedEffect(path) { if (thumb == null) thumb = vm.images.load(path, THUMB_WIDTH) }
    val info = vm.images.manifest.page
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PageStyle.entries.forEach { style ->
            val selected = style == s.pageStyle
            Column(Modifier.weight(1f).clickable { onSelect(style) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(info.aspect)
                        .clip(RoundedCornerShape(6.dp))
                        .background(letterboxColor(Color(info.bgArgb), style))
                        .border(
                            if (selected) 2.5.dp else 1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(6.dp),
                        ),
                ) {
                    thumb?.let {
                        Image(
                            it, contentDescription = null, contentScale = ContentScale.Fit,
                            colorFilter = pageColorFilter(style), modifier = Modifier.fillMaxSize().padding(2.dp),
                        )
                    }
                }
                Text(
                    style.label(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

private fun PageStyle.label() = when (this) {
    PageStyle.ORIGINAL -> "Original"
    PageStyle.NIGHT -> "Night"
    PageStyle.INVERT -> "Invert"
    PageStyle.WARM -> "Warm"
}
