package com.example.keyboard

import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TextEditingLayout(
    inputConnection: InputConnection?,
    hapticEnabled: Boolean,
    keyHeight: Dp = 52.dp
) {
    fun sendKey(keyCode: Int) {
        inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("DPAD & Cursor Control Panel")
        Spacer(modifier = Modifier.height(8.dp))

        // Row 1: UP
        Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
            Spacer(modifier = Modifier.weight(1f))
            KeyButton(
                text = "▲",
                modifier = Modifier.weight(1f),
                height = keyHeight,
                hapticEnabled = hapticEnabled,
                onClick = { sendKey(KeyEvent.KEYCODE_DPAD_UP) }
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // Row 2: LEFT, CENTER/SELECT, RIGHT
        Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
            KeyButton(
                text = "◀",
                modifier = Modifier.weight(1f),
                height = keyHeight,
                hapticEnabled = hapticEnabled,
                onClick = { sendKey(KeyEvent.KEYCODE_DPAD_LEFT) }
            )
            KeyButton(
                text = "SELECT",
                modifier = Modifier.weight(1f),
                height = keyHeight,
                hapticEnabled = hapticEnabled,
                onClick = { sendKey(KeyEvent.KEYCODE_DPAD_CENTER) }
            )
            KeyButton(
                text = "▶",
                modifier = Modifier.weight(1f),
                height = keyHeight,
                hapticEnabled = hapticEnabled,
                onClick = { sendKey(KeyEvent.KEYCODE_DPAD_RIGHT) }
            )
        }

        // Row 3: DOWN
        Row(modifier = Modifier.fillMaxWidth().height(keyHeight)) {
            Spacer(modifier = Modifier.weight(1f))
            KeyButton(
                text = "▼",
                modifier = Modifier.weight(1f),
                height = keyHeight,
                hapticEnabled = hapticEnabled,
                onClick = { sendKey(KeyEvent.KEYCODE_DPAD_DOWN) }
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation shortcuts: Home, End, Select All, Cut, Copy, Paste
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { sendKey(KeyEvent.KEYCODE_MOVE_HOME) },
                modifier = Modifier.weight(1f).padding(2.dp)
            ) {
                Text("Home")
            }
            OutlinedButton(
                onClick = { sendKey(KeyEvent.KEYCODE_MOVE_END) },
                modifier = Modifier.weight(1f).padding(2.dp)
            ) {
                Text("End")
            }
            Button(
                onClick = { inputConnection?.performContextMenuAction(android.R.id.selectAll) },
                modifier = Modifier.weight(1f).padding(2.dp)
            ) {
                Text("Select All")
            }
        }
    }
}
