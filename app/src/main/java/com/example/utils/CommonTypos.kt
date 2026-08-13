package com.example.utils

object CommonTypos {
    private val map = mapOf(
        "teh" to "the",
        "taht" to "that",
        "waht" to "what",
        "wiht" to "with",
        "hvae" to "have",
        "aot" to "out",
        "dont" to "don't",
        "cant" to "can't",
        "wont" to "won't",
        "im" to "I'm",
        "youre" to "you're",
        "theyre" to "they're",
        "hes" to "he's",
        "shes" to "she's",
        "its" to "it's",
        "thier" to "their",
        "recieve" to "receive",
        "seperate" to "separate",
        "occured" to "occurred"
    )

    fun getCorrection(word: String): String? {
        val lower = word.lowercase()
        val correction = map[lower] ?: return null
        return if (word.firstOrNull()?.isUpperCase() == true) {
            correction.replaceFirstChar { it.uppercase() }
        } else {
            correction
        }
    }
}
