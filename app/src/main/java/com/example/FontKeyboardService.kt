package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.ClipboardItem
import com.example.data.CustomEmoji
import com.example.data.CustomFont
import com.example.data.KeyboardPreferencesManager
import com.example.data.KeyboardRepository
import com.example.data.TextShortcut
import com.example.keyboard.KeyboardScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class FontKeyboardService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle = lifecycleRegistry
    override val viewModelStore: ViewModelStore = store
    override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

    val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Database loaded state
    val customFontsState = mutableStateListOf<CustomFont>()
    val customEmojisState = mutableStateListOf<CustomEmoji>()
    val shortcutsState = mutableStateListOf<TextShortcut>()
    val clipboardState = mutableStateListOf<ClipboardItem>()

    // Status message for dictation / sticker copy
    var dictationStatus = mutableStateOf("")

    private lateinit var appRepository: KeyboardRepository
    private var clipboardManager: ClipboardManager? = null
    private var primaryClipListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        val app = application as FontKeyboardApplication
        appRepository = app.repository

        setupClipboardListener()

        // Load data flows from Repository
        serviceScope.launch {
            appRepository.allFonts.collectLatest { fonts ->
                customFontsState.clear()
                customFontsState.addAll(fonts)
            }
        }
        serviceScope.launch {
            appRepository.allEmojis.collectLatest { emojis ->
                customEmojisState.clear()
                customEmojisState.addAll(emojis)
            }
        }
        serviceScope.launch {
            appRepository.allShortcuts.collectLatest { shortcuts ->
                shortcutsState.clear()
                shortcutsState.addAll(shortcuts)
            }
        }
        serviceScope.launch {
            appRepository.allClipboardItems.collectLatest { items ->
                clipboardState.clear()
                clipboardState.addAll(items)
            }
        }
    }

    private fun setupClipboardListener() {
        try {
            clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            primaryClipListener = ClipboardManager.OnPrimaryClipChangedListener {
                val clipData = clipboardManager?.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0).text?.toString()
                    if (!text.isNullOrBlank()) {
                        serviceScope.launch(Dispatchers.IO) {
                            appRepository.insertClipboardItem(
                                ClipboardItem(text = text, timestamp = System.currentTimeMillis())
                            )
                        }
                    }
                }
            }
            primaryClipListener?.let { clipboardManager?.addPrimaryClipChangedListener(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareStickerImage(context: Context, bitmap: Bitmap) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val stickerDir = File(context.cacheDir, "stickers").apply { mkdirs() }
                val file = File(stickerDir, "temp_sticker_${System.currentTimeMillis()}.png")
                file.outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${packageName}.fileprovider",
                    file
                )

                launch(Dispatchers.Main) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newUri(context.contentResolver, "Font Sticker", uri)
                    clipboard.setPrimaryClip(clip)
                    dictationStatus.value = "Sticker copied to clipboard!"
                    Handler(Looper.getMainLooper()).postDelayed({
                        dictationStatus.value = ""
                    }, 3000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateInputView(): View {
        val rootView = LayoutInflater.from(this).inflate(R.layout.keyboard_view, null)
        val composeView = rootView.findViewById<ComposeView>(R.id.compose_keyboard_view)

        composeView.apply {
            setViewTreeLifecycleOwner(this@FontKeyboardService)
            setViewTreeViewModelStoreOwner(this@FontKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@FontKeyboardService)

            setContent {
                MyApplicationTheme {
                    val prefs = appRepository.preferencesManager
                    val haptic by prefs.hapticFeedbackFlow.collectAsState(initial = true)
                    val autoCap by prefs.autoCapitalizationFlow.collectAsState(initial = true)
                    val autoCorrect by prefs.autoCorrectionFlow.collectAsState(initial = true)
                    val keyPopup by prefs.keyPopupFlow.collectAsState(initial = true)
                    val showNumRow by prefs.showNumberRowFlow.collectAsState(initial = true)
                    val suggestionStrip by prefs.suggestionStripFlow.collectAsState(initial = true)
                    val floatingX by prefs.floatingXFlow.collectAsState(initial = 0f)
                    val floatingY by prefs.floatingYFlow.collectAsState(initial = 0f)
                    val floatingMode by prefs.floatingModeFlow.collectAsState(initial = false)
                    val recentEmojisList by prefs.recentEmojisFlow.collectAsState(initial = listOf("😊", "👍", "❤️", "🔥", "🎉", "😂", "✨", "🙏"))

                    KeyboardScreen(
                        inputConnection = currentInputConnection,
                        repository = appRepository,
                        customFonts = customFontsState,
                        customEmojis = customEmojisState,
                        shortcuts = shortcutsState,
                        clipboardItems = clipboardState,
                        hapticEnabled = haptic,
                        autoCapEnabled = autoCap,
                        autoCorrectEnabled = autoCorrect,
                        keyPopupEnabled = keyPopup,
                        showNumberRow = showNumRow,
                        suggestionStripEnabled = suggestionStrip,
                        recentEmojisList = recentEmojisList,
                        initialFloatingX = floatingX,
                        initialFloatingY = floatingY,
                        initialFloatingMode = floatingMode,
                        onVoiceDictationClick = {
                            dictationStatus.value = "Use Gemini AI tab for voice dictation & text generation."
                        },
                        onShareStickerBitmap = { bmp ->
                            shareStickerImage(this@FontKeyboardService, bmp)
                        }
                    )
                }
            }
        }
        return rootView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        try {
            primaryClipListener?.let { clipboardManager?.removePrimaryClipChangedListener(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        serviceScope.cancel()
        store.clear()
    }
}
