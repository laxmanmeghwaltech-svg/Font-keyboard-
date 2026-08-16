# 🔤 Font Keyboard for Android

A high-performance Android Input Method (IME) with real-time Unicode font styling, smart autocorrect, voice dictation, custom clipboard history, and adaptive layout modes.

---

## 🌟 Features

- **Dynamic Font Ribbon**: Instantly switch between Bold, Italic, Calligraphy/Script, Gothic, Double-Struck (Outline), Monospace, Bubble, and Squared glyphs that work across any third-party app (WhatsApp, Instagram, Telegram).
- **Custom Font Engine (`FontManager`)**: In-memory caching and loading of `.ttf` and `.otf` typography files from assets and user storage with automatic fallback.
- **Companion Setup Wizard**: Direct system shortcuts to enable and select the keyboard with live status checking (`InputMethodManager`).
- **Interactive Practice Workspace**: Real-time test card in the companion app to practice typing and verify font transformations.
- **Smart Typos & Autocorrection**: Offline correction engine for common typing errors.
- **Clipboard History**: Persistent Room database tracking clipped text snippets for one-tap insertion.
- **Custom Stickers & Emoji Picker**: High-resolution text-to-sticker generation and clipboard sharing.
- **Adaptive Layout Modes**: Supports Standard Full-Width, Centered 12.1" Mode, Split Keyboard for thumb typing, and Floating Window draggable mode.
- **Comprehensive Customization**: Toggleable number row, haptic feedback, key popups, auto-capitalization, and incognito privacy mode.

---

## 🏗️ Architecture & Project Structure

```text
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── FontKeyboardApplication.kt
│   │   │   ├── FontKeyboardService.kt        # InputMethodService implementation
│   │   │   ├── MainActivity.kt               # Setup wizard & settings interface
│   │   │   ├── data/                         # Room DB, DataStore preferences & entities
│   │   │   ├── keyboard/                     # Compose keyboard layouts & key buttons
│   │   │   ├── screens/                      # Companion setup, tester, & config cards
│   │   │   ├── ui/                           # Material 3 theme & color schemes
│   │   │   └── utils/                        # FontManager, FontStyler & typo corrector
│   │   ├── res/
│   │   │   ├── drawable/                     # Key preview background & vector icons
│   │   │   ├── layout/                       # keyboard_view.xml, key_preview_layout.xml, activity_main.xml
│   │   │   ├── values/                       # strings.xml, colors.xml, themes.xml
│   │   │   └── xml/                          # method.xml (IME registration), file_paths.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                    # Version Catalog
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Installation & Setup

1. **Build & Install**: Install the generated APK on your Android device or emulator.
2. **Enable Keyboard**: Open **Font Keyboard**, tap **"1. Enable in Settings"**, and toggle on **Font Keyboard**.
3. **Select Keyboard**: Tap **"2. Select Input Method"** and choose **Font Keyboard**.
4. **Start Typing**: Open any app or use the built-in test card to type in styled Unicode fonts!

---

## 🛠️ Build Instructions

```bash
# Assemble Debug APK
gradle :app:assembleDebug

# Run local Robolectric unit tests
gradle :app:testDebugUnitTest
```

---

## 📄 License
Open source under the Apache 2.0 License.
