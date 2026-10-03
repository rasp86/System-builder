package com.example

import com.example.core.security.AppSecurityValidator
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppSecurityValidatorTest {

    private val validator = AppSecurityValidator()

    @Test
    fun testAppSecurityValidatorDoesNotCrashOnApiKeyCheck() {
        val configured = validator.isGeminiApiKeyConfigured()
        val key = validator.getApiKey()
        assertNotNull(key)

        val result = validator.validateSecrets()
        if (configured) {
            assert(result.isSuccess)
        } else {
            assert(result.isFailure)
        }
    }
}
