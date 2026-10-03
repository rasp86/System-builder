package com.example.core.security

import com.example.BuildConfig

class MissingConfigurationException(message: String) : Exception(message)

/**
 * Validates application security configuration and runtime secrets.
 */
class AppSecurityValidator {

    fun isGeminiApiKeyConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.contains("TODO")
    }

    fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    fun validateSecrets(): Result<Unit> {
        val apiKey = getApiKey()
        return if (isGeminiApiKeyConfigured()) {
            Result.success(Unit)
        } else {
            Result.failure(MissingConfigurationException("GEMINI_API_KEY is not configured. Running in offline architecture knowledge base mode."))
        }
    }
}
