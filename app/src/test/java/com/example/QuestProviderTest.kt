package com.example

import com.example.data.game.QuestsData
import com.example.data.questprovider.QuestRepositoryImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestProviderTest {

    @Test
    fun testGetAllQuestsReturnsNonEmptyList() {
        val provider = QuestRepositoryImpl()
        val quests = provider.getAllQuests()

        assertTrue(quests.isNotEmpty())
        assertEquals(QuestsData.allQuests.size, quests.size)
    }

    @Test
    fun testGetQuestById() {
        val provider = QuestRepositoryImpl()
        val quest = provider.getQuestById("Q1_1_MBR_MAGIC")

        assertNotNull(quest)
        assertEquals("Q1_1_MBR_MAGIC", quest?.id)
        assertEquals(1, quest?.phase)
    }

    @Test
    fun testGetQuestsByPhase() {
        val provider = QuestRepositoryImpl()
        val phase1Quests = provider.getQuestsByPhase(1)

        assertTrue(phase1Quests.isNotEmpty())
        assertTrue(phase1Quests.all { it.phase == 1 })
    }

    @Test
    fun testGetNextQuest() {
        val provider = QuestRepositoryImpl()
        val next = provider.getNextQuest("Q1_1_MBR_MAGIC")

        assertNotNull(next)
        assertEquals("Q1_2_GDT_PROTECTED_MODE", next?.id)
    }

    @Test
    fun testGetInitialQuest() {
        val provider = QuestRepositoryImpl()
        val initial = provider.getInitialQuest()

        assertNotNull(initial)
        assertEquals("Q1_1_MBR_MAGIC", initial.id)
    }
}
