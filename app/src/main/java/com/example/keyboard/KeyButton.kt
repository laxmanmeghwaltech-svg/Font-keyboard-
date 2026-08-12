package com.example.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeyButton(
    text: String,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    hapticEnabled: Boolean = true,
    showPopup: Boolean = false,
    onClick: () -> Unit
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(2.dp)
            .shadow(if (isPressed) 1.dp else 2.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) containerColor.copy(alpha = 0.7f) else containerColor)
            .pointerInput(text) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        if (hapticEnabled) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        onClick()
                    }
                )
            }
            .testTag("key_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium
        )

        if (showPopup && isPressed && text.length == 1) {
            Box(
                modifier = Modifier
                    .offset(y = (-48).dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
