package com.example.data.ai

import com.example.BuildConfig

/**
 * Service abstraction for the AI Kernel Mentor ("Ada").
 * Enforces configuration validation, secure secret checking, and graceful domain fallbacks.
 */
interface AiMentorService {
    suspend fun askMentor(
        userPrompt: String,
        questContext: String,
        codeContext: String
    ): AiMentorResult
}

class GeminiAiMentorService(
    private val geminiRepository: GeminiRepository = GeminiRepository()
) : AiMentorService {

    override suspend fun askMentor(
        userPrompt: String,
        questContext: String,
        codeContext: String
    ): AiMentorResult {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // Validate API Key presence and format
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("TODO")) {
            return geminiRepository.generateOfflineArchitectResponse(userPrompt, questContext, codeContext)
        }

        return try {
            geminiRepository.askKernelArchitectMentor(userPrompt, questContext, codeContext)
        } catch (e: Exception) {
            geminiRepository.generateOfflineArchitectResponse(userPrompt, questContext, codeContext)
        }
    }
}
