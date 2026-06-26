package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>, val role: String? = null)

@JsonClass(generateAdapter = true)
data class GeminiSystemInstruction(val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiSystemInstruction? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

// Since response content can sometimes have minor structural variances, we can use simpler parsing or explicit classes
@JsonClass(generateAdapter = true)
data class GeminiResponsePart(val text: String? = null)

@JsonClass(generateAdapter = true)
data class GeminiResponseContent(val parts: List<GeminiResponsePart>? = null)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiResponseContent? = null)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>? = null)

// News data classes that our UI will digest
@JsonClass(generateAdapter = true)
data class NewspaperVocabItem(
    val word: String,
    val definition: String,
    val examTip: String
)

@JsonClass(generateAdapter = true)
data class NewspaperQuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

@JsonClass(generateAdapter = true)
data class GeneratedNewspaperArticle(
    val title: String,
    val author: String,
    val readingTimeMin: Int,
    val content: String,
    val simplifiedContent: String,
    val vocabulary: List<NewspaperVocabItem>,
    val quiz: List<NewspaperQuizQuestion>
)

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Call the Gemini API directly via OkHttp to ensure 100% reliable execution.
     */
    suspend fun generateText(
        prompt: String,
        systemInstructionText: String? = null,
        isJson: Boolean = false
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is missing or default placeholder!")
            return@withContext null
        }

        val requestUrl = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

        val requestBodyData = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            systemInstruction = systemInstructionText?.let {
                GeminiSystemInstruction(parts = listOf(GeminiPart(text = it)))
            },
            generationConfig = if (isJson) {
                GeminiGenerationConfig(responseMimeType = "application/json", temperature = 0.2f)
            } else {
                GeminiGenerationConfig(temperature = 0.7f)
            }
        )

        val jsonAdapter = moshi.adapter(GeminiRequest::class.java)
        val requestJson = jsonAdapter.toJson(requestBodyData)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toRequestBody(mediaType)

        val httpRequest = Request.Builder()
            .url(requestUrl)
            .post(body)
            .build()

        try {
            okHttpClient.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string()
                    Log.e(TAG, "Error response code: ${response.code}, body: $errBody")
                    return@withContext null
                }

                val responseBody = response.body?.string() ?: return@withContext null
                val responseAdapter = moshi.adapter(GeminiResponse::class.java)
                val parsedResponse = responseAdapter.fromJson(responseBody)

                val resultText = parsedResponse?.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text

                return@withContext resultText
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call exception", e)
            return@withContext null
        }
    }

    /**
     * Generates a fully formatted newspaper article about any given topic, including vocabulary words and quizzes.
     */
    suspend fun generateNewspaperArticle(topic: String): GeneratedNewspaperArticle? {
        val prompt = """
            Create a highly educational and comprehensive daily newspaper-style article about: "$topic".
            Ensure the article has high-end scientific or cultural vocabulary suited for AP or collegiate level, and then provide a simplified, accessible summary.
            
            Return a JSON object matching this structure exactly:
            {
              "title": "Title of the Newspaper Article",
              "author": "Dr. Jane AI",
              "readingTimeMin": 3,
              "content": "The deep, detailed article containing complex terms and structures...",
              "simplifiedContent": "An elegant, simplified breakdown of the core concepts in 10th-grade English...",
              "vocabulary": [
                {
                  "word": "VocabWord1",
                  "definition": "Definition of VocabWord1",
                  "examTip": "Standard exam tip on how to use it in context."
                }
              ],
              "quiz": [
                {
                  "question": "Quiz question based on the content...",
                  "options": ["Option A", "Option B", "Option C", "Option D"],
                  "correctIndex": 0
                }
              ]
            }
            
            Extract exactly 3-5 high-end vocabulary words that appeared in the article. Provide 3 multiple-choice questions for the quiz with options and 0-indexed correct answer.
        """.trimIndent()

        val systemInstruction = "You are a senior science journalist and world-class tutor who explains complex topics elegantly."

        val jsonResult = generateText(prompt, systemInstruction, isJson = true) ?: return null

        return try {
            val adapter = moshi.adapter(GeneratedNewspaperArticle::class.java)
            adapter.fromJson(jsonResult)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse NewspaperArticle JSON: $jsonResult", e)
            null
        }
    }

    /**
     * Simulates an AI Tutor replying to a study chat.
     */
    suspend fun getAiTutorResponse(chatHistory: List<ChatMessage>, newMessageText: String): String {
        // Build thread history for context
        val contextPrompt = StringBuilder()
        contextPrompt.append("Here is the study group chat conversation so far:\n")
        for (msg in chatHistory.takeLast(10)) {
            contextPrompt.append("${msg.sender}: ${msg.text}\n")
        }
        contextPrompt.append("New Student Question: $newMessageText\n")
        contextPrompt.append("Provide your reply directly as @AI_Tutor. Keep it encouraging, educational, and highly engaging with bullet points where necessary. Limit to 3-4 concise sentences.")

        val systemInstruction = "You are @AI_Tutor, an expert, enthusiastic, and kind study coach who answers academic and productivity questions in a friendly collaborative student group chat."

        return generateText(contextPrompt.toString(), systemInstruction, isJson = false) 
            ?: "Hey there! I am currently processing your query. Feel free to re-ask or check your internet connection! @AI_Tutor"
    }
}
