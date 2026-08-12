package com.example.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.FontStyler

@Composable
fun PracticeWorkspaceCard(modifier: Modifier = Modifier) {
    var textInput by remember { mutableStateOf("") }
    var selectedFont by remember { mutableStateOf(FontStyler.KeyboardFont.NORMAL) }

    Card(
        modifier = modifier.testTag("practice_workspace_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⌨️ Font Test & Practice Workspace",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap below to test your active system font keyboard",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(modifier = Modifier.fillMaxWidth()) {
                items(FontStyler.KeyboardFont.values()) { font ->
                    val sample = FontStyler.styleText("Font", font)
                    FilterChip(
                        selected = selectedFont == font,
                        onClick = {
                            selectedFont = font
                            if (textInput.isNotEmpty()) {
                                textInput = FontStyler.styleText(textInput, font)
                            }
                        },
                        label = { Text(sample, fontSize = 12.sp) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Type here to test fonts & keyboard...") },
                modifier = Modifier.fillMaxWidth().height(110.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
