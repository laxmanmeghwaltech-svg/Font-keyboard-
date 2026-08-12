package com.example.keyboard

import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.utils.CommonTypos
import com.example.utils.FontStyler

@Composable
fun AlphabetKeyboardLayout(
    inputConnection: InputConnection?,
    currentFontMode: FontStyler.KeyboardFont,
    isShiftActive: Boolean,
    showNumberRow: Boolean,
    autoCapEnabled: Boolean,
    autoCorrectEnabled: Boolean,
    hapticEnabled: Boolean,
    keyPopupEnabled: Boolean,
    keyHeight: Dp = 52.dp,
    onToggleShift: () -> Unit,
    onToggleSymbols: () -> Unit,
    onVoiceDictationClick: () -> Unit
) {
    val row0 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val row3 = listOf("z", "x", "c", "v", "b", "n", "m")

    fun getWordBeforeCursor(): String {
        if (inputConnection == null) return ""
        val textBefore = inputConnection.getTextBeforeCursor(30, 0)?.toString() ?: ""
        val lastSpace = textBefore.lastIndexOfAny(charArrayOf(' ', '\n', '\t'))
        return if (lastSpace != -1) textBefore.substring(lastSpace + 1) else textBefore
    }

    fun shouldAutoCapitalize(): Boolean {
        if (!autoCapEnabled || inputConnection == null) return false
        val textBefore = inputConnection.getTextBeforeCursor(2, 0)?.toString() ?: ""
        if (textBefore.isEmpty()) return true
        return textBefore.endsWith(". ") || textBefore.endsWith("! ") || textBefore.endsWith("? ") || textBefore.endsWith("\n")
    }

    fun handleKeyTap(charStr: String) {
        if (inputConnection == null) return
        val cap = isShiftActive || shouldAutoCapitalize()
        val rawChar = if (cap) charStr.uppercase() else charStr.lowercase()
        val styled = FontStyler.styleText(rawChar, currentFontMode)
        inputConnection.commitText(styled, 1)
    }

    fun handleSpaceTap() {
        if (inputConnection == null) return
        if (autoCorrectEnabled) {
            val word = getWordBeforeCursor()
            if (word.isNotBlank()) {
                val correction = CommonTypos.getCorrection(word)
                if (correction != null) {
                    inputConnection.deleteSurroundingText(word.length, 0)
                    val styledCorrection = FontStyler.styleText(correction, currentFontMode)
                    inputConnection.commitText(styledCorrection, 1)
                }
            }
        }
        inputConnection.commitText(" ", 1)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // Optional Number Row
        if (showNumberRow) {
            Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
                row0.forEach { num ->
                    KeyButton(
                        text = num,
                        modifier = Modifier.weight(1f),
                        height = keyHeight,
                        hapticEnabled = hapticEnabled,
                        showPopup = keyPopupEnabled,
                        onClick = { inputConnection?.commitText(num, 1) }
                    )
                }
            }
        }

        // Row 1
        Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
            row1.forEach { charStr ->
                val display = if (isShiftActive || (autoCapEnabled && shouldAutoCapitalize())) charStr.uppercase() else charStr
                val styledDisplay = FontStyler.styleText(display, currentFontMode)
                KeyButton(
                    text = styledDisplay,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    hapticEnabled = hapticEnabled,
                    showPopup = keyPopupEnabled,
                    onClick = { handleKeyTap(charStr) }
                )
            }
        }

        // Row 2
        Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
            Spacer(modifier = Modifier.weight(0.5f))
            row2.forEach { charStr ->
                val display = if (isShiftActive || (autoCapEnabled && shouldAutoCapitalize())) charStr.uppercase() else charStr
                val styledDisplay = FontStyler.styleText(display, currentFontMode)
                KeyButton(
                    text = styledDisplay,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    hapticEnabled = hapticEnabled,
                    showPopup = keyPopupEnabled,
                    onClick = { handleKeyTap(charStr) }
                )
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }

        // Row 3
        Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
            KeyButton(
                text = "⇧",
                modifier = Modifier.weight(1.5f),
                height = keyHeight,
                containerColor = if (isShiftActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isShiftActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                hapticEnabled = hapticEnabled,
                onClick = onToggleShift
            )

            row3.forEach { charStr ->
                val display = if (isShiftActive || (autoCapEnabled && shouldAutoCapitalize())) charStr.uppercase() else charStr
                val styledDisplay = FontStyler.styleText(display, currentFontMode)
                KeyButton(
                    text = styledDisplay,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    hapticEnabled = hapticEnabled,
                    showPopup = keyPopupEnabled,
                    onClick = { handleKeyTap(charStr) }
                )
            }

            KeyButton(
                text = "⌫",
                modifier = Modifier.weight(1.5f),
                height = keyHeight,
                hapticEnabled = hapticEnabled,
                onClick = {
                    inputConnection?.deleteSurroundingText(1, 0)
                }
            )
        }

        // Row 4 (Bottom Bar)
        Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
            KeyButton(
                text = "?123",
                modifier = Modifier.weight(1.5f),
                height = keyHeight,
                hapticEnabled = hapticEnabled,
                onClick = onToggleSymbols
            )

            KeyButton(
                text = "🎙️",
                modifier = Modifier.weight(1f),
                height = keyHeight,
                hapticEnabled = hapticEnabled,
                onClick = onVoiceDictationClick
            )

            KeyButton(
                text = "space",
                modifier = Modifier.weight(4.5f),
                height = keyHeight,
                hapticEnabled = hapticEnabled,
                onClick = { handleSpaceTap() }
            )

            KeyButton(
                text = ".",
                modifier = Modifier.weight(1f),
                height = keyHeight,
                hapticEnabled = hapticEnabled,
                onClick = { inputConnection?.commitText(".", 1) }
            )

            KeyButton(
                text = "↵",
                modifier = Modifier.weight(1.5f),
                height = keyHeight,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                hapticEnabled = hapticEnabled,
                onClick = {
                    inputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
                    inputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER))
                }
            )
        }
    }
}
