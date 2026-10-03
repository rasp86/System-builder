package com.example.data.ai

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class AiSourceType(val displayName: String) {
    ONLINE_PRO_THINKING("Gemini 3.1 Pro (High Thinking)"),
    ONLINE_FLASH("Gemini 2.5 Flash"),
    OFFLINE_REASONING("Ada Kernel Engine (Offline)"),
    ERROR("Błąd połączenia")
}

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "role") val role: String? = null,
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "thinkingConfig") val thinkingConfig: GeminiThinkingConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiThinkingConfig(
    @Json(name = "thinkingLevel") val thinkingLevel: String = "HIGH"
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

data class AiMentorResult(
    val replyText: String,
    val reasoningSteps: List<String> = emptyList(),
    val isThinkingModelUsed: Boolean = true,
    val sourceType: AiSourceType = AiSourceType.OFFLINE_REASONING,
    val error: String? = null
)
