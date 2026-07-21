package com.example.utils

object FontStyler {

    enum class KeyboardFont(val displayName: String) {
        NORMAL("Normal"),
        SERIF_BOLD("Bold"),
        SERIF_ITALIC("Italic"),
        SERIF_BOLD_ITALIC("Bold Italic"),
        SCRIPT("Calligraphy"),
        DOUBLE_STRUCK("Outline"),
        GOTHIC("Gothic"),
        MONOSPACE("Monospace"),
        CIRCLED("Bubble"),
        SQUARED("Squared"),
        UPSIDE_DOWN("Upside Down"),
        REGRET("Slashed")
    }

    fun styleText(text: String, font: KeyboardFont): String {
        if (font == KeyboardFont.NORMAL) return text

        val sb = StringBuilder()
        for (char in text) {
            sb.append(styleChar(char, font))
        }
        return sb.toString()
    }

    private fun styleChar(char: Char, font: KeyboardFont): String {
        val code = char.code
        return when (font) {
            KeyboardFont.NORMAL -> char.toString()
            KeyboardFont.SERIF_BOLD -> {
                if (char in 'A'..'Z') {
                    getSmpString(0x1D400 + (code - 65))
                } else if (char in 'a'..'z') {
                    getSmpString(0x1D41A + (code - 97))
                } else if (char in '0'..'9') {
                    getSmpString(0x1D7CE + (code - 48))
                } else {
                    char.toString()
                }
            }
            KeyboardFont.SERIF_ITALIC -> {
                if (char in 'A'..'Z') {
                    // Some exceptions in math symbols
                    if (char == 'H') return "\u210B"
                    getSmpString(0x1D434 + (code - 65))
                } else if (char in 'a'..'z') {
                    if (char == 'h') return "\u210E"
                    getSmpString(0x1D44E + (code - 97))
                } else {
                    char.toString()
                }
            }
            KeyboardFont.SERIF_BOLD_ITALIC -> {
                if (char in 'A'..'Z') {
                    getSmpString(0x1D468 + (code - 65))
                } else if (char in 'a'..'z') {
                    getSmpString(0x1D482 + (code - 97))
                } else {
                    char.toString()
                }
            }
            KeyboardFont.SCRIPT -> {
                if (char in 'A'..'Z') {
                    getSmpString(0x1D4D0 + (code - 65))
                } else if (char in 'a'..'z') {
                    getSmpString(0x1D4EA + (code - 97))
                } else {
                    char.toString()
                }
            }
            KeyboardFont.DOUBLE_STRUCK -> {
                if (char in 'A'..'Z') {
                    // Exceptions
                    when (char) {
                        'C' -> return "\u2102"
                        'H' -> return "\u210D"
                        'N' -> return "\u2115"
                        'P' -> return "\u2119"
                        'Q' -> return "\u211A"
                        'R' -> return "\u211D"
                        'Z' -> return "\u2124"
                    }
                    getSmpString(0x1D538 + (code - 65))
                } else if (char in 'a'..'z') {
                    getSmpString(0x1D552 + (code - 97))
                } else if (char in '0'..'9') {
                    getSmpString(0x1D7D8 + (code - 48))
                } else {
                    char.toString()
                }
            }
            KeyboardFont.GOTHIC -> {
                if (char in 'A'..'Z') {
                    when (char) {
                        'C' -> return "\u212D"
                        'H' -> return "\u210C"
                        'I' -> return "\u2111"
                        'R' -> return "\u211C"
                        'Z' -> return "\u2128"
                    }
                    getSmpString(0x1D504 + (code - 65))
                } else if (char in 'a'..'z') {
                    getSmpString(0x1D51E + (code - 97))
                } else {
                    char.toString()
                }
            }
            KeyboardFont.MONOSPACE -> {
                if (char in 'A'..'Z') {
                    getSmpString(0x1D670 + (code - 65))
                } else if (char in 'a'..'z') {
                    getSmpString(0x1D68A + (code - 97))
                } else if (char in '0'..'9') {
                    getSmpString(0x1D7F6 + (code - 48))
                } else {
                    char.toString()
                }
            }
            KeyboardFont.CIRCLED -> {
                if (char in 'A'..'Z') {
                    getBmpString(0x24B6 + (code - 65))
                } else if (char in 'a'..'z') {
                    getBmpString(0x24D0 + (code - 97))
                } else if (char == '0') {
                    "\u24EA"
                } else if (char in '1'..'9') {
                    getBmpString(0x2460 + (code - 49))
                } else {
                    char.toString()
                }
            }
            KeyboardFont.SQUARED -> {
                if (char in 'A'..'Z') {
                    getSmpString(0x1F130 + (code - 65))
                } else if (char in 'a'..'z') {
                    getSmpString(0x1F130 + (code - 32 - 65)) // Capitalized squared
                } else {
                    char.toString()
                }
            }
            KeyboardFont.UPSIDE_DOWN -> {
                val normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!?.,'"
                val upside = "ɐqɔpǝɟƃɥᴉɾʞlɯuodbɹsʇnʌʍxʎz∀ᗺƆᗡƎℲ⅁HIſKꞀWNOԀΌᴚS┴∩ΛMX⅄Z0⇂ᄅƐㄣϛ9ㄥ86¡¿˙'╻"
                val idx = normal.indexOf(char)
                if (idx != -1) upside[idx].toString() else char.toString()
            }
            KeyboardFont.REGRET -> {
                // Slash character overlay
                char.toString() + "\u0338"
            }
        }
    }

    private fun getSmpString(codePoint: Int): String {
        return String(Character.toChars(codePoint))
    }

    private fun getBmpString(codePoint: Int): String {
        return codePoint.toChar().toString()
    }
}
