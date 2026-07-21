package com.example

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.inputmethodservice.InputMethodService
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import coil.compose.AsyncImage
import com.example.data.CustomEmoji
import com.example.data.CustomFont
import com.example.data.KeyboardDatabase
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.FontStyler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

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
    private val customFontsState = mutableStateListOf<CustomFont>()
    private val customEmojisState = mutableStateListOf<CustomEmoji>()

    // Recording State
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    var isRecording = mutableStateOf(false)
    var dictationStatus = mutableStateOf("")

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        // Load data from Room Database
        serviceScope.launch {
            val db = KeyboardDatabase.getInstance(applicationContext)
            db.fontDao().getAllFonts().collectLatest { fonts ->
                customFontsState.clear()
                customFontsState.addAll(fonts)
            }
        }
        serviceScope.launch {
            val db = KeyboardDatabase.getInstance(applicationContext)
            db.emojiDao().getAllEmojis().collectLatest { emojis ->
                customEmojisState.clear()
                customEmojisState.addAll(emojis)
            }
        }
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
        store.clear()
    }

    override fun onCreateInputView(): View {
        // Set window to transparent to support rounded floating container mode
        window?.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FontKeyboardService)
            setViewTreeViewModelStoreOwner(this@FontKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@FontKeyboardService)
        }

        composeView.setContent {
            MyApplicationTheme {
                KeyboardScreen(
                    service = this,
                    customFonts = customFontsState,
                    customEmojis = customEmojisState
                )
            }
        }
        return composeView
    }

    // Audio Dictation logic
    fun toggleRecording() {
        if (isRecording.value) {
            stopAndTranscribe()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            dictationStatus.value = "Audio permission required! Open app to grant."
            // Open main app
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            return
        }

        try {
            audioFile = File(cacheDir, "dictation.m4a")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile!!.absolutePath)
                prepare()
                start()
            }
            isRecording.value = true
            dictationStatus.value = "Listening... Tap mic again to transcribe"
        } catch (e: Exception) {
            e.printStackTrace()
            dictationStatus.value = "Recording error: ${e.message}"
        }
    }

    private fun stopAndTranscribe() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        isRecording.value = false
        dictationStatus.value = "Transcribing with Gemini..."

        val file = audioFile ?: return
        if (!file.exists() || file.length() == 0L) {
            dictationStatus.value = "Empty audio recording"
            return
        }

        serviceScope.launch(Dispatchers.IO) {
            val bytes = file.readBytes()
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                launch(Dispatchers.Main) {
                    dictationStatus.value = "Gemini API key is not configured!"
                }
                return@launch
            }

            transcribeAudio(bytes, apiKey) { result ->
                launch(Dispatchers.Main) {
                    if (result.startsWith("Dictation error:") || result.startsWith("Failed to parse")) {
                        dictationStatus.value = result
                    } else {
                        currentInputConnection?.commitText(result, 1)
                        dictationStatus.value = ""
                    }
                }
            }
        }
    }

    private fun transcribeAudio(audioBytes: ByteArray, apiKey: String, callback: (String) -> Unit) {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val base64Audio = android.util.Base64.encodeToString(audioBytes, android.util.Base64.NO_WRAP)

        // Create JSON request
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "You are an accurate voice dictation transcriber. Transcribe the following speech into precise, natural text. Return ONLY the transcription, without any preamble, explanation, or quotes.")
                        })
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "audio/mp4")
                                put("data", base64Audio)
                            })
                        })
                    })
                })
            })
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                callback("Dictation error: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!it.isSuccessful) {
                        callback("Dictation error: HTTP ${it.code}")
                        return
                    }
                    try {
                        val responseStr = it.body?.string() ?: ""
                        val json = JSONObject(responseStr)
                        val candidates = json.getJSONArray("candidates")
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.getJSONObject("content")
                        val parts = content.getJSONArray("parts")
                        val text = parts.getJSONObject(0).getString("text")
                        callback(text.trim())
                    } catch (e: Exception) {
                        callback("Failed to parse dictation: ${e.message}")
                    }
                }
            }
        })
    }

    // Helper to send generated sticker/image to clipboard
    fun shareStickerImage(context: Context, bitmap: Bitmap) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "temp_sticker_${System.currentTimeMillis()}.png")
                file.outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "com.aistudio.fontkeyboard.vspflw.fileprovider",
                    file
                )

                launch(Dispatchers.Main) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newUri(context.contentResolver, "Font Sticker", uri)
                    clipboard.setPrimaryClip(clip)
                    dictationStatus.value = "Sticker copied to clipboard! Paste it anywhere."
                    // Clear message after 3 seconds
                    Handler(Looper.getMainLooper()).postDelayed({
                        dictationStatus.value = ""
                    }, 3000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    dictationStatus.value = "Sticker creation failed: ${e.message}"
                }
            }
        }
    }
}

