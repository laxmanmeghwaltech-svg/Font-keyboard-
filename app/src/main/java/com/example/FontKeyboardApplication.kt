package com.example

import android.app.Application
import com.example.data.EncryptedApiKeyManager
import com.example.data.GeminiAiService
import com.example.data.KeyboardDatabase
import com.example.data.KeyboardPreferencesManager
import com.example.data.KeyboardRepository

class FontKeyboardApplication : Application() {
    val database by lazy { KeyboardDatabase.getInstance(this) }
    val preferencesManager by lazy { KeyboardPreferencesManager(this) }
    val encryptedApiKeyManager by lazy { EncryptedApiKeyManager(this) }
    val aiService by lazy { GeminiAiService { encryptedApiKeyManager.getApiKey() } }

    val repository by lazy {
        KeyboardRepository(
            database = database,
            preferencesManager = preferencesManager,
            aiService = aiService,
            encryptedApiKeyManager = encryptedApiKeyManager
        )
    }
}
