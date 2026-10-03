package com.example.data.questprovider

import com.example.data.game.Quest
import com.example.data.game.QuestsData
import com.example.domain.provider.QuestProvider

/**
 * Default implementation of [QuestProvider] backed by modular quest definitions.
 */
class QuestRepositoryImpl(
    private val questList: List<Quest> = QuestsData.allQuests
) : QuestProvider {

    override fun getAllQuests(): List<Quest> {
        return questList
    }

    override fun getQuestById(id: String): Quest? {
        return questList.find { it.id == id }
    }

    override fun getQuestsByPhase(phase: Int): List<Quest> {
        return questList.filter { it.phase == phase }
    }

    override fun getNextQuest(currentQuestId: String): Quest? {
        val index = questList.indexOfFirst { it.id == currentQuestId }
        return if (index != -1 && index + 1 < questList.size) {
            questList[index + 1]
        } else {
            null
        }
    }

    override fun getInitialQuest(): Quest {
        return questList.firstOrNull() ?: QuestsData.allQuests.first()
    }
}
