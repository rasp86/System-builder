package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.game.Quest
import com.example.data.repository.GameRepository
import com.example.domain.usecase.RunQuestTestsUseCase
import com.example.domain.usecase.SaveUserCodeUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Feature ViewModel managing user assembly/C code editor, compilation and unit testing.
 */
class EditorViewModel(
    private val runTestsUseCase: RunQuestTestsUseCase,
    private val saveCodeUseCase: SaveUserCodeUseCase,
    private val repository: GameRepository
) : ViewModel() {

    private val _editorCode = MutableStateFlow("")
    val editorCode: StateFlow<String> = _editorCode.asStateFlow()

    private val _testResults = MutableStateFlow<List<TestResultItem>>(emptyList())
    val testResults: StateFlow<List<TestResultItem>> = _testResults.asStateFlow()

    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    private var activeJob: Job? = null

    fun loadCodeForQuest(quest: Quest) {
        _editorCode.value = quest.defaultCode
        _testResults.value = emptyList()
        viewModelScope.launch {
            val progress = repository.getQuestById(quest.id)
            if (progress != null && progress.userCode.isNotBlank()) {
                _editorCode.value = progress.userCode
            }
        }
    }

    fun updateCode(newCode: String, quest: Quest?) {
        _editorCode.value = newCode
        if (quest != null) {
            viewModelScope.launch {
                saveCodeUseCase.execute(quest.id, newCode, quest.filePath)
            }
        }
    }

    fun resetToDefaultCode(quest: Quest) {
        _editorCode.value = quest.defaultCode
        viewModelScope.launch {
            saveCodeUseCase.execute(quest.id, quest.defaultCode, quest.filePath)
            _toastEvent.emit("Przywrócono domyślny szablon kodu.")
        }
    }

    fun resetToReferenceSolution(quest: Quest) {
        _editorCode.value = quest.referenceSolution
        viewModelScope.launch {
            saveCodeUseCase.execute(quest.id, quest.referenceSolution, quest.filePath)
            _toastEvent.emit("Załadowano rozwiązanie referencyjne!")
        }
    }

    fun runTests(
        quest: Quest,
        onAllTestsPassed: suspend (questId: String) -> Unit = {}
    ) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            _isCompiling.value = true
            delay(400) // Simulated test execution time

            val (results, allPassed) = runTestsUseCase.execute(quest, _editorCode.value)
            _testResults.value = results
            _isCompiling.value = false

            if (allPassed) {
                onAllTestsPassed(quest.id)
                _toastEvent.emit("Wszystkie testy zaliczone pomyślnie!")
            } else {
                _toastEvent.emit("Testy nie przeszły. Sprawdź błędy w edytorze.")
            }
        }
    }
}
