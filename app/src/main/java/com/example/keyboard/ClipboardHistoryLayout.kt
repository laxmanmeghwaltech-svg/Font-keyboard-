package com.example.keyboard

import android.view.inputmethod.InputConnection
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardItem

@Composable
fun ClipboardHistoryLayout(
    inputConnection: InputConnection?,
    clipboardItems: List<ClipboardItem>,
    onPinItem: (ClipboardItem) -> Unit,
    onDeleteItem: (ClipboardItem) -> Unit,
    onClearUnpinned: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clipboard History",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(onClick = onClearUnpinned) {
                Text("Clear Unpinned", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (clipboardItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Clipboard is empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(clipboardItems) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (item.isPinned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                inputConnection?.commitText(item.text, 1)
                            }
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.text,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                fontSize = 14.sp
                            )
                            IconButton(onClick = { onPinItem(item) }) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "Pin",
                                    tint = if (item.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteItem(item) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
