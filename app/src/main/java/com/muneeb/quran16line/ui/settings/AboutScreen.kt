package com.muneeb.quran16line.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.muneeb.quran16line.BuildConfig
import com.muneeb.quran16line.ui.safeBottomPadding
import com.muneeb.quran16line.ui.safeTopPadding

private val credits = listOf(
    "Found a mistake?" to "This app was made with great care, but humans make mistakes. If you notice a missing or " +
        "misplaced page, a wrong Surah, Para or page number, or any bug, please report it on the project's " +
        "GitHub page (Issues → Report a mistake) so it can be corrected. If there is any mistake, it is " +
        "completely unintentional: no Muslim could ever think of making a mistake in the Quran on purpose. " +
        "May Allah forgive our shortcomings and accept this humble effort. Ameen.",
    "Mushaf" to "16-line IndoPak Mushaf with colour-coded tajweed, 548 pages (pages 2–549).",
    "Source" to "“Quran 16 Line” by Qamar Apps (stated in the PDF as CC BY-SA 4.0). " +
        "Printed edition credited to Pak Company (registered), March 2011, proofread by the Punjab Auqaf department. " +
        "PDFs downloaded from myonlinestudent.wordpress.com/read-quran.",
    "Changes made" to "Pages were trimmed to their frame, aligned and resized for phone screens. The text is unchanged.",
    "Takedown" to "The original publisher of these scans could not be fully confirmed. " +
        "If you hold rights to this material and want it removed, please open an issue on the project's GitHub page " +
        "and it will be taken down.",
    "App" to "Version ${BuildConfig.VERSION_NAME}. Free, offline, no ads, no tracking, no internet permission.",
)

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().safeTopPadding()) {
        TopBar("Credits & sources", onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).safeBottomPadding(),
        ) {
            credits.forEach { (title, body) ->
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp))
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
