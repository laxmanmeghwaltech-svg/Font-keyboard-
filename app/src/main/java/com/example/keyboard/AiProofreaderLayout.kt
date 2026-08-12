package com.example.keyboard

import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiAction
import com.example.data.AiService
import kotlinx.coroutines.launch

@Composable
fun AiProofreaderLayout(
    inputConnection: InputConnection?,
    aiService: AiService
) {
    val coroutineScope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("Select an AI action below") }
    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    fun processAiAction(action: AiAction) {
        if (inputConnection == null) {
            statusText = "No active text field"
            return
        }

        val selectedText = inputConnection.getSelectedText(0)?.toString()
        val textToProcess = if (!selectedText.isNullOrBlank()) {
            selectedText
        } else {
            inputConnection.getTextBeforeCursor(500, 0)?.toString() ?: ""
        }

        if (textToProcess.isBlank()) {
            statusText = "Please type or select text first."
            return
        }

        isLoading = true
        statusText = "Processing AI action..."
        resultText = ""

        coroutineScope.launch {
            val result = aiService.proofread(action, textToProcess)
            isLoading = false
            result.fold(
                onSuccess = { text ->
                    resultText = text
                    statusText = "Done! Click Below to Apply:"
                },
                onFailure = { err ->
                    statusText = "Error: ${err.message}"
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "✨ Gemini AI Proofreader & Assistant",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = statusText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (isLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator(modifier = Modifier.height(32.dp))
        }

        if (resultText.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(8.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        inputConnection?.commitText(resultText, 1)
                        resultText = ""
                        statusText = "Applied to field!"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Replace Text")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { processAiAction(AiAction.PROOFREAD) },
                modifier = Modifier.weight(1f).padding(2.dp)
            ) {
                Text("Proofread", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { processAiAction(AiAction.FORMAL) },
                modifier = Modifier.weight(1f).padding(2.dp)
            ) {
                Text("Formal", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { processAiAction(AiAction.FRIENDLY) },
                modifier = Modifier.weight(1f).padding(2.dp)
            ) {
                Text("Friendly", fontSize = 12.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { processAiAction(AiAction.SUMMARIZE) },
                modifier = Modifier.weight(1f).padding(2.dp)
            ) {
                Text("Summarize", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { processAiAction(AiAction.TRANSLATE) },
                modifier = Modifier.weight(1f).padding(2.dp)
            ) {
                Text("Translate", fontSize = 12.sp)
            }
        }
    }
}
