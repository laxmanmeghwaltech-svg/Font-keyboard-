package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_fonts")
data class CustomFont(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val filePath: String, // Path to internal storage TTF/OTF file
    val addedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_emojis")
data class CustomEmoji(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val filePath: String, // Path to saved PNG in internal storage
    val addedTime: Long = System.currentTimeMillis()
)
