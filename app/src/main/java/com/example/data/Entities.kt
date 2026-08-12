package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_fonts")
data class CustomFont(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val filePath: String, // Path to internal storage TTF/OTF file
    val fileSize: Long = 0L,
    val fontFormat: String = "ttf",
    val addedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_emojis")
data class CustomEmoji(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val filePath: String, // Path to saved PNG in internal storage
    val addedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "text_shortcuts")
data class TextShortcut(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shortcut: String,
    val expansion: String,
    val isEnabled: Boolean = true,
    val addedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "clipboard_history")
data class ClipboardItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

