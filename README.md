<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="110" alt="Font Keyboard icon" />

# ⌨️ Font Keyboard

**A custom Android keyboard with Unicode fonts, AI proofreading, voice dictation & more.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Gemini API](https://img.shields.io/badge/Gemini-API-8E75FF?style=for-the-badge&logo=googlegemini&logoColor=white)](https://ai.google.dev/)
[![License](https://img.shields.io/badge/License-Not%20Set-lightgrey?style=for-the-badge)](#license)

[![Get APK on GitHub](https://img.shields.io/badge/Get%20APK-on%20GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/laxmanmeghwaltech-svg/Font-keyboard-/releases)
[![Star this repo](https://img.shields.io/github/stars/laxmanmeghwaltech-svg/Font-keyboard-?style=for-the-badge&color=FFD21E&logo=github)](https://github.com/laxmanmeghwaltech-svg/Font-keyboard-/stargazers)
[![Last Commit](https://img.shields.io/github/last-commit/laxmanmeghwaltech-svg/Font-keyboard-?style=for-the-badge&color=orange)](https://github.com/laxmanmeghwaltech-svg/Font-keyboard-/commits)

</div>

<br>

> ⚠️ No APK is published under **Releases** yet — the badge above will work as soon as you publish one. See [Publishing a Release](#-publishing-a-release-so-the-apk-badge-works) below.

---

## ✨ Features

| | |
|---|---|
| 🔤 **Unicode Font Styling** | Bold, Italic, Bold-Italic, Calligraphy Script, Outline, Gothic, Monospace, Bubble, Squared, Upside Down & Slashed — rendered via Unicode SMP mapping, works in any app instantly. |
| 📁 **Custom Font Upload** | Import and use your own font files directly from the keyboard. |
| 🤖 **AI Proofreader** | Rewrite text as Proofread, Formal, Friendly, Summarized, or Translated — powered by Gemini. |
| 🎙️ **Voice Dictation** | Speech-to-text transcription powered by Gemini. |
| 😀 **Custom Emoji & Stickers** | Add and use your own personalized emoji/sticker sets. |
| 📋 **Clipboard Manager** | Persistent clipboard history backed by Room, with quick paste-back. |
| ⚡ **Text Shortcuts** | Save and auto-expand custom text snippets. |
| 🖥️ **Split / Tablet Layout** | Dedicated split keyboard layout optimized for larger screens. |
| 🧪 **In-App Practice Workspace** | Try fonts and keyboard behavior without leaving the app. |
| 🚀 **Guided Onboarding** | Step-by-step setup for enabling the keyboard and granting permissions. |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | Android Input Method Service (IME) |
| Local Storage | Room, DataStore Preferences |
| Networking | Retrofit, OkHttp, Moshi |
| AI | Gemini API (proofreading & transcription) |
| Backend | Firebase (AI, App Check) |
| Image Loading | Coil |
| Security | AndroidX Security Crypto (encrypted API key storage) |
| Testing | JUnit, Espresso, Robolectric, Roborazzi (screenshot testing) |

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | Android Input Method Service (IME) |
| Local Storage | Room, DataStore Preferences |
| Networking | Retrofit, OkHttp, Moshi |
| AI | Gemini API (proofreading & transcription) |
| Backend | Firebase (AI, App Check) |
| Image Loading | Coil |
| Security | AndroidX Security Crypto (encrypted API key storage) |
| Testing | JUnit, Espresso, Robolectric, Roborazzi (screenshot testing) |

<div align="center">

**Min SDK 24** · **Target/Compile SDK 36** · **Kotlin 2.2.10** · **AGP 9.1.1**

</div>

---

## 📂 Project Structure

```
app/src/main/java/com/example/
├── data/          # Room entities, database, repository, preferences, AI service
├── keyboard/      # Keyboard layouts: alphabet, symbols, emoji, clipboard, split/tablet, AI proofreader
├── screens/       # Compose screens: onboarding, settings, font uploader, emoji/sticker manager
├── ui/theme/      # Compose theming (colors, typography)
├── utils/         # Font styling engine, input connection helpers, typo correction
├── FontKeyboardApplication.kt
├── FontKeyboardService.kt   # Core InputMethodService
└── MainActivity.kt          # Companion app for setup & settings
```

---

## 🚀 Getting Started

### Prerequisites
- 🧰 Android Studio (latest stable)
- ☕ JDK 11+
- 🔑 A Gemini API key from [Google AI Studio](https://aistudio.google.com/)

### 1️⃣ Clone & Configure

```bash
git clone https://github.com/laxmanmeghwaltech-svg/Font-keyboard-.git
cd Font-keyboard-
```

Create a `.env` file in the project root (see `.env.example`) with your Gemini API key:

```
GEMINI_API_KEY=your_actual_api_key_here
```

> 💡 You can also add/store your Gemini API key at runtime via the in-app **Settings** screen — it's encrypted locally with `EncryptedSharedPreferences`.

### 2️⃣ Build & Run

Open the project in Android Studio and hit ▶️ Run, or use the CLI:

```bash
./gradlew assembleDebug
```

### 3️⃣ Enable the Keyboard

1. 📲 Launch the app and complete onboarding.
2. ⚙️ Go to **Settings → System → Languages & input → On-screen keyboard**, enable **Font Keyboard**.
3. 🔁 Switch to it from any text field using your device's keyboard switcher.

---

## 🔐 Permissions

| Permission | Reason |
|---|---|
| `INTERNET` | Gemini API calls (proofreading, translation, transcription) |
| `RECORD_AUDIO` | Voice dictation |

---

## 📦 Publishing a Release (so the APK badge works)

The **"Get APK on GitHub"** badge at the top links to this repo's Releases page. To make it functional:

```bash
./gradlew assembleRelease
```

Then on GitHub: **Releases → Draft a new release → attach the generated `.apk` from `app/build/outputs/apk/release/` → Publish.**
Once published, the badge automatically takes visitors straight to the latest downloadable APK.

---

## 🗺️ Roadmap

- [ ] Additional font style packs
- [ ] Theming/customization for keyboard appearance
- [ ] Cloud sync for shortcuts & clipboard history
- [ ] Google Play Store listing

---

## 📄 License

No license file is currently included in this repository. Add one (e.g. MIT, Apache 2.0) if you intend for others to use or contribute to this project.

---

<div align="center">

Built with ❤️ by **Laxman Meghwal** — [Laxman AI Systems](https://github.com/laxmanmeghwaltech-svg)

[![GitHub](https://img.shields.io/badge/GitHub-Follow-181717?style=flat-square&logo=github)](https://github.com/laxmanmeghwaltech-svg)

</div>
