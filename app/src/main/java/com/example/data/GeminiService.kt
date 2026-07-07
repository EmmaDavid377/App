package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Moshi data class configurations matching Gemini endpoint
@JsonClass(generateAdapter = true)
data class Part(val text: String)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(val contents: List<Content>)

@JsonClass(generateAdapter = true)
data class Candidate(val content: Content)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(val candidates: List<Candidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    // Configure client with generous timeouts as required by the Gemini API gotchas
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

object GeminiHelper {
    suspend fun draftReply(rentalDetails: String, guestInquiry: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return "Note: Gemini API key is not configured in Secrets. Here is a generic draft:\n\n" +
                   "Hello! Thank you for reaching out. Let me look into that for you. Detailed instruction / info is currently unavailable offline."
        }

        val prompt = """
            You are a polite, helpful assistant coordinating an event/rental.
            
            Context of the event/rental is:
            $rentalDetails
            
            The guest asks:
            "$guestInquiry"
            
            Please draft a warm, informative, professional, and precise reply on behalf of the coordinator. Keep the answer accurate to the context provided. Do not invent any wifi passwords, house codes or rules that aren't mentioned in the context. Keep the tone friendly and elegant.
        """.trimIndent()

        return executePrompt(prompt, apiKey)
    }

    suspend fun draftLogisticsChecklist(eventName: String, eventType: String, description: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return "Note: Gemini API key is not configured in Secrets.\n\n" +
                   "- Set up check-in table\n" +
                   "- Send welcome message to guests\n" +
                   "- Complete property safety inspection"
        }

        val prompt = """
            You are a professional logistics and coordinator coordinator. 
            Help draft a checklist of 5 critical tasks for a:
            Type: $eventType
            Name: $eventName
            Description: $description
            
            Provide each task on a new line starting with an asterisk '*'. Keep them actionable and short (under 10 words). Include categories: "Setup", "Catering", "Welcome", "Cleanup", "Security" if appropriate.
        """.trimIndent()

        return executePrompt(prompt, apiKey)
    }

    suspend fun generateEventIdeas(promptText: String, requestedType: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return "Note: Gemini API key is not configured in Secrets. Here are some default creative suggestions for your event:\n\n" +
                   "• Theme: Retro Neon Oasis\n" +
                   "• Decor: Incorporate bright neon signs, matching custom balloons, and starry fairy lights\n" +
                   "• Layout: Cozy round seating around a centered spotlight stage\n" +
                   "• Accent Colors: Electric Blue, Hot Violet, and Matte Gold"
        }

        val prompt = if (requestedType == "text") {
            """
            You are a creative, professional party decorator and event stylist. 
            The customer is requesting event theme and decoration ideas with the following description:
            "$promptText"
            
            Provide some amazing, cohesive aesthetic styling instructions, recommended color schemes, visual layout ideas, and specific details. Organize with clear headers or modern bullet points.
            """.trimIndent()
        } else {
            """
            You are a creative AI graphic concept artist for luxury events.
            The user wants to generate image background design ideas or aesthetic descriptions for their event:
            "$promptText"
            
            Write a detailed, highly cinematic, atmospheric artist visual description/artwork brief. Explain the visual scene, lighting style, camera angle, backdrop elements, and mood so they can visualize of how a gorgeous photography backdrop for their event would look.
            """.trimIndent()
        }

        return executePrompt(prompt, apiKey)
    }

    private suspend fun executePrompt(prompt: String, apiKey: String): String {
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )
        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No response from coordinator assistant. Please try again."
        } catch (e: Exception) {
            "Error draft message: ${e.message ?: "Unknown error"}. Check internet connection or API keys."
        }
    }
}
