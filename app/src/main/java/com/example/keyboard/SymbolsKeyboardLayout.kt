package com.example.keyboard

import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SymbolsKeyboardLayout(
    inputConnection: InputConnection?,
    hapticEnabled: Boolean,
    keyHeight: Dp = 52.dp,
    onBackToABC: () -> Unit
) {
    val row1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val row2 = listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/")
    val row3 = listOf("*", "\"", "'", ":", ";", "!", "?", "~", "`", "|")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
            row1.forEach { charStr ->
                KeyButton(
                    text = charStr,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    hapticEnabled = hapticEnabled,
                    onClick = { inputConnection?.commitText(charStr, 1) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
            row2.forEach { charStr ->
                KeyButton(
                    text = charStr,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    hapticEnabled = hapticEnabled,
                    onClick = { inputConnection?.commitText(charStr, 1) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
            row3.forEach { charStr ->
                KeyButton(
                    text = charStr,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    hapticEnabled = hapticEnabled,
                    onClick = { inputConnection?.commitText(charStr, 1) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
            KeyButton(
                text = "ABC",
                modifier = Modifier.weight(2f),
                height = keyHeight,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                hapticEnabled = hapticEnabled,
                onClick = onBackToABC
            )

            KeyButton(
                text = "space",
                modifier = Modifier.weight(5f),
                height = keyHeight,
                hapticEnabled = hapticEnabled,
                onClick = { inputConnection?.commitText(" ", 1) }
            )

            KeyButton(
                text = "⌫",
                modifier = Modifier.weight(2f),
                height = keyHeight,
                hapticEnabled = hapticEnabled,
                onClick = { inputConnection?.deleteSurroundingText(1, 0) }
            )
        }
    }
}
