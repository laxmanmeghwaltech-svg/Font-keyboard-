package com.example.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import coil.compose.AsyncImage
import com.example.data.CustomEmoji

@Composable
fun CustomEmojiStickerCard(
    customEmojis: List<CustomEmoji>,
    onEmojiUploaded: (Uri, String) -> Unit,
    onDeleteEmoji: (CustomEmoji) -> Unit,
    modifier: Modifier = Modifier
) {
    var emojiName by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                selectedUri = uri
            }
        }
    )

    Card(
        modifier = modifier.testTag("custom_emoji_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎨 Custom PNG Sticker & Emoji Pack",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Import PNG images as custom keyboard stickers",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = emojiName,
                onValueChange = { emojiName = it },
                label = { Text("Sticker Name / Tag") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Select Image")
                }

                if (selectedUri != null && emojiName.isNotBlank()) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        onClick = {
                            onEmojiUploaded(selectedUri!!, emojiName.trim())
                            selectedUri = null
                            emojiName = ""
                        }
                    ) {
                        Text("Save Sticker")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (customEmojis.isEmpty()) {
                Text(
                    text = "No custom stickers uploaded.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                    items(customEmojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = emoji.filePath,
                                    contentDescription = emoji.name,
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp))
                                )
                                Text(
                                    text = emoji.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f).padding(start = 12.dp)
                                )
                                IconButton(onClick = { onDeleteEmoji(emoji) }) {
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
