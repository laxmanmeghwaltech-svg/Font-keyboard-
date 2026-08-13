package com.example.data

import kotlinx.coroutines.flow.Flow

class KeyboardRepository(
    private val database: KeyboardDatabase,
    val preferencesManager: KeyboardPreferencesManager,
    val aiService: AiService,
    val encryptedApiKeyManager: EncryptedApiKeyManager
) {
    val allFonts: Flow<List<CustomFont>> = database.fontDao().getAllFonts()
    val allEmojis: Flow<List<CustomEmoji>> = database.emojiDao().getAllEmojis()
    val allShortcuts: Flow<List<TextShortcut>> = database.shortcutDao().getAllShortcuts()
    val allClipboardItems: Flow<List<ClipboardItem>> = database.clipboardDao().getAllClipboardItems()

    suspend fun insertFont(font: CustomFont): Long = database.fontDao().insertFont(font)
    suspend fun deleteFont(font: CustomFont) = database.fontDao().deleteFont(font)

    suspend fun insertEmoji(emoji: CustomEmoji) = database.emojiDao().insertEmoji(emoji)
    suspend fun deleteEmoji(emoji: CustomEmoji) = database.emojiDao().deleteEmoji(emoji)

    suspend fun insertShortcut(shortcut: TextShortcut) = database.shortcutDao().insertShortcut(shortcut)
    suspend fun deleteShortcut(shortcut: TextShortcut) = database.shortcutDao().deleteShortcut(shortcut)

    suspend fun insertClipboardItem(item: ClipboardItem) = database.clipboardDao().insertClipboardItem(item)
    suspend fun deleteClipboardItem(item: ClipboardItem) = database.clipboardDao().deleteClipboardItem(item)
    suspend fun clearUnpinnedClipboard() = database.clipboardDao().clearUnpinnedClipboard()
}
