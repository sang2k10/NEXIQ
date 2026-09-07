package com.screentranslator.domain.model

/**
 * Represents a natural language supported by the OCR / Translation pipeline.
 */
data class Language(
    val code: String,
    val displayName: String,
    val isAutoDetect: Boolean = false
) {
    companion object {
        val AUTO = Language(code = "auto", displayName = "Auto Detect", isAutoDetect = true)
        val VIETNAMESE = Language(code = "vi", displayName = "Vietnamese")
        val JAPANESE = Language(code = "ja", displayName = "Japanese")
        val ENGLISH = Language(code = "en", displayName = "English")
        val CHINESE = Language(code = "zh", displayName = "Chinese")
        val KOREAN = Language(code = "ko", displayName = "Korean")
        val FRENCH = Language(code = "fr", displayName = "French")
        val GERMAN = Language(code = "de", displayName = "German")
        val SPANISH = Language(code = "es", displayName = "Spanish")
        val RUSSIAN = Language(code = "ru", displayName = "Russian")

        val SUPPORTED_SOURCE_LANGUAGES = listOf(
            AUTO,
            JAPANESE,
            ENGLISH,
            CHINESE,
            KOREAN,
            VIETNAMESE,
            FRENCH,
            GERMAN,
            SPANISH,
            RUSSIAN
        )

        val SUPPORTED_TARGET_LANGUAGES = listOf(
            VIETNAMESE,
            ENGLISH,
            JAPANESE,
            CHINESE,
            KOREAN,
            FRENCH,
            GERMAN,
            SPANISH,
            RUSSIAN
        )

        fun fromCode(code: String): Language {
            if (code.equals("auto", ignoreCase = true)) return AUTO
            return SUPPORTED_TARGET_LANGUAGES.find { it.code.equals(code, ignoreCase = true) }
                ?: SUPPORTED_SOURCE_LANGUAGES.find { it.code.equals(code, ignoreCase = true) }
                ?: Language(code = code, displayName = code.uppercase())
        }
    }
}
