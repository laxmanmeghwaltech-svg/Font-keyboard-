package com.example.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiConfig {
    const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    const val MODEL_NAME = "gemini-1.5-flash"
}

enum class AiAction {
    PROOFREAD, FORMAL, FRIENDLY, SUMMARIZE, TRANSLATE
}

interface AiService {
    suspend fun proofread(action: AiAction, text: String): Result<String>
    suspend fun transcribe(audioBytes: ByteArray, mimeType: String): Result<String>
}

class EncryptedApiKeyManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "encrypted_ai_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.values()[0],
        EncryptedSharedPreferences.PrefValueEncryptionScheme.values()[0]
    )

    fun getApiKey(): String? {
        return sharedPreferences.getString("user_gemini_api_key", null)?.takeIf { it.isNotBlank() }
    }

    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString("user_gemini_api_key", apiKey.trim()).apply()
    }

    fun clearApiKey() {
        sharedPreferences.edit().remove("user_gemini_api_key").apply()
    }
}

class GeminiAiService(private val apiKeyProvider: () -> String?) : AiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun proofread(action: AiAction, text: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured in Settings."))
        }

        val prompt = when (action) {
            AiAction.PROOFREAD -> "Fix all grammar, spelling, and punctuation errors in this text while keeping its tone intact. Output ONLY the corrected text:\n\n$text"
            AiAction.FORMAL -> "Rewrite the following text to sound professional and polite. Output ONLY the rewritten text:\n\n$text"
            AiAction.FRIENDLY -> "Rewrite the following text to sound casual, friendly, and warm. Output ONLY the rewritten text:\n\n$text"
            AiAction.SUMMARIZE -> "Summarize the following text concisely. Output ONLY the summary:\n\n$text"
            AiAction.TRANSLATE -> "Translate the following text into English cleanly. Output ONLY the translation:\n\n$text"
        }

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "${GeminiConfig.BASE_URL}/${GeminiConfig.MODEL_NAME}:generateContent"

            val request = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string()
                if (!response.isSuccessful || bodyStr == null) {
                    return@withContext Result.failure(Exception("Gemini API error ${response.code}: ${response.message}"))
                }

                val json = JSONObject(bodyStr)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val resultText = parts.getJSONObject(0).optString("text", "").trim()
                        return@withContext Result.success(resultText)
                    }
                }
                Result.failure(Exception("Empty response from Gemini API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun transcribe(audioBytes: ByteArray, mimeType: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured in Settings."))
        }

        try {
            val base64Audio = android.util.Base64.encodeToString(audioBytes, android.util.Base64.NO_WRAP)
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("inline_data", JSONObject().apply {
                                    put("mime_type", mimeType)
                                    put("data", base64Audio)
                                })
                            })
                            put(JSONObject().put("text", "Transcribe the spoken audio text accurately without any conversational prefix or quotes. Output ONLY the raw transcript."))
                        })
                    })
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "${GeminiConfig.BASE_URL}/${GeminiConfig.MODEL_NAME}:generateContent"

            val request = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string()
                if (!response.isSuccessful || bodyStr == null) {
                    return@withContext Result.failure(Exception("Gemini API error ${response.code}: ${response.message}"))
                }

                val json = JSONObject(bodyStr)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val transcript = parts.getJSONObject(0).optString("text", "").trim()
                        return@withContext Result.success(transcript)
                    }
                }
                Result.failure(Exception("Empty transcript from Gemini API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
