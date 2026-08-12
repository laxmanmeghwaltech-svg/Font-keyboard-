package com.example.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun KeyboardPreferencesCard(
    fontStyleStrip: Boolean,
    onToggleFontStyleStrip: (Boolean) -> Unit,
    showNumberRow: Boolean,
    onToggleShowNumberRow: (Boolean) -> Unit,
    autoCorrection: Boolean,
    onToggleAutoCorrection: (Boolean) -> Unit,
    autoCapitalization: Boolean,
    onToggleAutoCapitalization: (Boolean) -> Unit,
    hapticFeedback: Boolean,
    onToggleHapticFeedback: (Boolean) -> Unit,
    keyPopup: Boolean,
    onToggleKeyPopup: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onToggleIncognitoMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("preferences_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚙️ Keyboard Settings & Behavior",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Font Style Strip
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.font_style_strip_label), fontSize = 14.sp, modifier = Modifier.weight(1f))
                Switch(checked = fontStyleStrip, onCheckedChange = onToggleFontStyleStrip)
            }

            // Show Number Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.show_number_row_label), fontSize = 14.sp, modifier = Modifier.weight(1f))
                Switch(checked = showNumberRow, onCheckedChange = onToggleShowNumberRow)
            }

            // Auto-Correction
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.auto_correction_label), fontSize = 14.sp, modifier = Modifier.weight(1f))
                Switch(checked = autoCorrection, onCheckedChange = onToggleAutoCorrection)
            }

            // Auto-Capitalization
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.auto_capitalization_label), fontSize = 14.sp, modifier = Modifier.weight(1f))
                Switch(checked = autoCapitalization, onCheckedChange = onToggleAutoCapitalization)
            }

            // Haptic Feedback
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.haptic_feedback_label), fontSize = 14.sp, modifier = Modifier.weight(1f))
                Switch(checked = hapticFeedback, onCheckedChange = onToggleHapticFeedback)
            }

            // Key Popup
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.key_popup_label), fontSize = 14.sp, modifier = Modifier.weight(1f))
                Switch(checked = keyPopup, onCheckedChange = onToggleKeyPopup)
            }

            // Incognito Mode
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.incognito_mode_label), fontSize = 14.sp, modifier = Modifier.weight(1f))
                Switch(checked = incognitoMode, onCheckedChange = onToggleIncognitoMode)
            }
        }
    }
}
