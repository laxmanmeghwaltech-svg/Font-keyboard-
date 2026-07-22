# README.md


# FontBoard ⌨️

A Gboard-inspired Android keyboard with **17 Unicode text styles**, **custom font upload**, **emoji + custom sticker support**, a **floating dictation bar**, and **voice-to-text powered by Google Gemini** — built for large Android tablets.

> Type `hello` as `𝓱𝓮𝓵𝓵𝓸`, `𝕙𝕖𝕝𝕝𝕠`, `𝖍𝖊𝖑𝖑𝖔`, `🅗🅔🅛🅛🅞`, `ɥǝʃʃo`… in **any** app — WhatsApp, Telegram, Notes, Gmail, everywhere.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔤 **Unicode text styles** | Bold, Italic, Script, Fraktur, Double-struck, Monospace, Bubbles, Squared, Upside-down, Strikethrough & more — work in every app |
| 🖋 **Real fonts** | Bundled Google Fonts (OFL) applied to the keyboard UI |
| 📤 **Custom font upload** | Import your own `.ttf` / `.otf` files — no root needed |
| 🎙 **Voice dictation** | Speak → Gemini transcribes → text auto-inserted at the cursor |
| 🪟 **Floating bar** | Draggable overlay window for dictation & quick text insertion |
| 😊 **Emoji panel** | 7 categories + kaomoji |
| ⭐ **Custom emojis** | Import your own PNG/GIF stickers, pasted via `commitContent` |
| 🌑 **Gboard-style UI** | Dark Material theme, rounded keys, haptics, caps-lock, 2 symbol pages |
| 📱 **Tablet-optimized** | Number row, weight-based key sizing — designed for 12.4″ displays |

---

## 🧠 How the font system works

Android does not allow a keyboard to change the font rendered *inside* other apps. FontBoard solves this two ways:

1. **Unicode styles** — your typed characters are mapped to lookalike Unicode glyphs (Mathematical Alphanumeric Symbols, Enclosed Alphanumerics, etc.). These render differently in every app, messenger, and browser.
2. **UI fonts** — bundled and user-uploaded TTF/OTF fonts restyle the keyboard itself.

---

## 📋 Requirements

- Android **9.0+** (API 26)
- Android Studio Ladybug or newer (AGP 8.7+, Kotlin 2.0+)
- A [Google AI Studio API key](https://aistudio.google.com/apikey) (free tier) for voice dictation

---

## 🚀 Quick Start

### 1. Build

```bash
git clone <your-repo-url> FontBoard
cd FontBoard
# (optional) drop .ttf files into app/src/main/assets/fonts/
./gradlew assembleDebug


### 2. Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or copy the APK to your tablet and tap it (enable *Install from unknown sources*).

### 3. Enable the keyboard

1. Open the **FontBoard** app
2. Tap **Open system keyboard settings** → turn on *FontBoard Keyboard*
3. Back in the app → **Switch keyboard now** → select FontBoard

### 4. Set up voice dictation

1. Get a free API key at [aistudio.google.com](https://aistudio.google.com/apikey)
2. Paste it in the FontBoard app → **Save key**
3. In any text field, tap 🎙 → speak → tap 🎙 again → transcript is inserted automatically

---

## ⚙️ Configuration

| Task | How |
|---|---|
| Change text style | Tap 🎨 in the keyboard → pick a style (shown on the spacebar) |
| Change keyboard font | Tap 🎨 → scroll to the right → pick a font |
| Import a font | FontBoard app → *Import a font* → select `.ttf`/`.otf` |
| Import custom emojis | FontBoard app → *Import custom emojis* → select PNG/GIF files → find them under ⭐ |
| Floating bar | FontBoard app → *Launch floating bar* (grant "Display over other apps") |
| Change Gemini model | FontBoard app → model field (default: `gemini-2.5-flash`) |

---

## 📁 Project Structure

```
app/src/main/
├── java/com/fontboard/app/
│   ├── MainActivity.kt              # Setup wizard & settings
│   ├── ime/
│   │   ├── FontIME.kt               # InputMethodService core
│   │   ├── KeyboardView.kt          # Key layout & input handling
│   │   ├── FontStripView.kt         # Style/font picker strip
│   │   ├── EmojiPanelView.kt        # Emoji grid
│   │   └── EmojiData.kt             # Emoji dataset
│   ├── fonts/
│   │   ├── UnicodeStyles.kt         # Style definitions + StyleEngine
│   │   └── FontManager.kt           # Bundled & imported font loading
│   ├── voice/
│   │   └── VoiceDictation.kt        # Recorder + Gemini API client
│   └── floating/
│       └── FloatingBarService.kt    # Overlay dictation bar
├── assets/fonts/                    # ← put your .ttf/.otf files here
└── res/                             # Layouts, drawables, theme
```

---

## 🔐 Privacy & Security

- **Keystrokes never leave the device.** No logging, no analytics.
- Audio is sent to Google's Gemini API **only** while you actively record, using **your own** API key stored locally.
- Custom fonts and emojis are stored in app-private internal storage.
- Permissions used:
  - `RECORD_AUDIO` — voice dictation (runtime-requested)
  - `SYSTEM_ALERT_WINDOW` — floating bar
  - `INTERNET` — Gemini API calls

---

## 🏪 Publishing to Google Play

FontBoard is designed to stay compliant:

- ✅ Original branding — **not** affiliated with Google or Gboard
- ✅ Only OFL/Apache-licensed fonts may be bundled (verify before shipping!)
- ⚠️ **You must add**: a privacy policy URL, the Data Safety form (disclose audio collection), and a generated launcher icon
- 💡 For public release, proxy the Gemini key through a small backend (e.g., Cloud Run) instead of shipping it client-side

---

## 🗺 Roadmap

- [ ] Autocorrect & next-word prediction
- [ ] Glide/swipe typing
- [ ] Light theme + theme picker
- [ ] GIF search (Tenor API)
- [ ] Multilingual layouts
- [ ] Clipboard history

---

## 📄 License

- **Code:** MIT License
- **Fonts:** must be OFL/Apache-licensed (e.g., [Google Fonts](https://fonts.google.com))

---

## ⚠️ Disclaimer

FontBoard is an independent project and is **not** affiliated with, endorsed by, or connected to Google LLC. "Google Fonts" and "Gemini" are trademarks of their respective owners.
```

Drop this into the project root as `README.md`. Want me to also generate a `PRIVACY_POLICY.md` template (you'll need one for Play Store submission) or a `CONTRIBUTING.md`?
