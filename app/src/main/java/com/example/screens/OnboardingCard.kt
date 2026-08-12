package com.example.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun OnboardingCard(
    isKeyboardEnabled: Boolean,
    isKeyboardSelected: Boolean,
    onPermissionRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.testTag("onboarding_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.onboarding_subtitle),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Step 1
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isKeyboardEnabled) Icons.Default.CheckCircle else Icons.Default.Settings,
                    contentDescription = null,
                    tint = if (isKeyboardEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Text(
                    text = stringResource(R.string.step_enable_keyboard),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    fontSize = 14.sp
                )
                if (!isKeyboardEnabled) {
                    OutlinedButton(onClick = { context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }) {
                        Text("Enable", fontSize = 12.sp)
                    }
                }
            }

            // Step 2
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isKeyboardSelected) Icons.Default.CheckCircle else Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = if (isKeyboardSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Text(
                    text = stringResource(R.string.step_select_keyboard),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    fontSize = 14.sp
                )
                if (!isKeyboardSelected) {
                    OutlinedButton(onClick = {
                        val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                        imm.showInputMethodPicker()
                    }) {
                        Text("Select", fontSize = 12.sp)
                    }
                }
            }

            // Step 3
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.step_voice_permission),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    fontSize = 14.sp
                )
                OutlinedButton(onClick = onPermissionRequested) {
                    Text("Grant", fontSize = 12.sp)
                }
            }
        }
    }
}
