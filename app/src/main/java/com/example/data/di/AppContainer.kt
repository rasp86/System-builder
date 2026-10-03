package com.example.data.di

import android.content.Context
import com.example.core.security.AppSecurityValidator
import com.example.data.ai.AiMentorService
import com.example.data.ai.GeminiAiMentorService
import com.example.data.ai.GeminiRepository
import com.example.data.db.AppDatabase
import com.example.data.game.TerminalEngine
import com.example.data.game.quest.QuestVerificationEngine
import com.example.data.game.vm.VirtualMachineEngine
import com.example.data.local.GenesisDatabase
import com.example.data.local.OsSnapshotRepository
import com.example.data.repository.GameRepository
import com.example.domain.usecase.AskAiMentorUseCase
import com.example.domain.usecase.BootVirtualMachineUseCase
import com.example.domain.usecase.CompleteQuestUseCase
import com.example.domain.usecase.ExecuteTerminalCommandUseCase
import com.example.domain.usecase.RunQuestTestsUseCase
import com.example.domain.usecase.SaveUserCodeUseCase

/**
 * Dependency Injection container providing application-scoped services.
 */
interface AppContainer {
    val gameRepository: GameRepository
    val osSnapshotRepository: OsSnapshotRepository
    val terminalEngine: TerminalEngine
    val vmEngine: VirtualMachineEngine
    val questVerificationEngine: QuestVerificationEngine
    val aiMentorService: AiMentorService
    val securityValidator: AppSecurityValidator
    val questProvider: com.example.domain.provider.QuestProvider

    // Domain Use Cases
    val runQuestTestsUseCase: RunQuestTestsUseCase
    val completeQuestUseCase: CompleteQuestUseCase
    val saveUserCodeUseCase: SaveUserCodeUseCase
    val executeTerminalCommandUseCase: ExecuteTerminalCommandUseCase
    val askAiMentorUseCase: AskAiMentorUseCase
    val bootVirtualMachineUseCase: BootVirtualMachineUseCase
}

/**
 * Default implementation of [AppContainer] creating real repository and engine instances.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    private val appDatabase: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    private val genesisDatabase: GenesisDatabase by lazy {
        GenesisDatabase.getInstance(context)
    }

    override val securityValidator: AppSecurityValidator by lazy {
        AppSecurityValidator()
    }

    override val questProvider: com.example.domain.provider.QuestProvider by lazy {
        com.example.data.questprovider.QuestRepositoryImpl()
    }

    override val osSnapshotRepository: OsSnapshotRepository by lazy {
        OsSnapshotRepository(genesisDatabase.osSnapshotDao())
    }

    override val aiMentorService: AiMentorService by lazy {
        GeminiAiMentorService(GeminiRepository())
    }

    override val gameRepository: GameRepository by lazy {
        GameRepository(appDatabase, aiMentorService)
    }

    override val terminalEngine: TerminalEngine by lazy {
        TerminalEngine().apply {
            snapshotRepository = osSnapshotRepository
        }
    }

    override val vmEngine: VirtualMachineEngine by lazy {
        VirtualMachineEngine()
    }

    override val questVerificationEngine: QuestVerificationEngine by lazy {
        QuestVerificationEngine()
    }

    override val runQuestTestsUseCase: RunQuestTestsUseCase by lazy {
        RunQuestTestsUseCase(questVerificationEngine)
    }

    override val completeQuestUseCase: CompleteQuestUseCase by lazy {
        CompleteQuestUseCase(gameRepository)
    }

    override val saveUserCodeUseCase: SaveUserCodeUseCase by lazy {
        SaveUserCodeUseCase(gameRepository)
    }

    override val executeTerminalCommandUseCase: ExecuteTerminalCommandUseCase by lazy {
        ExecuteTerminalCommandUseCase(terminalEngine)
    }

    override val askAiMentorUseCase: AskAiMentorUseCase by lazy {
        AskAiMentorUseCase(gameRepository)
    }

    override val bootVirtualMachineUseCase: BootVirtualMachineUseCase by lazy {
        BootVirtualMachineUseCase(vmEngine)
    }
}
