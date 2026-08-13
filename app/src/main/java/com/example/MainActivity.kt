package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ClipboardItem
import com.example.data.CustomEmoji
import com.example.data.CustomFont
import com.example.data.KeyboardRepository
import com.example.data.TextShortcut
import com.example.screens.AiSettingsCard
import com.example.screens.ClipboardManagerCard
import com.example.screens.CustomEmojiStickerCard
import com.example.screens.CustomFontUploaderCard
import com.example.screens.KeyboardPreferencesCard
import com.example.screens.OnboardingCard
import com.example.screens.PracticeWorkspaceCard
import com.example.screens.TextShortcutManagerCard
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = context.applicationContext as FontKeyboardApplication
    val repository = app.repository

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    // Column count calculation for responsive adaptive layout
    val columnCount = when {
        screenWidth >= 1000 -> 3
        screenWidth >= 600 -> 2
        else -> 1
    }

    // Keyboard activation state
    var isKeyboardEnabled by remember { mutableStateOf(false) }
    var isKeyboardSelected by remember { mutableStateOf(false) }

    fun refreshKeyboardStatus() {
        isKeyboardEnabled = checkIsKeyboardEnabled(context)
        isKeyboardSelected = checkIsKeyboardSelected(context)
    }

    LaunchedEffect(Unit) {
        refreshKeyboardStatus()
    }

    // Permission launcher for record audio
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            // Voice dictation ready
        }
    )

    // Repository Flows
    val customFonts by repository.allFonts.collectAsState(initial = emptyList())
    val customEmojis by repository.allEmojis.collectAsState(initial = emptyList())
    val shortcuts by repository.allShortcuts.collectAsState(initial = emptyList())
    val clipboardItems by repository.allClipboardItems.collectAsState(initial = emptyList())

    val prefs = repository.preferencesManager
    val fontStyleStrip by prefs.suggestionStripFlow.collectAsState(initial = true)
    val showNumberRow by prefs.showNumberRowFlow.collectAsState(initial = true)
    val autoCorrection by prefs.autoCorrectionFlow.collectAsState(initial = true)
    val autoCapitalization by prefs.autoCapitalizationFlow.collectAsState(initial = true)
    val hapticFeedback by prefs.hapticFeedbackFlow.collectAsState(initial = true)
    val keyPopup by prefs.keyPopupFlow.collectAsState(initial = true)
    val incognitoMode by prefs.incognitoModeFlow.collectAsState(initial = false)

    var hasApiKey by remember { mutableStateOf(!app.encryptedApiKeyManager.getApiKey().isNullOrBlank()) }

    var practiceText by remember { mutableStateOf("") }
    var isInAppKeyboardOpen by remember { mutableStateOf(true) }

    val currentView = androidx.compose.ui.platform.LocalView.current
    val inAppInputConnection = remember {
        com.example.utils.InAppInputConnection(
            targetView = currentView,
            getCurrentText = { practiceText },
            onTextUpdated = { practiceText = it }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Sticky Header Top Bar
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (isKeyboardEnabled && isKeyboardSelected) "✓ Active System IME" else "⚠ System IME Inactive",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isKeyboardEnabled && isKeyboardSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }

        // Adaptive Grid for Cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Onboarding Card (Full width span if not enabled)
            item(span = { GridItemSpan(columnCount) }) {
                OnboardingCard(
                    isKeyboardEnabled = isKeyboardEnabled,
                    isKeyboardSelected = isKeyboardSelected,
                    onPermissionRequested = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            }

            // Font Practice Workspace
            item {
                PracticeWorkspaceCard(
                    textInput = practiceText,
                    onTextInputChange = { practiceText = it },
                    isInAppKeyboardOpen = isInAppKeyboardOpen,
                    onToggleInAppKeyboard = { isInAppKeyboardOpen = !isInAppKeyboardOpen }
                )
            }

            // Keyboard Preferences & Behaviors
            item {
                KeyboardPreferencesCard(
                    fontStyleStrip = fontStyleStrip,
                    onToggleFontStyleStrip = { coroutineScope.launch { prefs.setSuggestionStrip(it) } },
                    showNumberRow = showNumberRow,
                    onToggleShowNumberRow = { coroutineScope.launch { prefs.setShowNumberRow(it) } },
                    autoCorrection = autoCorrection,
                    onToggleAutoCorrection = { coroutineScope.launch { prefs.setAutoCorrection(it) } },
                    autoCapitalization = autoCapitalization,
                    onToggleAutoCapitalization = { coroutineScope.launch { prefs.setAutoCapitalization(it) } },
                    hapticFeedback = hapticFeedback,
                    onToggleHapticFeedback = { coroutineScope.launch { prefs.setHapticFeedback(it) } },
                    keyPopup = keyPopup,
                    onToggleKeyPopup = { coroutineScope.launch { prefs.setKeyPopup(it) } },
                    incognitoMode = incognitoMode,
                    onToggleIncognitoMode = { coroutineScope.launch { prefs.setIncognitoMode(it) } }
                )
            }

            // AI & Gemini Settings
            item {
                AiSettingsCard(
                    hasApiKey = hasApiKey,
                    onSaveKey = { key ->
                        app.encryptedApiKeyManager.saveApiKey(key)
                        hasApiKey = true
                    },
                    onClearKey = {
                        app.encryptedApiKeyManager.clearApiKey()
                        hasApiKey = false
                    }
                )
            }

            // Custom Font Uploader
            item {
                CustomFontUploaderCard(
                    customFonts = customFonts,
                    onFontUploaded = { uri ->
                        val fileName = getFileNameFromUri(context, uri) ?: "font_${System.currentTimeMillis()}.ttf"
                        val savedFile = copyUriToInternalStorage(context, uri, "fonts", fileName)
                        if (savedFile != null) {
                            coroutineScope.launch {
                                repository.insertFont(
                                    CustomFont(
                                        name = fileName.removeSuffix(".ttf").removeSuffix(".otf"),
                                        filePath = savedFile.absolutePath
                                    )
                                )
                            }
                        }
                    },
                    onDeleteFont = { font ->
                        coroutineScope.launch {
                            repository.deleteFont(font)
                            File(font.filePath).delete()
                        }
                    }
                )
            }

            // Custom PNG Sticker & Emoji Pack Uploader
            item {
                CustomEmojiStickerCard(
                    customEmojis = customEmojis,
                    onEmojiUploaded = { uri, name ->
                        val fileName = "sticker_${System.currentTimeMillis()}.png"
                        val savedFile = copyUriToInternalStorage(context, uri, "emojis", fileName)
                        if (savedFile != null) {
                            coroutineScope.launch {
                                repository.insertEmoji(
                                    CustomEmoji(
                                        name = name,
                                        filePath = savedFile.absolutePath
                                    )
                                )
                            }
                        }
                    },
                    onDeleteEmoji = { emoji ->
                        coroutineScope.launch {
                            repository.deleteEmoji(emoji)
                            File(emoji.filePath).delete()
                        }
                    }
                )
            }

            // Text Shortcuts & Expander Manager
            item {
                TextShortcutManagerCard(
                    shortcuts = shortcuts,
                    onAddShortcut = { tag, expansion ->
                        coroutineScope.launch {
                            repository.insertShortcut(
                                TextShortcut(shortcut = tag, expansion = expansion, isEnabled = true)
                            )
                        }
                    },
                    onToggleShortcut = { shortcut ->
                        coroutineScope.launch {
                            repository.insertShortcut(shortcut.copy(isEnabled = !shortcut.isEnabled))
                        }
                    },
                    onDeleteShortcut = { shortcut ->
                        coroutineScope.launch {
                            repository.deleteShortcut(shortcut)
                        }
                    }
                )
            }

            // Clipboard History Manager
            item {
                ClipboardManagerCard(
                    clipboardItems = clipboardItems,
                    onTogglePin = { item ->
                        coroutineScope.launch {
                            repository.insertClipboardItem(item.copy(isPinned = !item.isPinned))
                        }
                    },
                    onDeleteItem = { item ->
                        coroutineScope.launch {
                            repository.deleteClipboardItem(item)
                        }
                    },
                    onClearUnpinned = {
                        coroutineScope.launch {
                            repository.clearUnpinnedClipboard()
                        }
                    }
                )
            }
        }

        // Live Interactive In-App Keyboard Preview
        if (isInAppKeyboardOpen) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                com.example.keyboard.KeyboardScreen(
                    inputConnection = inAppInputConnection,
                    repository = repository,
                    customFonts = customFonts,
                    customEmojis = customEmojis,
                    shortcuts = shortcuts,
                    clipboardItems = clipboardItems,
                    hapticEnabled = hapticFeedback,
                    autoCapEnabled = autoCapitalization,
                    autoCorrectEnabled = autoCorrection,
                    keyPopupEnabled = keyPopup,
                    showNumberRow = showNumberRow,
                    suggestionStripEnabled = fontStyleStrip,
                    recentEmojisList = listOf("😊", "👍", "❤️", "🔥", "🎉", "😂", "✨", "🙏"),
                    initialFloatingX = 0f,
                    initialFloatingY = 0f,
                    initialFloatingMode = false,
                    onVoiceDictationClick = { },
                    onShareStickerBitmap = { }
                )
            }
        }
    }
}

fun checkIsKeyboardEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
    val enabledImes = imm.enabledInputMethodList
    return enabledImes.any { it.packageName == context.packageName }
}

fun checkIsKeyboardSelected(context: Context): Boolean {
    val currentIME = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.DEFAULT_INPUT_METHOD
    )
    return currentIME != null && currentIME.startsWith(context.packageName)
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}

private fun copyUriToInternalStorage(context: Context, uri: Uri, folderName: String, fileName: String): File? {
    return try {
        val resolver = context.contentResolver
        val inputStream = resolver.openInputStream(uri) ?: return null
        val folder = File(context.filesDir, folderName).apply { mkdirs() }
        val destFile = File(folder, fileName)
        destFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        destFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
