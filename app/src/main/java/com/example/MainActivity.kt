package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.ClipboardItem
import com.example.data.CustomEmoji
import com.example.data.CustomFont
import com.example.data.KeyboardDatabase
import com.example.data.KeyboardPreferencesManager
import com.example.data.TextShortcut
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.FontStyler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
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

    // Keyboard activation state checking
    var isKeyboardEnabled by remember { mutableStateOf(false) }
    var isKeyboardSelected by remember { mutableStateOf(false) }

    fun refreshKeyboardStatus() {
        isKeyboardEnabled = checkIsKeyboardEnabled(context)
        isKeyboardSelected = checkIsKeyboardSelected(context)
    }

    // Refresh state initially and when resuming
    LaunchedEffect(Unit) {
        refreshKeyboardStatus()
    }

    // Database access
    val db = remember { KeyboardDatabase.getInstance(context) }
    val customFonts = remember { mutableStateListOf<CustomFont>() }
    val customEmojis = remember { mutableStateListOf<CustomEmoji>() }

    LaunchedEffect(Unit) {
        db.fontDao().getAllFonts().collectLatest { fonts ->
            customFonts.clear()
            customFonts.addAll(fonts)
        }
    }

    LaunchedEffect(Unit) {
        db.emojiDao().getAllEmojis().collectLatest { emojis ->
            customEmojis.clear()
            customEmojis.addAll(emojis)
        }
    }

    // Audio recording permission state
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Microphone access granted for voice dictation!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Voice dictation requires audio recording permission.", Toast.LENGTH_LONG).show()
        }
    }

    // Custom Font File Picker Launcher (.ttf/.otf)
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            coroutineScope.launch(Dispatchers.IO) {
                val fileName = getFileNameFromUri(context, selectedUri) ?: "font_${System.currentTimeMillis()}.ttf"
                val copiedFile = copyUriToInternalStorage(context, selectedUri, "fonts", fileName)
                if (copiedFile != null) {
                    val fontName = fileName.substringBeforeLast(".")
                    val extension = fileName.substringAfterLast(".", "ttf").lowercase()
                    val customFont = CustomFont(
                        name = fontName,
                        filePath = copiedFile.absolutePath,
                        fileSize = copiedFile.length(),
                        fontFormat = extension
                    )
                    db.fontDao().insertFont(customFont)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Font '$fontName' uploaded successfully!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to load font file", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Custom Emoji Image Picker Launcher (PNG/JPG)
    val emojiPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            coroutineScope.launch(Dispatchers.IO) {
                val fileName = "sticker_${System.currentTimeMillis()}.png"
                val copiedFile = copyUriToInternalStorage(context, selectedUri, "emojis", fileName)
                if (copiedFile != null) {
                    val customEmoji = CustomEmoji(name = "Sticker", filePath = copiedFile.absolutePath)
                    db.emojiDao().insertEmoji(customEmoji)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Sticker added successfully!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to load image file", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Testing / Typing workspace state
    var testText by remember { mutableStateOf("") }
    var selectedFontMode by remember { mutableStateOf(FontStyler.KeyboardFont.NORMAL) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isTablet = maxWidth >= 600.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("main_settings_scroll"),
            contentPadding = PaddingValues(if (isTablet) 24.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- PROFESSIONAL POLISH & TABLET OPTIMIZED HEADER ---
            item {
                Card(
                    modifier = Modifier
                        .widthIn(max = 1100.dp)
                        .fillMaxWidth()
                        .testTag("hero_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // "FK" Avatar Badging
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD3E4FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "FK",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF001C38),
                                    fontSize = 20.sp
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "FontKey Pro",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    // Active status badge
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (isTablet) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "12.1\" TABLET ENHANCED",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFD3E4FF))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "PREMIUM",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF001C38)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isTablet) "Optimized for 12.1-inch Tablet Displays (Redmi Pad Pro 5G / HyperOS)" else "Connected to Google AI Studio",
                                    fontSize = 12.sp,
                                    color = Color(0xFF44474E)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "A fully functional Material 3 input method. Type in elegant custom Unicode & loaded OTF/TTF fonts anywhere, generate sticker graphics instantly, use split/floating keyboard modes on tablets, and dictate text with Gemini AI voice parsing.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // --- TABLET ERGONOMICS & SETTINGS DUAL-COLUMN LAYOUT ---
            item {
                Box(
                    modifier = Modifier
                        .widthIn(max = 1100.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (isTablet) {
                        // 2 Column Layout for 12.1" Tablet display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Column 1
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OnboardingActivationCard(
                                    isKeyboardEnabled = isKeyboardEnabled,
                                    isKeyboardSelected = isKeyboardSelected,
                                    context = context
                                )

                                TabletOptimizationInfoCard()

                                KeyboardPreferencesCard(
                                    context = context,
                                    coroutineScope = coroutineScope
                                )

                                VoiceDictationCard(
                                    hasMicPermission = hasMicPermission,
                                    micPermissionLauncher = micPermissionLauncher
                                )
                            }

                            // Column 2
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                PracticeWorkspaceCard(
                                    testText = testText,
                                    onTestTextChange = { testText = it },
                                    selectedFontMode = selectedFontMode,
                                    onFontModeChange = { selectedFontMode = it }
                                )

                                TextShortcutManagerCard(
                                    db = db,
                                    coroutineScope = coroutineScope
                                )

                                ClipboardManagerCard(
                                    db = db,
                                    coroutineScope = coroutineScope
                                )

                                CustomFontUploaderCard(
                                    customFonts = customFonts,
                                    fontPickerLauncher = fontPickerLauncher,
                                    coroutineScope = coroutineScope,
                                    db = db
                                )

                                CustomEmojiStickerCard(
                                    customEmojis = customEmojis,
                                    emojiPickerLauncher = emojiPickerLauncher,
                                    coroutineScope = coroutineScope,
                                    db = db,
                                    isTablet = true
                                )
                            }
                        }
                    } else {
                        // Single Column Layout for phones
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OnboardingActivationCard(
                                isKeyboardEnabled = isKeyboardEnabled,
                                isKeyboardSelected = isKeyboardSelected,
                                context = context
                            )

                            KeyboardPreferencesCard(
                                context = context,
                                coroutineScope = coroutineScope
                            )

                            VoiceDictationCard(
                                hasMicPermission = hasMicPermission,
                                micPermissionLauncher = micPermissionLauncher
                            )

                            PracticeWorkspaceCard(
                                testText = testText,
                                onTestTextChange = { testText = it },
                                selectedFontMode = selectedFontMode,
                                onFontModeChange = { selectedFontMode = it }
                            )

                            TextShortcutManagerCard(
                                db = db,
                                coroutineScope = coroutineScope
                            )

                            ClipboardManagerCard(
                                db = db,
                                coroutineScope = coroutineScope
                            )

                            CustomFontUploaderCard(
                                customFonts = customFonts,
                                fontPickerLauncher = fontPickerLauncher,
                                coroutineScope = coroutineScope,
                                db = db
                            )

                            CustomEmojiStickerCard(
                                customEmojis = customEmojis,
                                emojiPickerLauncher = emojiPickerLauncher,
                                coroutineScope = coroutineScope,
                                db = db,
                                isTablet = false
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-COMPOSABLES FOR MODULAR CLEANLINESS ---

@Composable
fun OnboardingActivationCard(
    isKeyboardEnabled: Boolean,
    isKeyboardSelected: Boolean,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_card"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Keyboard Activation Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Step 1: Enable Keyboard
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isKeyboardEnabled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isKeyboardEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Column {
                        Text(
                            text = "Step 1: Enable Keyboard",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toggle the Font Keyboard in settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    },
                    enabled = !isKeyboardEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Enable", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Step 2: Select Keyboard
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isKeyboardSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isKeyboardSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Column {
                        Text(
                            text = "Step 2: Active Keyboard",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Set Font Keyboard as primary input",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = {
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showInputMethodPicker()
                    },
                    enabled = isKeyboardEnabled && !isKeyboardSelected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Select", fontSize = 12.sp)
                }
            }

            if (isKeyboardEnabled && isKeyboardSelected) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PartyMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Excellent! Font Keyboard is active and ready to compose in any app.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabletOptimizationInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tablet_opt_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TabletAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "12.1\" Redmi Pad Pro 5G Optimization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tailored for 12.1-inch 2.5K high-refresh tablet displays with Xiaomi HyperOS multi-window support.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Split Thumb Keyboard Mode for comfortable 2-hand typing", fontSize = 12.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.PictureInPicture, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Floating Window Dock for split-screen apps & multitasking", fontSize = 12.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Create, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Stylus & Touch precision target scaling for Redmi Smart Pen", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun VoiceDictationCard(
    hasMicPermission: Boolean,
    micPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dictation_status_card"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Voice Dictation Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dictation relies on the Gemini 3.5-flash Audio API to transcribe your speech with remarkable precision.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (hasMicPermission) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = null,
                        tint = if (hasMicPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Microphone Permission",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasMicPermission) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                        contentColor = if (hasMicPermission) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(if (hasMicPermission) "Granted" else "Grant", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // API Key Check
            val isApiKeyConfigured = BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isApiKeyConfigured) Icons.Default.VpnKey else Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = if (isApiKeyConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Column {
                    Text(
                        text = "Google AI Studio API Secret",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isApiKeyConfigured) "Configured & Active in BuildConfig" else "GEMINI_API_KEY not configured in Secrets",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isApiKeyConfigured) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun PracticeWorkspaceCard(
    testText: String,
    onTestTextChange: (String) -> Unit,
    selectedFontMode: FontStyler.KeyboardFont,
    onFontModeChange: (FontStyler.KeyboardFont) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("practice_card"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Font Practice Workspace",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = testText,
                onValueChange = onTestTextChange,
                label = { Text("Type here to test fonts") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("practice_input"),
                trailingIcon = {
                    if (testText.isNotEmpty()) {
                        IconButton(onClick = { onTestTextChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scrolling selectors of font modes
            ScrollableTabRow(
                selectedTabIndex = selectedFontMode.ordinal,
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                FontStyler.KeyboardFont.values().forEach { font ->
                    Tab(
                        selected = selectedFontMode == font,
                        onClick = { onFontModeChange(font) },
                        text = { Text(font.displayName, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Live Styled Preview:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (testText.isEmpty()) "Live Preview text here..." else FontStyler.styleText(testText, selectedFontMode),
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (testText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun CustomFontUploaderCard(
    customFonts: List<CustomFont>,
    fontPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    db: KeyboardDatabase
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("font_manager_card"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Font Uploader (.otf / .ttf)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = { fontPickerLauncher.launch("*/*") },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Upload custom font files from free font websites. Stored locally, rendered into sticker graphics through your keyboard.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (customFonts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No custom fonts uploaded yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    customFonts.forEach { font ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = font.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Path: ${font.filePath.substringAfterLast("/")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            File(font.filePath).delete()
                                            db.fontDao().deleteFont(font)
                                        }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
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
fun CustomEmojiStickerCard(
    customEmojis: List<CustomEmoji>,
    emojiPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    db: KeyboardDatabase,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("emoji_manager_card"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Stickers / Emojis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = { emojiPickerLauncher.launch("image/*") },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add PNG", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add custom transparent PNG stickers to use directly in messenger apps through the keyboard.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (customEmojis.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No custom stickers uploaded yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (isTablet) 3 else 4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                ) {
                    items(customEmojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.2f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp)
                        ) {
                            AsyncImage(
                                model = File(emoji.filePath),
                                contentDescription = "Custom Sticker",
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                                    .clickable {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            File(emoji.filePath).delete()
                                            db.emojiDao().deleteEmoji(emoji)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextShortcutManagerCard(
    db: KeyboardDatabase,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    val shortcuts = remember { mutableStateListOf<TextShortcut>() }
    var shortcutText by remember { mutableStateOf("") }
    var expansionText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.shortcutDao().getAllShortcuts().collectLatest { list ->
            shortcuts.clear()
            shortcuts.addAll(list)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("text_shortcut_card"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Text Expander & Shortcuts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Type short codes (e.g. 'omw' -> 'On my way!') to expand instantly. Variables: {date}, {time}, {clipboard}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = shortcutText,
                    onValueChange = { shortcutText = it },
                    label = { Text("Code (e.g., omw)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(0.4f)
                )

                OutlinedTextField(
                    value = expansionText,
                    onValueChange = { expansionText = it },
                    label = { Text("Expanded Text", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (shortcutText.isNotBlank() && expansionText.isNotBlank()) {
                        coroutineScope.launch(Dispatchers.IO) {
                            db.shortcutDao().insertShortcut(
                                TextShortcut(shortcut = shortcutText.trim(), expansion = expansionText.trim())
                            )
                        }
                        shortcutText = ""
                        expansionText = ""
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Shortcut", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (shortcuts.isEmpty()) {
                Text(
                    text = "No custom shortcuts created yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    shortcuts.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.shortcut, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(text = item.expansion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            db.shortcutDao().deleteShortcut(item)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Shortcut", tint = MaterialTheme.colorScheme.error)
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
fun ClipboardManagerCard(
    db: KeyboardDatabase,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    val clips = remember { mutableStateListOf<ClipboardItem>() }

    LaunchedEffect(Unit) {
        db.clipboardDao().getAllClipboardItems().collectLatest { list ->
            clips.clear()
            clips.addAll(list)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("clipboard_manager_card"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Clipboard History Manager (${clips.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (clips.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                db.clipboardDao().clearUnpinnedClipboard()
                            }
                        }
                    ) {
                        Text("Clear Unpinned", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Clipboard items copied in any app appear here automatically. Pin items to preserve them.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (clips.isEmpty()) {
                Text(
                    text = "Clipboard history is currently empty.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    clips.take(5).forEach { clip ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (clip.isPinned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = clip.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    modifier = Modifier.weight(1f)
                                )

                                Row {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                db.clipboardDao().insertClipboardItem(clip.copy(isPinned = !clip.isPinned))
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (clip.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                            contentDescription = "Pin",
                                            tint = MaterialTheme.colorScheme.primary
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
}

@Composable
fun KeyboardPreferencesCard(
    context: Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    val prefsManager = remember { KeyboardPreferencesManager(context) }

    val hapticFeedback by prefsManager.hapticFeedbackFlow.collectAsState(initial = true)
    val autoCorrection by prefsManager.autoCorrectionFlow.collectAsState(initial = true)
    val autoCapitalization by prefsManager.autoCapitalizationFlow.collectAsState(initial = true)
    val keyPopup by prefsManager.keyPopupFlow.collectAsState(initial = true)
    val suggestionStrip by prefsManager.suggestionStripFlow.collectAsState(initial = true)
    val incognitoMode by prefsManager.incognitoModeFlow.collectAsState(initial = false)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("keyboard_preferences_card"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Keyboard Preferences (DataStore)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Customize feedback, typing behavior, and privacy settings stored persistently in DataStore.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            PreferenceToggleRow(
                title = "Haptic Feedback",
                description = "Tactile vibration pulse on every key tap",
                icon = Icons.Default.Vibration,
                checked = hapticFeedback,
                onCheckedChange = { enabled ->
                    coroutineScope.launch { prefsManager.setHapticFeedback(enabled) }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            PreferenceToggleRow(
                title = "Auto-Correction",
                description = "Automatically fix typos and common spelling errors",
                icon = Icons.Default.AutoFixHigh,
                checked = autoCorrection,
                onCheckedChange = { enabled ->
                    coroutineScope.launch { prefsManager.setAutoCorrection(enabled) }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            PreferenceToggleRow(
                title = "Auto-Capitalization",
                description = "Capitalize first character of every new sentence",
                icon = Icons.Default.Title,
                checked = autoCapitalization,
                onCheckedChange = { enabled ->
                    coroutineScope.launch { prefsManager.setAutoCapitalization(enabled) }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            PreferenceToggleRow(
                title = "Key Press Popup Preview",
                description = "Display enlarged letter bubble when pressing keys",
                icon = Icons.Default.FlipToFront,
                checked = keyPopup,
                onCheckedChange = { enabled ->
                    coroutineScope.launch { prefsManager.setKeyPopup(enabled) }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            PreferenceToggleRow(
                title = "Word Suggestion Strip",
                description = "Show quick font modes & suggestion bar above keyboard",
                icon = Icons.Default.FontDownload,
                checked = suggestionStrip,
                onCheckedChange = { enabled ->
                    coroutineScope.launch { prefsManager.setSuggestionStrip(enabled) }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            PreferenceToggleRow(
                title = "Incognito Privacy Mode",
                description = "Pause saving clipboard history while active",
                icon = Icons.Default.VisibilityOff,
                checked = incognitoMode,
                onCheckedChange = { enabled ->
                    coroutineScope.launch { prefsManager.setIncognitoMode(enabled) }
                }
            )
        }
    }
}

@Composable
private fun PreferenceToggleRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// --- ONBOARDING HELPER FUNCTIONS ---

fun checkIsKeyboardEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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

// --- FILE PARSING & COPYING UTILITIES ---

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
