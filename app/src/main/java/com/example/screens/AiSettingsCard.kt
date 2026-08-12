package com.example.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun AiSettingsCard(
    hasApiKey: Boolean,
    onSaveKey: (String) -> Unit,
    onClearKey: () -> Unit,
    modifier: Modifier = Modifier
) {
    var apiKeyInput by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("") }

    Card(
        modifier = modifier.testTag("ai_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.ai_settings_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (hasApiKey) "Key configured via Encrypted SharedPreferences" else "Enter Google Gemini API key to enable AI proofreading & voice features",
                fontSize = 13.sp,
                color = if (hasApiKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text(stringResource(R.string.gemini_api_key_label)) },
                placeholder = { Text(stringResource(R.string.gemini_api_key_hint)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        if (apiKeyInput.isNotBlank()) {
                            onSaveKey(apiKeyInput.trim())
                            apiKeyInput = ""
                            statusText = "API Key saved securely!"
                        }
                    },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Text(stringResource(R.string.save_key_button))
                }

                if (hasApiKey) {
                    OutlinedButton(
                        onClick = {
                            onClearKey()
                            statusText = "API Key cleared."
                        },
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    ) {
                        Text(stringResource(R.string.clear_key_button))
                    }
                }
            }

            if (statusText.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = statusText, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
