package com.example.utils

import android.text.Editable
import android.text.Selection
import android.text.SpannableStringBuilder
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection

class InAppInputConnection(
    targetView: View,
    private val getCurrentText: () -> String,
    private val onTextUpdated: (String) -> Unit
) : BaseInputConnection(targetView, true) {

    private val buffer = SpannableStringBuilder(getCurrentText())

    override fun getEditable(): Editable {
        val current = getCurrentText()
        if (buffer.toString() != current) {
            buffer.clear()
            buffer.append(current)
            Selection.setSelection(buffer, buffer.length)
        }
        return buffer
    }

    override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
        val editable = editable
        val start = Selection.getSelectionStart(editable).coerceAtLeast(0)
        val end = Selection.getSelectionEnd(editable).coerceAtLeast(0)
        editable.replace(start.coerceAtMost(end), start.coerceAtLeast(end), text ?: "")
        onTextUpdated(editable.toString())
        return true
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        val editable = editable
        val start = Selection.getSelectionStart(editable)
        val end = Selection.getSelectionEnd(editable)

        if (start != end) {
            editable.replace(start.coerceAtMost(end), start.coerceAtLeast(end), "")
        } else if (start > 0) {
            val deleteStart = (start - beforeLength).coerceAtLeast(0)
            editable.delete(deleteStart, start)
        }
        onTextUpdated(editable.toString())
        return true
    }

    override fun sendKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DEL -> {
                    deleteSurroundingText(1, 0)
                    return true
                }
                KeyEvent.KEYCODE_ENTER -> {
                    commitText("\n", 1)
                    return true
                }
                KeyEvent.KEYCODE_SPACE -> {
                    commitText(" ", 1)
                    return true
                }
            }
        }
        return super.sendKeyEvent(event)
    }
}
