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

@Dao
interface CustomFontDao {
    @Query("SELECT * FROM custom_fonts ORDER BY addedTime DESC")
    fun getAllFonts(): Flow<List<CustomFont>>

    @Query("SELECT * FROM custom_fonts ORDER BY addedTime DESC")
    suspend fun getAllFontsDirect(): List<CustomFont>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFont(font: CustomFont)

    @Delete
    suspend fun deleteFont(font: CustomFont)
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

@Database(entities = [CustomFont::class, CustomEmoji::class], version = 1, exportSchema = false)
abstract class KeyboardDatabase : RoomDatabase() {
    abstract fun fontDao(): CustomFontDao
    abstract fun emojiDao(): CustomEmojiDao

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
