package com.muneeb.quran16line.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val credits = listOf(
    "Mushaf" to "16-line IndoPak Mushaf, 548 pages (pages 2–549).",
    "Tajweed coloured edition" to "“Quran 16 Line” by Qamar Apps (stated in the PDF as CC BY-SA 4.0). " +
        "Printed edition credited to Pak Company (registered), March 2011, proofread by the Punjab Auqaf department. " +
        "PDFs downloaded from myonlinestudent.wordpress.com/read-quran.",
    "Plain edition" to "PDFs downloaded from tanzilone.wordpress.com. Original publisher not stated.",
    "Changes made" to "Pages were trimmed to their frame, aligned and resized for phone screens. The text is unchanged.",
    "Takedown" to "The original publishers of these scans could not be fully identified. " +
        "If you hold rights to this material and want it removed, please open an issue on the project's GitHub page " +
        "and it will be taken down.",
    "App" to "Free, offline, no ads, no tracking, no internet permission.",
)

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        TopBar("Credits & sources", onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding(),
        ) {
            credits.forEach { (title, body) ->
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp))
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
