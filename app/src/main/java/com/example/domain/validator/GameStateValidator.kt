package com.example.domain.validator

import com.example.data.db.GameSaveEntity
import com.example.data.db.QuestProgressEntity
import com.example.data.db.VirtualFileEntity
import com.example.domain.model.GameState
import com.example.domain.model.QuestProgress

class GameStateValidationException(message: String) : Exception(message)

class GameStateValidator {

    fun validateGameSave(save: GameSaveEntity): Result<Unit> {
        if (save.osName.isBlank()) {
            return Result.failure(GameStateValidationException("Nazwa systemu operacyjnego nie może być pusta"))
        }
        if (save.level < 1) {
            return Result.failure(GameStateValidationException("Poziom inżyniera jądra musi być >= 1"))
        }
        if (save.xp < 0 || save.bits < 0) {
            return Result.failure(GameStateValidationException("Wartości XP oraz Bits nie mogą być ujemne"))
        }
        if (save.currentPhase !in 1..10) {
            return Result.failure(GameStateValidationException("Nieprawidłowa faza rozwoju jądra: ${save.currentPhase}"))
        }
        return Result.success(Unit)
    }

    fun validateDomainGameState(state: GameState): Result<Unit> {
        if (state.osName.isBlank()) {
            return Result.failure(GameStateValidationException("Nazwa systemu operacyjnego nie może być pusta"))
        }
        if (state.level < 1) {
            return Result.failure(GameStateValidationException("Poziom inżyniera jądra musi być >= 1"))
        }
        if (state.xp < 0 || state.bits < 0) {
            return Result.failure(GameStateValidationException("Wartości XP oraz Bits nie mogą być ujemne"))
        }
        return Result.success(Unit)
    }

    fun validateQuestProgress(quest: QuestProgressEntity): Result<Unit> {
        if (quest.questId.isBlank()) {
            return Result.failure(GameStateValidationException("Identyfikator zadania nie może być pusty"))
        }
        if (quest.stars !in 0..3) {
            return Result.failure(GameStateValidationException("Liczba gwiazdek musi mieścić się w przedziale 0-3"))
        }
        return Result.success(Unit)
    }

    fun validateVirtualFile(file: VirtualFileEntity): Result<Unit> {
        if (file.path.isBlank() || !file.path.startsWith("/")) {
            return Result.failure(GameStateValidationException("Nieprawidłowa ścieżka pliku VFS: '${file.path}'"))
        }
        return Result.success(Unit)
    }
}
