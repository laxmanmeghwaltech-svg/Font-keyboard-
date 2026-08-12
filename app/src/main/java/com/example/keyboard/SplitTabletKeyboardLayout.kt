package com.example.keyboard

import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.utils.CommonTypos
import com.example.utils.FontStyler

@Composable
fun SplitTabletKeyboardLayout(
    inputConnection: InputConnection?,
    currentFontMode: FontStyler.KeyboardFont,
    isShiftActive: Boolean,
    autoCapEnabled: Boolean,
    autoCorrectEnabled: Boolean,
    hapticEnabled: Boolean,
    keyPopupEnabled: Boolean,
    keyHeight: Dp = 64.dp,
    onToggleShift: () -> Unit,
    onToggleSymbols: () -> Unit,
    onVoiceDictationClick: () -> Unit
) {
    val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")

    val leftRow1 = listOf("q", "w", "e", "r", "t")
    val rightRow1 = listOf("y", "u", "i", "o", "p")

    val leftRow2 = listOf("a", "s", "d", "f", "g")
    val rightRow2 = listOf("h", "j", "k", "l")

    val leftRow3 = listOf("z", "x", "c", "v")
    val rightRow3 = listOf("b", "n", "m")

    fun sendArrow(keyCode: Int) {
        inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    fun handleKeyTap(charStr: String) {
        if (inputConnection == null) return
        val rawChar = if (isShiftActive) charStr.uppercase() else charStr.lowercase()
        val styled = FontStyler.styleText(rawChar, currentFontMode)
        inputConnection.commitText(styled, 1)
    }

    fun handleSpaceTap() {
        if (inputConnection == null) return
        if (autoCorrectEnabled) {
            val textBefore = inputConnection.getTextBeforeCursor(30, 0)?.toString() ?: ""
            val lastSpace = textBefore.lastIndexOfAny(charArrayOf(' ', '\n', '\t'))
            val word = if (lastSpace != -1) textBefore.substring(lastSpace + 1) else textBefore
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
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Top Center Number Row across full width
        Row(
            modifier = Modifier.fillMaxWidth().height(keyHeight),
            horizontalArrangement = Arrangement.Center
        ) {
            numbers.forEach { num ->
                KeyButton(
                    text = num,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    hapticEnabled = hapticEnabled,
                    onClick = { inputConnection?.commitText(num, 1) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Split Left and Right halves with >= 120dp center gap
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // LEFT HALF
            Column(modifier = Modifier.weight(1f)) {
                // Left Row 1
                Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
                    leftRow1.forEach { charStr ->
                        val display = FontStyler.styleText(if (isShiftActive) charStr.uppercase() else charStr, currentFontMode)
                        KeyButton(
                            text = display,
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            hapticEnabled = hapticEnabled,
                            showPopup = keyPopupEnabled,
                            onClick = { handleKeyTap(charStr) }
                        )
                    }
                }
                // Left Row 2
                Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
                    leftRow2.forEach { charStr ->
                        val display = FontStyler.styleText(if (isShiftActive) charStr.uppercase() else charStr, currentFontMode)
                        KeyButton(
                            text = display,
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            hapticEnabled = hapticEnabled,
                            showPopup = keyPopupEnabled,
                            onClick = { handleKeyTap(charStr) }
                        )
                    }
                }
                // Left Row 3
                Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
                    KeyButton(
                        text = "⇧",
                        modifier = Modifier.weight(1.2f),
                        height = keyHeight,
                        containerColor = if (isShiftActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isShiftActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        hapticEnabled = hapticEnabled,
                        onClick = onToggleShift
                    )
                    leftRow3.forEach { charStr ->
                        val display = FontStyler.styleText(if (isShiftActive) charStr.uppercase() else charStr, currentFontMode)
                        KeyButton(
                            text = display,
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            hapticEnabled = hapticEnabled,
                            showPopup = keyPopupEnabled,
                            onClick = { handleKeyTap(charStr) }
                        )
                    }
                }
                // Left Row 4 (Bottom)
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
                        modifier = Modifier.weight(2.5f),
                        height = keyHeight,
                        hapticEnabled = hapticEnabled,
                        onClick = { handleSpaceTap() }
                    )
                }
            }

            // CENTER GAP (Comfortable 130dp gap for thumb typing)
            Spacer(modifier = Modifier.width(130.dp))

            // RIGHT HALF
            Column(modifier = Modifier.weight(1f)) {
                // Right Row 1
                Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
                    rightRow1.forEach { charStr ->
                        val display = FontStyler.styleText(if (isShiftActive) charStr.uppercase() else charStr, currentFontMode)
                        KeyButton(
                            text = display,
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            hapticEnabled = hapticEnabled,
                            showPopup = keyPopupEnabled,
                            onClick = { handleKeyTap(charStr) }
                        )
                    }
                }
                // Right Row 2
                Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
                    rightRow2.forEach { charStr ->
                        val display = FontStyler.styleText(if (isShiftActive) charStr.uppercase() else charStr, currentFontMode)
                        KeyButton(
                            text = display,
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            hapticEnabled = hapticEnabled,
                            showPopup = keyPopupEnabled,
                            onClick = { handleKeyTap(charStr) }
                        )
                    }
                    KeyButton(
                        text = "⌫",
                        modifier = Modifier.weight(1.2f),
                        height = keyHeight,
                        hapticEnabled = hapticEnabled,
                        onClick = { inputConnection?.deleteSurroundingText(1, 0) }
                    )
                }
                // Right Row 3
                Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
                    rightRow3.forEach { charStr ->
                        val display = FontStyler.styleText(if (isShiftActive) charStr.uppercase() else charStr, currentFontMode)
                        KeyButton(
                            text = display,
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            hapticEnabled = hapticEnabled,
                            showPopup = keyPopupEnabled,
                            onClick = { handleKeyTap(charStr) }
                        )
                    }
                    KeyButton(
                        text = "↵",
                        modifier = Modifier.weight(1.5f),
                        height = keyHeight,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        hapticEnabled = hapticEnabled,
                        onClick = {
                            sendArrow(KeyEvent.KEYCODE_ENTER)
                        }
                    )
                }
                // Right Row 4 (Bottom with Arrow Navigation Keys)
                Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
                    KeyButton(
                        text = "space",
                        modifier = Modifier.weight(1.5f),
                        height = keyHeight,
                        hapticEnabled = hapticEnabled,
                        onClick = { handleSpaceTap() }
                    )
                    KeyButton(
                        text = "←",
                        modifier = Modifier.weight(1f),
                        height = keyHeight,
                        hapticEnabled = hapticEnabled,
                        onClick = { sendArrow(KeyEvent.KEYCODE_DPAD_LEFT) }
                    )
                    KeyButton(
                        text = "↓",
                        modifier = Modifier.weight(1f),
                        height = keyHeight,
                        hapticEnabled = hapticEnabled,
                        onClick = { sendArrow(KeyEvent.KEYCODE_DPAD_DOWN) }
                    )
                    KeyButton(
                        text = "↑",
                        modifier = Modifier.weight(1f),
                        height = keyHeight,
                        hapticEnabled = hapticEnabled,
                        onClick = { sendArrow(KeyEvent.KEYCODE_DPAD_UP) }
                    )
                    KeyButton(
                        text = "→",
                        modifier = Modifier.weight(1f),
                        height = keyHeight,
                        hapticEnabled = hapticEnabled,
                        onClick = { sendArrow(KeyEvent.KEYCODE_DPAD_RIGHT) }
                    )
                }
            }
        }
    }
}
