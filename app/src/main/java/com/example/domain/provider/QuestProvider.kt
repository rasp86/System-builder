package com.example.domain.provider

import com.example.data.game.Quest

/**
 * Interface providing access to OS Architect quest definitions.
 */
interface QuestProvider {
    fun getAllQuests(): List<Quest>
    fun getQuestById(id: String): Quest?
    fun getQuestsByPhase(phase: Int): List<Quest>
    fun getNextQuest(currentQuestId: String): Quest?
    fun getInitialQuest(): Quest
}