// --- COMPOSE KEYBOARD UI ---

@Composable
fun KeyboardScreen(
    service: FontKeyboardService,
    customFonts: List<CustomFont>,
    customEmojis: List<CustomEmoji>
) {
    val context = LocalContext.current
    var isFloating by remember { mutableStateOf(false) }
    var currentFontMode by remember { mutableStateOf(FontStyler.KeyboardFont.NORMAL) }
    var selectedCustomFont by remember { mutableStateOf<CustomFont?>(null) }
    var isShiftActive by remember { mutableStateOf(false) }
    var isSymbolsActive by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("keys") } // keys, emojis, custom_fonts

    // Custom Font sticker compositing buffer
    var stickerInputBuffer by remember { mutableStateOf("") }

    // Draggable floating offsets
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val containerModifier = if (isFloating) {
        Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(420.dp)
            .wrapContentHeight()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
            .testTag("floating_keyboard_container")
    } else {
        Modifier
            .fillMaxWidth()
            .height(290.dp)
            .background(MaterialTheme.colorScheme.surface)
            .testTag("docked_keyboard_container")
    }

    Box(
        modifier = if (isFloating) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
        contentAlignment = if (isFloating) Alignment.Center else Alignment.BottomCenter
    ) {
        Surface(
            modifier = containerModifier,
            tonalElevation = 8.dp,
            color = if (isFloating) MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp) else MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                // --- Accessory Bar / Toolbar ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Floating window handle or indicators
                    IconButton(
                        onClick = { isFloating = !isFloating },
                        modifier = Modifier.testTag("floating_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isFloating) Icons.Default.VerticalAlignBottom else Icons.Outlined.Launch,
                            contentDescription = "Toggle Floating Keyboard",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Dictation Microphone Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        IconButton(
                            onClick = { service.toggleRecording() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (service.isRecording.value) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
                                .testTag("dictation_btn")
                        ) {
                            Icon(
                                imageVector = if (service.isRecording.value) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Voice Dictation",
                                tint = if (service.isRecording.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Status or Live preview
                        Text(
                            text = if (service.dictationStatus.value.isNotEmpty()) {
                                service.dictationStatus.value
                            } else if (selectedCustomFont != null && stickerInputBuffer.isNotEmpty()) {
                                "Sticker Preview: Click to Paste"
                            } else {
                                "Font Keyboard Active"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        )
                    }

                    // Tab Buttons
                    IconButton(
                        onClick = {
                            activeTab = if (activeTab == "emojis") "keys" else "emojis"
                        },
                        modifier = Modifier.testTag("emoji_tab_btn")
                    ) {
                        Icon(
                            imageVector = if (activeTab == "emojis") Icons.Outlined.KeyboardAlt else Icons.Outlined.EmojiEmotions,
                            contentDescription = "Emojis",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            activeTab = if (activeTab == "custom_fonts") "keys" else "custom_fonts"
                        },
                        modifier = Modifier.testTag("fonts_tab_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FontDownload,
                            contentDescription = "Fonts Selection",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Launch Main App
                    IconButton(
                        onClick = {
                            val intent = Intent(context, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open Settings App",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // --- Live Font Sticker Composer bar if Custom Font is selected ---
                AnimatedVisibility(visible = selectedCustomFont != null && stickerInputBuffer.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clickable {
                                // Render PNG of stickerInputBuffer using selectedCustomFont path
                                selectedCustomFont?.let { font ->
                                    val bitmap = renderTextToBitmap(context, stickerInputBuffer, font.filePath)
                                    service.shareStickerImage(context, bitmap)
                                    stickerInputBuffer = ""
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stickerInputBuffer,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Paste Sticker",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // --- MAIN VIEW AREA DEPENDING ON ACTIVE TAB ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (activeTab) {
                        "keys" -> {
                            Column {
                                // Row of Unicode Fonts
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(FontStyler.KeyboardFont.values()) { font ->
                                        val isSelected = currentFontMode == font && selectedCustomFont == null
                                        Button(
                                            onClick = {
                                                currentFontMode = font
                                                selectedCustomFont = null
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(
                                                text = FontStyler.styleText(font.displayName, font),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Render standard Keyboard keys
                                if (isSymbolsActive) {
                                    SymbolsKeyboardLayout(
                                        onKeyTap = { key ->
                                            service.currentInputConnection?.commitText(key, 1)
                                        },
                                        onBackspace = {
                                            service.currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                                            service.currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                                        },
                                        onEnter = {
                                            service.currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                                            service.currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                                        },
                                        onToggleSymbols = { isSymbolsActive = false }
                                    )
                                } else {
                                    AlphabetKeyboardLayout(
                                        isShiftActive = isShiftActive,
                                        onKeyTap = { char ->
                                            if (selectedCustomFont != null) {
                                                // Buffer the typing to composite as sticker later
                                                stickerInputBuffer += char
                                            } else {
                                                val styled = FontStyler.styleText(char.toString(), currentFontMode)
                                                service.currentInputConnection?.commitText(styled, 1)
                                            }
                                        },
                                        onShift = { isShiftActive = !isShiftActive },
                                        onBackspace = {
                                            if (selectedCustomFont != null && stickerInputBuffer.isNotEmpty()) {
                                                stickerInputBuffer = stickerInputBuffer.dropLast(1)
                                            } else {
                                                service.currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                                                service.currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                                            }
                                        },
                                        onSpace = {
                                            if (selectedCustomFont != null) {
                                                stickerInputBuffer += " "
                                            } else {
                                                service.currentInputConnection?.commitText(" ", 1)
                                            }
                                        },
                                        onEnter = {
                                            if (selectedCustomFont != null && stickerInputBuffer.isNotEmpty()) {
                                                // Commit sticker
                                                selectedCustomFont?.let { font ->
                                                    val bitmap = renderTextToBitmap(context, stickerInputBuffer, font.filePath)
                                                    service.shareStickerImage(context, bitmap)
                                                    stickerInputBuffer = ""
                                                }
                                            } else {
                                                service.currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                                                service.currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                                            }
                                        },
                                        onToggleSymbols = { isSymbolsActive = true }
                                    )
                                }
                            }
                        }
                        "emojis" -> {
                            EmojiPickerLayout(
                                customEmojis = customEmojis,
                                onEmojiSelect = { emoji ->
                                    service.currentInputConnection?.commitText(emoji, 1)
                                },
                                onCustomEmojiSelect = { customEmoji ->
                                    // Load custom emoji image and share/paste as image
                                    service.serviceScope.launch(Dispatchers.IO) {
                                        try {
                                            val file = File(customEmoji.filePath)
                                            if (file.exists()) {
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "com.aistudio.fontkeyboard.vspflw.fileprovider",
                                                    file
                                                )
                                                launch(Dispatchers.Main) {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = ClipData.newUri(context.contentResolver, "Emoji Sticker", uri)
                                                    clipboard.setPrimaryClip(clip)
                                                    service.dictationStatus.value = "Emoji copied! Paste it in messenger."
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            )
                        }
                        "custom_fonts" -> {
                            CustomFontsPickerLayout(
                                customFonts = customFonts,
                                selectedFont = selectedCustomFont,
                                onFontSelect = { font ->
                                    selectedCustomFont = font
                                    activeTab = "keys"
                                },
                                onReset = {
                                    selectedCustomFont = null
                                    activeTab = "keys"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- KEYBOARD LAYOUT COMPOSABLES ---

@Composable
fun AlphabetKeyboardLayout(
    isShiftActive: Boolean,
    onKeyTap: (Char) -> Unit,
    onShift: () -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onToggleSymbols: () -> Unit
) {
    val row1 = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p')
    val row2 = listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l')
    val row3 = listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row1.forEach { char ->
                val letter = if (isShiftActive) char.uppercaseChar() else char
                KeyButton(text = letter.toString(), modifier = Modifier.weight(1f)) {
                    onKeyTap(letter)
                }
            }
        }

        // Row 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row2.forEach { char ->
                val letter = if (isShiftActive) char.uppercaseChar() else char
                KeyButton(text = letter.toString(), modifier = Modifier.weight(1f)) {
                    onKeyTap(letter)
                }
            }
        }

        // Row 3 (Shift + Keys + Backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shift
            KeyButton(
                icon = if (isShiftActive) Icons.Default.KeyboardDoubleArrowUp else Icons.Default.ArrowUpward,
                modifier = Modifier.width(54.dp),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                onShift()
            }

            row3.forEach { char ->
                val letter = if (isShiftActive) char.uppercaseChar() else char
                KeyButton(text = letter.toString(), modifier = Modifier.weight(1f)) {
                    onKeyTap(letter)
                }
            }

            // Backspace
            KeyButton(
                icon = Icons.Outlined.Backspace,
                modifier = Modifier.width(54.dp),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                onBackspace()
            }
        }

        // Row 4 (Symbols toggle, space, enter)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KeyButton(
                text = "?123",
                modifier = Modifier.width(68.dp),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                onToggleSymbols()
            }

            KeyButton(
                text = " ",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                onSpace()
            }

            KeyButton(
                icon = Icons.Default.KeyboardReturn,
                modifier = Modifier.width(84.dp),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                onEnter()
            }
        }
    }
}

@Composable
fun SymbolsKeyboardLayout(
    onKeyTap: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onToggleSymbols: () -> Unit
) {
    val row1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val row2 = listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/")
    val row3 = listOf("*", "\"", "'", ":", ";", "!", "?")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row1.forEach { sym ->
                KeyButton(text = sym, modifier = Modifier.weight(1f)) {
                    onKeyTap(sym)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row2.forEach { sym ->
                KeyButton(text = sym, modifier = Modifier.weight(1f)) {
                    onKeyTap(sym)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(36.dp))

            row3.forEach { sym ->
                KeyButton(text = sym, modifier = Modifier.weight(1f)) {
                    onKeyTap(sym)
                }
            }

            KeyButton(
                icon = Icons.Outlined.Backspace,
                modifier = Modifier.width(54.dp),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                onBackspace()
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KeyButton(
                text = "ABC",
                modifier = Modifier.width(68.dp),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                onToggleSymbols()
            }

            KeyButton(
                text = " ",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                onKeyTap(" ")
            }

            KeyButton(
                icon = Icons.Default.KeyboardReturn,
                modifier = Modifier.width(84.dp),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                onEnter()
            }
        }
    }
}

@Composable
fun EmojiPickerLayout(
    customEmojis: List<CustomEmoji>,
    onEmojiSelect: (String) -> Unit,
    onCustomEmojiSelect: (CustomEmoji) -> Unit
) {
    val emojis = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
        "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
        "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
        "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣",
        "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬",
        "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗"
    )

    var currentPickerTab by remember { mutableStateOf("standard") } // standard, custom

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Standard Emojis",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (currentPickerTab == "standard") FontWeight.Bold else FontWeight.Normal,
                color = if (currentPickerTab == "standard") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable { currentPickerTab = "standard" }
                    .padding(horizontal = 8.dp)
            )

            Text(
                text = "Custom Stickers (${customEmojis.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (currentPickerTab == "custom") FontWeight.Bold else FontWeight.Normal,
                color = if (currentPickerTab == "custom") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable { currentPickerTab = "custom" }
                    .padding(horizontal = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(4.dp)
        ) {
            if (currentPickerTab == "standard") {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 44.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(emojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { onEmojiSelect(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }
            } else {
                if (customEmojis.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No custom emojis uploaded yet.\nOpen App settings to add them!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(customEmojis) { emoji ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clickable { onCustomEmojiSelect(emoji) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = File(emoji.filePath),
                                        contentDescription = emoji.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomFontsPickerLayout(
    customFonts: List<CustomFont>,
    selectedFont: CustomFont?,
    onFontSelect: (CustomFont) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Custom Uploaded Fonts",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            if (selectedFont != null) {
                TextButton(onClick = onReset) {
                    Text("Reset to Normal", fontSize = 12.sp)
                }
            }
        }

        if (customFonts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No custom font files uploaded yet.\nOpen the settings app to load OTF/TTF files!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(customFonts) { font ->
                    val isSelected = selectedFont?.id == font.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFontSelect(font) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Text(
                                text = font.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Render as sticker",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyButton(
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(interactionSource = interactionSource, indication = androidx.compose.foundation.LocalIndication.current) {
                onClick()
            }
            .testTag(if (text != null) "key_$text" else "key_icon"),
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// Custom Font text renderer helper
fun renderTextToBitmap(context: Context, text: String, fontPath: String): Bitmap {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 54f
        color = android.graphics.Color.parseColor("#333333") // Beautiful slate color
        try {
            typeface = Typeface.createFromFile(fontPath)
        } catch (e: Exception) {
            typeface = Typeface.DEFAULT
        }
    }

    // Measure text bounds
    val bounds = android.graphics.Rect()
    paint.getTextBounds(text, 0, text.length, bounds)

    val paddingX = 24
    val paddingY = 24
    val width = bounds.width() + paddingX * 2
    val height = bounds.height() + paddingY * 2

    // Create empty transparent Bitmap
    val bitmap = Bitmap.createBitmap(
        width.coerceAtLeast(100),
        height.coerceAtLeast(60),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)

    // Draw transparent background with rounded border
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#F0F4F8") // light warm white sticker bubble background
        style = Paint.Style.FILL
    }
    val rect = android.graphics.RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat())
    canvas.drawRoundRect(rect, 16f, 16f, bgPaint)

    // Draw text centered
    val x = paddingX.toFloat() - bounds.left
    val y = (canvas.height / 2f) + (bounds.height() / 2f) - bounds.bottom
    canvas.drawText(text, x, y, paint)

    return bitmap
}
