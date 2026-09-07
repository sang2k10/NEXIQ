package com.screentranslator.data.engine.web

import com.screentranslator.domain.engine.TranslationEngine
import com.screentranslator.domain.model.Language
import kotlinx.coroutines.*
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class GoogleWebTranslationEngine : TranslationEngine {

    override val id: String = "google_web"
    override val displayName: String = "Google Translate (Cloud)"

    override suspend fun translate(
        text: String,
        sourceLang: Language,
        targetLang: Language
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (text.isBlank()) return@withContext Result.success("")

            val sl = if (sourceLang.isAutoDetect) "auto" else sourceLang.code
            val tl = targetLang.code
            val encodedQuery = URLEncoder.encode(text, "UTF-8")

            val urlString = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sl&tl=$tl&dt=t&q=$encodedQuery"
            val url = URL(urlString)

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val jsonArray = JSONArray(response)
                val sentencesArray = jsonArray.getJSONArray(0)
                val resultBuilder = StringBuilder()

                for (i in 0 until sentencesArray.length()) {
                    val sentence = sentencesArray.getJSONArray(i)
                    resultBuilder.append(sentence.getString(0))
                }

                Result.success(resultBuilder.toString())
            } else {
                Result.failure(IllegalStateException("HTTP ${connection.responseCode}: ${connection.responseMessage}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun translateBatch(
        texts: List<String>,
        sourceLang: Language,
        targetLang: Language
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            if (texts.isEmpty()) return@withContext Result.success(emptyList())

            val deferredResults = texts.map { text ->
                async {
                    val res = translate(text, sourceLang, targetLang)
                    res.getOrDefault(text)
                }
            }

            Result.success(deferredResults.awaitAll())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSupportedLanguages(): List<Language> {
        return Language.SUPPORTED_TARGET_LANGUAGES
    }
}
