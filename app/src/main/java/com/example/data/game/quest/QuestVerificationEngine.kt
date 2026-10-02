package com.example.data.game.quest

import com.example.data.game.Quest
import com.example.ui.viewmodel.TestResultItem

/**
 * Handles executing and validating user kernel code against unit test cases.
 */
class QuestVerificationEngine {

    fun runTests(quest: Quest, userCode: String): Pair<List<TestResultItem>, Boolean> {
        val results = mutableListOf<TestResultItem>()
        var allPassed = true

        quest.testCases.forEach { tc ->
            val (passed, msg) = tc.check(userCode)
            if (!passed) allPassed = false
            results.add(TestResultItem(tc.description, passed, msg))
        }

        return results to allPassed
    }
}
