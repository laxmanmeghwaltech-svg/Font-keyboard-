package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TextShortcut

@Composable
fun TextShortcutManagerCard(
    shortcuts: List<TextShortcut>,
    onAddShortcut: (String, String) -> Unit,
    onToggleShortcut: (TextShortcut) -> Unit,
    onDeleteShortcut: (TextShortcut) -> Unit,
    modifier: Modifier = Modifier
) {
    var shortcutTag by remember { mutableStateOf("") }
    var expansionText by remember { mutableStateOf("") }

    Card(
        modifier = modifier.testTag("text_shortcut_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ Text Shortcuts & Auto-Expander",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Type short tags to quickly paste long sentences",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = shortcutTag,
                    onValueChange = { shortcutTag = it },
                    label = { Text("Shortcut (e.g. omw)") },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                )
                OutlinedTextField(
                    value = expansionText,
                    onValueChange = { expansionText = it },
                    label = { Text("Full Expansion") },
                    modifier = Modifier.weight(1.5f).padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (shortcutTag.isNotBlank() && expansionText.isNotBlank()) {
                        onAddShortcut(shortcutTag.trim(), expansionText.trim())
                        shortcutTag = ""
                        expansionText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Add Text Shortcut")
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (shortcuts.isEmpty()) {
                Text(
                    text = "No shortcuts configured yet.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    items(shortcuts) { shortcut ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = shortcut.shortcut,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = " ➔ ${shortcut.expansion}",
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                                )
                                Switch(
                                    checked = shortcut.isEnabled,
                                    onCheckedChange = { onToggleShortcut(shortcut) }
                                )
                                IconButton(onClick = { onDeleteShortcut(shortcut) }) {
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
}
