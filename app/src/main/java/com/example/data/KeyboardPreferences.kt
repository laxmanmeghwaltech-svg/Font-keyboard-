package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

val Context.keyboardDataStore: DataStore<Preferences> by preferencesDataStore(name = "keyboard_preferences")

class KeyboardPreferencesManager(private val context: Context) {
    companion object {
        val HAPTIC_FEEDBACK_KEY = booleanPreferencesKey("haptic_feedback")
        val AUTO_CORRECTION_KEY = booleanPreferencesKey("auto_correction")
        val AUTO_CAPITALIZATION_KEY = booleanPreferencesKey("auto_capitalization")
        val KEY_POPUP_KEY = booleanPreferencesKey("key_popup")
        val SUGGESTION_STRIP_KEY = booleanPreferencesKey("suggestion_strip")
        val SHOW_NUMBER_ROW_KEY = booleanPreferencesKey("show_number_row")
        val INCOGNITO_MODE_KEY = booleanPreferencesKey("incognito_mode")
        val RECENT_EMOJIS_KEY = stringPreferencesKey("recent_emojis")
        val FLOATING_X_KEY = floatPreferencesKey("floating_x")
        val FLOATING_Y_KEY = floatPreferencesKey("floating_y")
        val FLOATING_MODE_KEY = booleanPreferencesKey("floating_mode")
    }

    val hapticFeedbackFlow: Flow<Boolean> = context.keyboardDataStore.data.map { preferences ->
        preferences[HAPTIC_FEEDBACK_KEY] ?: true
    }

    val autoCorrectionFlow: Flow<Boolean> = context.keyboardDataStore.data.map { preferences ->
        preferences[AUTO_CORRECTION_KEY] ?: true
    }

    val autoCapitalizationFlow: Flow<Boolean> = context.keyboardDataStore.data.map { preferences ->
        preferences[AUTO_CAPITALIZATION_KEY] ?: true
    }

    val keyPopupFlow: Flow<Boolean> = context.keyboardDataStore.data.map { preferences ->
        preferences[KEY_POPUP_KEY] ?: true
    }

    val suggestionStripFlow: Flow<Boolean> = context.keyboardDataStore.data.map { preferences ->
        preferences[SUGGESTION_STRIP_KEY] ?: true
    }

    val showNumberRowFlow: Flow<Boolean> = context.keyboardDataStore.data.map { preferences ->
        preferences[SHOW_NUMBER_ROW_KEY] ?: true
    }

    val incognitoModeFlow: Flow<Boolean> = context.keyboardDataStore.data.map { preferences ->
        preferences[INCOGNITO_MODE_KEY] ?: false
    }

    val recentEmojisFlow: Flow<List<String>> = context.keyboardDataStore.data.map { preferences ->
        val csv = preferences[RECENT_EMOJIS_KEY] ?: ""
        if (csv.isBlank()) listOf("😊", "👍", "❤️", "🔥", "🎉", "😂", "✨", "🙏") else csv.split(",")
    }

    val floatingXFlow: Flow<Float> = context.keyboardDataStore.data.map { preferences ->
        preferences[FLOATING_X_KEY] ?: 0f
    }

    val floatingYFlow: Flow<Float> = context.keyboardDataStore.data.map { preferences ->
        preferences[FLOATING_Y_KEY] ?: 0f
    }

    val floatingModeFlow: Flow<Boolean> = context.keyboardDataStore.data.map { preferences ->
        preferences[FLOATING_MODE_KEY] ?: false
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.keyboardDataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK_KEY] = enabled
        }
    }

    suspend fun setAutoCorrection(enabled: Boolean) {
        context.keyboardDataStore.edit { preferences ->
            preferences[AUTO_CORRECTION_KEY] = enabled
        }
    }

    suspend fun setAutoCapitalization(enabled: Boolean) {
        context.keyboardDataStore.edit { preferences ->
            preferences[AUTO_CAPITALIZATION_KEY] = enabled
        }
    }

    suspend fun setKeyPopup(enabled: Boolean) {
        context.keyboardDataStore.edit { preferences ->
            preferences[KEY_POPUP_KEY] = enabled
        }
    }

    suspend fun setSuggestionStrip(enabled: Boolean) {
        context.keyboardDataStore.edit { preferences ->
            preferences[SUGGESTION_STRIP_KEY] = enabled
        }
    }

    suspend fun setShowNumberRow(enabled: Boolean) {
        context.keyboardDataStore.edit { preferences ->
            preferences[SHOW_NUMBER_ROW_KEY] = enabled
        }
    }

    suspend fun setIncognitoMode(enabled: Boolean) {
        context.keyboardDataStore.edit { preferences ->
            preferences[INCOGNITO_MODE_KEY] = enabled
        }
    }

    suspend fun addRecentEmoji(emoji: String) {
        context.keyboardDataStore.edit { preferences ->
            val current = (preferences[RECENT_EMOJIS_KEY] ?: "").split(",").filter { it.isNotBlank() }.toMutableList()
            current.remove(emoji)
            current.add(0, emoji)
            preferences[RECENT_EMOJIS_KEY] = current.take(30).joinToString(",")
        }
    }

    suspend fun setFloatingPosition(x: Float, y: Float) {
        context.keyboardDataStore.edit { preferences ->
            preferences[FLOATING_X_KEY] = x
            preferences[FLOATING_Y_KEY] = y
        }
    }

    suspend fun setFloatingMode(enabled: Boolean) {
        context.keyboardDataStore.edit { preferences ->
            preferences[FLOATING_MODE_KEY] = enabled
        }
    }
}
