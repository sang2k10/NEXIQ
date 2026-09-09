package com.screentranslator.data.engine.web

import com.screentranslator.domain.engine.TranslationEngine
import com.screentranslator.domain.model.Language
import kotlinx.coroutines.*
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Experimental Cloud Translation fallback using unauthenticated web translation.
 *
 * PRIVACY NOTICE:
 * This engine transmits recognized screen text over the internet to external servers.
 * It is marked as experimental/non-production and requires explicit user opt-in in Settings.
 * For privacy and security, requests use HTTP POST body rather than GET query strings.
 */
class GoogleWebTranslationEngine : TranslationEngine {

    override val id: String = "google_web"
    override val displayName: String = "Experimental Cloud (Web Fallback — Non-Production)"

    override suspend fun translate(
        text: String,
        sourceLang: Language,
        targetLang: Language
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (text.isBlank()) return@withContext Result.success("")

            val sl = if (sourceLang.isAutoDetect) "auto" else sourceLang.code
            val tl = targetLang.code

            // Endpoint without sensitive text in URL query params
            val urlString = "https://translate.googleapis.com/translate_a/single?client=gtx&dt=t"
            val url = URL(urlString)

            val postBody = "sl=" + URLEncoder.encode(sl, "UTF-8") +
                    "&tl=" + URLEncoder.encode(tl, "UTF-8") +
                    "&q=" + URLEncoder.encode(text, "UTF-8")

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("User-Agent", "NEXIQ-Android/1.0")
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            }

            try {
                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(postBody)
                    writer.flush()
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { reader ->
                        reader.readText()
                    }

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
            } finally {
                connection.disconnect()
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
