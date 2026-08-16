package com.example.utils

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface as ComposeTypeface
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * FontManager
 *
 * Handles loading, caching, and serving Typeface objects for:
 * 1. Bundled asset fonts (assets/fonts/)
 * 2. Downloaded/custom user font files in app internal storage
 * 3. System fallback fonts
 * 4. Jetpack Compose FontFamily bridges
 */
object FontManager {
    private const val TAG = "FontManager"
    private const val ASSETS_FONTS_DIR = "fonts"

    // Thread-safe in-memory cache to prevent redundant disk I/O and Typeface allocations
    private val typefaceCache = ConcurrentHashMap<String, Typeface>()
    private val composeFontFamilyCache = ConcurrentHashMap<String, FontFamily>()

    // Built-in Font Names & Asset Descriptors
    data class FontItem(
        val id: String,
        val displayName: String,
        val assetPath: String? = null,
        val previewSample: String = "Abc 123",
        val isSystem: Boolean = false
    )

    private val defaultFonts = listOf(
        FontItem("default", "System Default", isSystem = true, previewSample = "Normal Text"),
        FontItem("sans_serif", "Sans Serif", isSystem = true, previewSample = "Modern Sans"),
        FontItem("serif", "Serif Elegant", isSystem = true, previewSample = "Serif Classic"),
        FontItem("monospace", "Monospace Code", isSystem = true, previewSample = "Mono Clean"),
        FontItem("roboto", "Roboto", "fonts/Roboto-Regular.ttf", "Clean & Crisp"),
        FontItem("caveat", "Caveat Cursive", "fonts/Caveat-Regular.ttf", "Handwritten Flow"),
        FontItem("poppins", "Poppins Geometric", "fonts/Poppins-Medium.ttf", "Bold Modern"),
        FontItem("dancing_script", "Dancing Script", "fonts/DancingScript-Bold.ttf", "Artistic Cursive"),
        FontItem("cinzel", "Cinzel Decorative", "fonts/Cinzel-Regular.ttf", "Regal Serif")
    )

    /**
     * Retrieve all available fonts (bundled + system defaults).
     */
    fun getAvailableFonts(): List<FontItem> {
        return defaultFonts
    }

    /**
     * Load Typeface with memory caching and fallback.
     */
    fun getTypeface(context: Context, fontId: String): Typeface {
        return typefaceCache.getOrPut(fontId) {
            loadTypefaceInternal(context, fontId)
        }
    }

    /**
     * Get Compose FontFamily representation for Jetpack Compose UI.
     */
    fun getComposeFontFamily(context: Context, fontId: String): FontFamily {
        return composeFontFamilyCache.getOrPut(fontId) {
            val tf = getTypeface(context, fontId)
            FontFamily(ComposeTypeface(tf))
        }
    }

    private fun loadTypefaceInternal(context: Context, fontId: String): Typeface {
        return try {
            when (fontId) {
                "default" -> Typeface.DEFAULT
                "sans_serif" -> Typeface.SANS_SERIF
                "serif" -> Typeface.SERIF
                "monospace" -> Typeface.MONOSPACE
                else -> {
                    val fontItem = defaultFonts.find { it.id == fontId }
                    if (fontItem?.assetPath != null) {
                        try {
                            Typeface.createFromAsset(context.assets, fontItem.assetPath)
                        } catch (e: Exception) {
                            Log.w(TAG, "Asset font not found at ${fontItem.assetPath}, checking fallback", e)
                            Typeface.DEFAULT
                        }
                    } else {
                        // Check custom user font in app internal files
                        val customFile = File(context.filesDir, "fonts/$fontId.ttf")
                        if (customFile.exists()) {
                            Typeface.createFromFile(customFile)
                        } else {
                            Typeface.DEFAULT
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load typeface for id=$fontId, returning default", e)
            Typeface.DEFAULT
        }
    }

    /**
     * Clears cached typefaces if memory pressure occurs.
     */
    fun clearCache() {
        typefaceCache.clear()
        composeFontFamilyCache.clear()
    }
}
