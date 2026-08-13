package com.example.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.FontStyler

@Composable
fun PracticeWorkspaceCard(
    textInput: String,
    onTextInputChange: (String) -> Unit,
    isInAppKeyboardOpen: Boolean,
    onToggleInAppKeyboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFont by remember { mutableStateOf(FontStyler.KeyboardFont.NORMAL) }

    Card(
        modifier = modifier.testTag("practice_workspace_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⌨️ Font Practice Workspace",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Type below or open the interactive keyboard preview",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(modifier = Modifier.fillMaxWidth()) {
                items(FontStyler.KeyboardFont.values()) { font ->
                    val sample = FontStyler.styleText("Font", font)
                    FilterChip(
                        selected = selectedFont == font,
                        onClick = {
                            selectedFont = font
                            if (textInput.isNotEmpty()) {
                                onTextInputChange(FontStyler.styleText(textInput, font))
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
                onValueChange = onTextInputChange,
                label = { Text("Type here to test custom fonts...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onToggleInAppKeyboard,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isInAppKeyboardOpen) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isInAppKeyboardOpen) Icons.Default.KeyboardHide else Icons.Default.Keyboard,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = if (isInAppKeyboardOpen) "Close Keyboard Preview" else "⌨️ Test Keyboard Right Here"
                )
            }
        }
    }
}

