package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

import androidx.room.Update

@Dao
interface CustomFontDao {
    @Query("SELECT * FROM custom_fonts ORDER BY addedTime DESC")
    fun getAllFonts(): Flow<List<CustomFont>>

    @Query("SELECT * FROM custom_fonts ORDER BY addedTime DESC")
    suspend fun getAllFontsDirect(): List<CustomFont>

    @Query("SELECT * FROM custom_fonts WHERE id = :fontId LIMIT 1")
    suspend fun getFontById(fontId: Int): CustomFont?

    @Query("SELECT * FROM custom_fonts WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFontsByName(query: String): Flow<List<CustomFont>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFont(font: CustomFont): Long

    @Update
    suspend fun updateFont(font: CustomFont)

    @Delete
    suspend fun deleteFont(font: CustomFont)

    @Query("DELETE FROM custom_fonts WHERE id = :fontId")
    suspend fun deleteFontById(fontId: Int)
}

@Dao
interface CustomEmojiDao {
    @Query("SELECT * FROM custom_emojis ORDER BY addedTime DESC")
    fun getAllEmojis(): Flow<List<CustomEmoji>>

    @Query("SELECT * FROM custom_emojis ORDER BY addedTime DESC")
    suspend fun getAllEmojisDirect(): List<CustomEmoji>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmoji(emoji: CustomEmoji)

    @Delete
    suspend fun deleteEmoji(emoji: CustomEmoji)
}

@Dao
interface TextShortcutDao {
    @Query("SELECT * FROM text_shortcuts ORDER BY shortcut ASC")
    fun getAllShortcuts(): Flow<List<TextShortcut>>

    @Query("SELECT * FROM text_shortcuts WHERE isEnabled = 1 ORDER BY shortcut ASC")
    suspend fun getActiveShortcutsDirect(): List<TextShortcut>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: TextShortcut)

    @Delete
    suspend fun deleteShortcut(shortcut: TextShortcut)
}

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_history ORDER BY isPinned DESC, timestamp DESC")
    fun getAllClipboardItems(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_history ORDER BY isPinned DESC, timestamp DESC LIMIT 30")
    suspend fun getRecentClipboardItemsDirect(): List<ClipboardItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClipboardItem(item: ClipboardItem)

    @Query("DELETE FROM clipboard_history WHERE isPinned = 0")
    suspend fun clearUnpinnedClipboard()

    @Delete
    suspend fun deleteClipboardItem(item: ClipboardItem)
}

@Database(
    entities = [CustomFont::class, CustomEmoji::class, TextShortcut::class, ClipboardItem::class],
    version = 3,
    exportSchema = false
)
abstract class KeyboardDatabase : RoomDatabase() {
    abstract fun fontDao(): CustomFontDao
    abstract fun emojiDao(): CustomEmojiDao
    abstract fun shortcutDao(): TextShortcutDao
    abstract fun clipboardDao(): ClipboardDao

    companion object {
        @Volatile
        private var INSTANCE: KeyboardDatabase? = null

        fun getInstance(context: Context): KeyboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KeyboardDatabase::class.java,
                    "keyboard_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
