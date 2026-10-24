package com.muneeb.quran16line.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.muneeb.quran16line.data.Bookmark
import com.muneeb.quran16line.data.QuranIndex

/** Save / rename / remove the bookmark for [page]. The default name is pre-filled. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkSheet(
    page: Int,
    existing: Bookmark?,
    onSave: (String) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    val default = QuranIndex.defaultBookmarkName(page)
    val initial = existing?.name ?: default
    var text by remember(page) { mutableStateOf(TextFieldValue(initial, TextRange(initial.length))) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 20.dp).imePadding()) {
            Text(
                if (existing == null) "Bookmark this page" else "Edit bookmark",
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            Text(
                if (existing == null) "Default name filled in. Edit it or just tap Save." else "Page $page",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                if (existing != null) {
                    TextButton(onClick = onRemove) { Text("Remove", color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.weight(1f))
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onSave(text.text.trim().ifEmpty { default }) }) { Text("Save") }
            }
        }
    }
}
