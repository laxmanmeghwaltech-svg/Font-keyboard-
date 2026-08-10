```markdown
# 🎨 FontKeyboard

<p align="center">
  <img src="assets/logo.png" alt="FontKeyboard Logo" width="180"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/version-1.0.0-blue.svg" alt="Version"/>
  <img src="https://img.shields.io/badge/platform-iOS%20%7C%20Android-green.svg" alt="Platform"/>
  <img src="https://img.shields.io/badge/license-MIT-orange.svg" alt="License"/>
  <img src="https://img.shields.io/badge/build-passing-brightgreen.svg" alt="Build"/>
</p>

<p align="center">
  <strong>A custom keyboard that lets you type in dozens of unique fonts and styles — anywhere you can type.</strong>
</p>

---

## 📖 Overview

**FontKeyboard** is a lightweight, privacy-focused custom keyboard application that empowers users to express themselves through a wide variety of fonts, styles, and decorative text. Whether you want elegant cursive for a bio, bold gothic for a headline, or playful bubble letters for a message — FontKeyboard makes it effortless.

The generated text uses Unicode characters, meaning it works seamlessly across **all apps and platforms** without requiring recipients to install anything.

---

## ✨ Features

- 🔤 **50+ Font Styles** — Cursive, Gothic, Bubble, Square, Strikethrough, Underline, and more
- ⚡ **Real-Time Preview** — See your text transformed as you type
- 📋 **One-Tap Copy** — Copy styled text to clipboard instantly
- 🌐 **Works Everywhere** — Social media, messaging apps, emails, documents
- 🔄 **Instant Switching** — Toggle between fonts without leaving the keyboard
- 🕐 **Recents & Favorites** — Quick access to your most-used styles
- 🌙 **Dark Mode** — Full dark/light theme support
- 🔒 **Privacy First** — No data collection, no internet permission required
- 🌍 **Multilingual Support** — Works with Latin-based alphabets and common symbols
- ♿ **Accessible** — VoiceOver / TalkBack compatible

---

## 📸 Screenshots

<p align="center">
  <img src="assets/screenshot-main.png" alt="Main Keyboard View" width="220"/>
  <img src="assets/screenshot-fonts.png" alt="Font Selection" width="220"/>
  <img src="assets/screenshot-preview.png" alt="Live Preview" width="220"/>
</p>

---

## 🚀 Getting Started

### Prerequisites

| Platform | Requirement |
|----------|-------------|
| iOS      | iOS 15.0+   |
| Android  | Android 8.0+ (API 26+) |

### Installation

#### iOS (TestFlight / App Store)

1. Download **FontKeyboard** from the App Store.
2. Navigate to **Settings → General → Keyboard → Keyboards → Add New Keyboard**.
3. Select **FontKeyboard**.
4. Tap the keyboard and enable **Allow Full Access** *(optional, for favorites sync)*.

#### Android (APK / Play Store)

1. Download **FontKeyboard** from the Play Store or the latest [release](../../releases).
2. Go to **Settings → System → Languages & Input → On-screen Keyboard → Manage Keyboards**.
3. Enable **FontKeyboard**.
4. Switch to FontKeyboard from your keyboard selector.

### Building from Source

```bash
# Clone the repository
git clone https://github.com/your-username/fontkeyboard.git
cd fontkeyboard

# --- iOS (Xcode 15+) ---
open FontKeyboard.xcodeproj

# --- Android (Gradle) ---
./gradlew assembleDebug
```

---

## 🖊️ Usage

1. **Open any text field** in any application.
2. **Switch to FontKeyboard** using your device's keyboard selector (🌐 / keyboard icon).
3. **Type your text** using the main keyboard layout.
4. **Browse font styles** by swiping the style carousel or tapping the font icon.
5. **Tap a style** to apply it. The preview updates in real time.
6. **Tap "Copy"** to send the styled text to your clipboard, or simply continue typing in the selected font.

---

## 🔠 Supported Font Styles (Sample)

| Style        | Example Output              |
|--------------|-----------------------------|
| Serif Bold   | 𝐇𝐞𝐥𝐥𝐨 𝐖𝐨𝐫𝐥𝐝              |
| Script       | 𝐻𝑒𝑙𝑙𝑜 𝑊𝑜𝑟𝑙𝑑               |
| Gothic       | ℌ𝔢𝔩𝔩𝔬 𝔚𝔬𝔯𝔩𝔡              |
| Bubble       | Ⓗⓔⓛⓛⓞ Ⓦⓞⓡⓛⓓ              |
| Monospace    | 𝙷𝚎𝚕𝚕𝚘 𝚆𝚘𝚛𝚕𝚍              |
| Strikethrough| H̶e̶l̶l̶o̶ ̶W̶o̶r̶l̶d̶          |
| Upside Down  | pןɹoM oןןǝH                 |

> Full list of 50+ styles available in-app.

---

## 🏗️ Project Structure

```
fontkeyboard/
├── ios/                    # iOS keyboard extension & app
│   ├── KeyboardExtension/
│   ├── App/
│   └── FontKeyboard.xcodeproj
├── android/                # Android IME & app
│   ├── app/
│   ├── keyboard/
│   └── build.gradle.kts
├── shared/                 # Cross-platform font mapping engine
│   ├── fontmaps/
│   └── utils/
├── assets/                 # Logos, screenshots, icons
├── docs/                   # Additional documentation
├── LICENSE
└── README.md
```

---

## 🤝 Contributing

Contributions are what make the open-source community amazing. Any contributions you make are **greatly appreciated**.

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/AmazingFont`).
3. Commit your changes (`git commit -m 'feat: add AmazingFont style'`).
4. Push to the branch (`git push origin feature/AmazingFont`).
5. Open a **Pull Request**.

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

---

## 🐛 Reporting Issues

Found a bug or have a feature request? Please open an issue using one of our templates:

- [Bug Report](../../issues/new?template=bug_report.md)
- [Feature Request](../../issues/new?template=feature_request.md)
- [New Font Request](../../issues/new?template=font_request.md)

---

## 📜 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Unicode character mappings inspired by the [Unicode Standard](https://unicode.org/).
- Design icons by [Lucide](https://lucide.dev/).
- Thanks to all beta testers who provided feedback.

---

## 📬 Contact & Support

- **Website:** [https://fontkeyboard.dev](https://fontkeyboard.dev)
- **Email:** support@fontkeyboard.dev
- **Twitter / X:** [@FontKeyboard](https://twitter.com/FontKeyboard)

---

<p align="center">
  Made with ❤️ by the FontKeyboard Team
</p>
```

This README is ready to drop into your repository. Just replace the placeholder URLs (e.g., `your-username`, `assets/` image paths) with your actual project details. It covers all the professional standards: badges, feature highlights, installation instructions for both platforms, usage guide, project structure, contribution guidelines, and licensing.
