package com.example.keyboard

import android.graphics.Bitmap
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardItem
import com.example.data.CustomEmoji
import com.example.data.CustomFont
import com.example.data.KeyboardPreferencesManager
import com.example.data.KeyboardRepository
import com.example.data.TextShortcut
import com.example.utils.FontStyler
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class TabletMode(val displayName: String) {
    FULL_WIDTH("Full Width"),
    CENTERED_COMPACT("Centered 12.1\""),
    SPLIT_THUMB("Split Keyboard"),
    FLOATING("Floating Window")
}

@Composable
fun KeyboardScreen(
    inputConnection: InputConnection?,
    repository: KeyboardRepository,
    customFonts: List<CustomFont>,
    customEmojis: List<CustomEmoji>,
    shortcuts: List<TextShortcut>,
    clipboardItems: List<ClipboardItem>,
    hapticEnabled: Boolean,
    autoCapEnabled: Boolean,
    autoCorrectEnabled: Boolean,
    keyPopupEnabled: Boolean,
    showNumberRow: Boolean,
    suggestionStripEnabled: Boolean,
    recentEmojisList: List<String>,
    initialFloatingX: Float,
    initialFloatingY: Float,
    initialFloatingMode: Boolean,
    onVoiceDictationClick: () -> Unit,
    onShareStickerBitmap: (Bitmap) -> Unit
) {
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var tabletMode by remember {
        mutableStateOf(if (initialFloatingMode) TabletMode.FLOATING else if (isTablet) TabletMode.SPLIT_THUMB else TabletMode.FULL_WIDTH)
    }
    var currentFontMode by remember { mutableStateOf(FontStyler.KeyboardFont.NORMAL) }
    var selectedCustomFont by remember { mutableStateOf<CustomFont?>(null) }
    var isShiftActive by remember { mutableStateOf(false) }
    var isSymbolsActive by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("keys") } // keys, font_strip, emojis, custom_fonts, ai, edit, clipboard, shortcuts

    // Draggable & Resizable floating offsets & size
    var offsetX by remember { mutableFloatStateOf(initialFloatingX) }
    var offsetY by remember { mutableFloatStateOf(initialFloatingY) }
    var floatingWidthDp by remember { mutableStateOf(460.dp) }

    val isFloating = tabletMode == TabletMode.FLOATING

    val containerModifier = when (tabletMode) {
        TabletMode.FLOATING -> {
            Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .width(floatingWidthDp)
                .wrapContentHeight()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        scope.launch {
                            repository.preferencesManager.setFloatingPosition(offsetX, offsetY)
                        }
                    }
                }
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.98f))
                .testTag("floating_keyboard_container")
        }
        TabletMode.CENTERED_COMPACT -> {
            Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .height(310.dp)
                .background(MaterialTheme.colorScheme.surface)
                .testTag("compact_keyboard_container")
        }
        TabletMode.SPLIT_THUMB, TabletMode.FULL_WIDTH -> {
            Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(MaterialTheme.colorScheme.surface)
                .testTag("docked_keyboard_container")
        }
    }

    Box(
        modifier = if (isFloating) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
        contentAlignment = if (isFloating) Alignment.Center else Alignment.BottomCenter
    ) {
        Surface(
            modifier = containerModifier,
            shadowElevation = if (isFloating) 12.dp else 4.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // TOOLBAR / TOP ROW
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick mode indicator & mode cycle button
                    IconButton(
                        onClick = {
                            tabletMode = when (tabletMode) {
                                TabletMode.FULL_WIDTH -> TabletMode.SPLIT_THUMB
                                TabletMode.SPLIT_THUMB -> TabletMode.CENTERED_COMPACT
                                TabletMode.CENTERED_COMPACT -> TabletMode.FLOATING
                                TabletMode.FLOATING -> TabletMode.FULL_WIDTH
                            }
                            scope.launch {
                                repository.preferencesManager.setFloatingMode(tabletMode == TabletMode.FLOATING)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isFloating) Icons.Default.OpenInFull else Icons.Outlined.AspectRatio,
                            contentDescription = "Toggle Mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    LazyRow(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            FilterChip(
                                selected = activeTab == "keys",
                                onClick = { activeTab = "keys" },
                                label = { Text("⌨️ Keys", fontSize = 12.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                        if (suggestionStripEnabled) {
                            item {
                                FilterChip(
                                    selected = activeTab == "font_strip",
                                    onClick = { activeTab = "font_strip" },
                                    label = { Text("✨ Styles", fontSize = 12.sp) },
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                        item {
                            FilterChip(
                                selected = activeTab == "emojis",
                                onClick = { activeTab = "emojis" },
                                label = { Text("😊 Emojis", fontSize = 12.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = activeTab == "custom_fonts",
                                onClick = { activeTab = "custom_fonts" },
                                label = { Text("🔤 TTF Fonts", fontSize = 12.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = activeTab == "ai",
                                onClick = { activeTab = "ai" },
                                label = { Text("🤖 Gemini AI", fontSize = 12.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = activeTab == "edit",
                                onClick = { activeTab = "edit" },
                                label = { Text("🎯 DPAD", fontSize = 12.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = activeTab == "clipboard",
                                onClick = { activeTab = "clipboard" },
                                label = { Text("📋 Clipboard", fontSize = 12.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = activeTab == "shortcuts",
                                onClick = { activeTab = "shortcuts" },
                                label = { Text("⚡ Shortcuts", fontSize = 12.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }

                    if (isFloating) {
                        Icon(
                            imageVector = Icons.Default.OpenWith,
                            contentDescription = "Drag Handle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                // Font style strip if enabled and activeTab is "font_strip"
                if (activeTab == "font_strip") {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(FontStyler.KeyboardFont.values()) { font ->
                            val sample = FontStyler.styleText("Font", font)
                            val isSelected = currentFontMode == font
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    currentFontMode = font
                                    activeTab = "keys"
                                },
                                label = { Text(sample, fontSize = 13.sp) },
                                modifier = Modifier.padding(horizontal = 3.dp)
                            )
                        }
                    }
                }

                // MAIN CONTENT PANEL BASED ON ACTIVE TAB
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (activeTab) {
                        "keys", "font_strip" -> {
                            if (isSymbolsActive) {
                                SymbolsKeyboardLayout(
                                    inputConnection = inputConnection,
                                    hapticEnabled = hapticEnabled,
                                    onBackToABC = { isSymbolsActive = false }
                                )
                            } else if (tabletMode == TabletMode.SPLIT_THUMB) {
                                SplitTabletKeyboardLayout(
                                    inputConnection = inputConnection,
                                    currentFontMode = currentFontMode,
                                    isShiftActive = isShiftActive,
                                    autoCapEnabled = autoCapEnabled,
                                    autoCorrectEnabled = autoCorrectEnabled,
                                    hapticEnabled = hapticEnabled,
                                    keyPopupEnabled = keyPopupEnabled,
                                    onToggleShift = { isShiftActive = !isShiftActive },
                                    onToggleSymbols = { isSymbolsActive = true },
                                    onVoiceDictationClick = onVoiceDictationClick
                                )
                            } else {
                                AlphabetKeyboardLayout(
                                    inputConnection = inputConnection,
                                    currentFontMode = currentFontMode,
                                    isShiftActive = isShiftActive,
                                    showNumberRow = showNumberRow,
                                    autoCapEnabled = autoCapEnabled,
                                    autoCorrectEnabled = autoCorrectEnabled,
                                    hapticEnabled = hapticEnabled,
                                    keyPopupEnabled = keyPopupEnabled,
                                    onToggleShift = { isShiftActive = !isShiftActive },
                                    onToggleSymbols = { isSymbolsActive = true },
                                    onVoiceDictationClick = onVoiceDictationClick
                                )
                            }
                        }

                        "emojis" -> {
                            EmojiPickerLayout(
                                inputConnection = inputConnection,
                                recentEmojisList = recentEmojisList,
                                customEmojis = customEmojis,
                                onEmojiUsed = { emoji ->
                                    scope.launch {
                                        repository.preferencesManager.addRecentEmoji(emoji)
                                    }
                                }
                            )
                        }

                        "custom_fonts" -> {
                            CustomFontsPickerLayout(
                                inputConnection = inputConnection,
                                customFonts = customFonts,
                                selectedCustomFont = selectedCustomFont,
                                onSelectCustomFont = { selectedCustomFont = it },
                                onShareStickerBitmap = onShareStickerBitmap
                            )
                        }

                        "ai" -> {
                            AiProofreaderLayout(
                                inputConnection = inputConnection,
                                aiService = repository.aiService
                            )
                        }

                        "edit" -> {
                            TextEditingLayout(
                                inputConnection = inputConnection,
                                hapticEnabled = hapticEnabled
                            )
                        }

                        "clipboard" -> {
                            ClipboardHistoryLayout(
                                inputConnection = inputConnection,
                                clipboardItems = clipboardItems,
                                onPinItem = { item ->
                                    scope.launch {
                                        repository.insertClipboardItem(item.copy(isPinned = !item.isPinned))
                                    }
                                },
                                onDeleteItem = { item ->
                                    scope.launch {
                                        repository.deleteClipboardItem(item)
                                    }
                                },
                                onClearUnpinned = {
                                    scope.launch {
                                        repository.clearUnpinnedClipboard()
                                    }
                                }
                            )
                        }

                        "shortcuts" -> {
                            TextShortcutsLayout(
                                inputConnection = inputConnection,
                                shortcuts = shortcuts
                            )
                        }
                    }
                }

                // Resizable handle in bottom right corner if floating
                if (isFloating) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val newWidth = (floatingWidthDp + dragAmount.x.dp).coerceIn(320.dp, 800.dp)
                                        floatingWidthDp = newWidth
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("◢", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
