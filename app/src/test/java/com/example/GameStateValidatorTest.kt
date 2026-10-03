package com.example

import com.example.data.db.GameSaveEntity
import com.example.data.db.QuestProgressEntity
import com.example.data.db.VirtualFileEntity
import com.example.domain.validator.GameStateValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateValidatorTest {

    private val validator = GameStateValidator()

    @Test
    fun testValidGameSavePasses() {
        val save = GameSaveEntity(
            id = 1,
            osName = "GenesisOS",
            playerNick = "KernelArchitect",
            level = 2,
            xp = 450,
            bits = 200,
            currentPhase = 2
        )
        val result = validator.validateGameSave(save)
        assertTrue(result.isSuccess)
    }

    @Test
    fun testBlankOsNameFailsValidation() {
        val save = GameSaveEntity(osName = "  ")
        val result = validator.validateGameSave(save)
        assertFalse(result.isSuccess)
    }

    @Test
    fun testInvalidLevelFailsValidation() {
        val save = GameSaveEntity(level = 0)
        val result = validator.validateGameSave(save)
        assertFalse(result.isSuccess)
    }

    @Test
    fun testNegativeXpOrBitsFailsValidation() {
        val save = GameSaveEntity(xp = -50)
        val result = validator.validateGameSave(save)
        assertFalse(result.isSuccess)
    }

    @Test
    fun testQuestProgressValidation() {
        val validQuest = QuestProgressEntity(
            questId = "Q1_1_MBR_MAGIC",
            phase = 1,
            isUnlocked = true,
            isCompleted = false,
            userCode = "",
            stars = 3
        )
        assertTrue(validator.validateQuestProgress(validQuest).isSuccess)

        val invalidStars = QuestProgressEntity(
            questId = "Q1_1_MBR_MAGIC",
            phase = 1,
            isUnlocked = true,
            isCompleted = false,
            userCode = "",
            stars = 5
        )
        assertFalse(validator.validateQuestProgress(invalidStars).isSuccess)
    }

    @Test
    fun testVirtualFileValidation() {
        val validFile = VirtualFileEntity(
            path = "/boot/boot.asm",
            fileName = "boot.asm",
            content = "",
            language = "asm"
        )
        assertTrue(validator.validateVirtualFile(validFile).isSuccess)

        val invalidFile = VirtualFileEntity(
            path = "relative/path.c",
            fileName = "path.c",
            content = "",
            language = "c"
        )
        assertFalse(validator.validateVirtualFile(invalidFile).isSuccess)
    }
}
