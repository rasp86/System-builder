package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_save WHERE id = 1")
    fun getGameSave(): Flow<GameSaveEntity?>

    @Query("SELECT * FROM game_save WHERE id = 1")
    suspend fun getGameSaveSync(): GameSaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameSave(save: GameSaveEntity)

    @Update
    suspend fun updateGameSave(save: GameSaveEntity)

    // Quests
    @Query("SELECT * FROM quest_progress ORDER BY phase ASC, questId ASC")
    fun getAllQuests(): Flow<List<QuestProgressEntity>>

    @Query("SELECT * FROM quest_progress WHERE questId = :questId")
    suspend fun getQuestById(questId: String): QuestProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateQuest(quest: QuestProgressEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestsIfNotExist(quests: List<QuestProgressEntity>)

    // Virtual Files
    @Query("SELECT * FROM virtual_files ORDER BY path ASC")
    fun getAllFiles(): Flow<List<VirtualFileEntity>>

    @Query("SELECT * FROM virtual_files WHERE path = :path")
    suspend fun getFileByPath(path: String): VirtualFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFile(file: VirtualFileEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFilesIfNotExist(files: List<VirtualFileEntity>)

    @Query("DELETE FROM virtual_files WHERE path = :path")
    suspend fun deleteFileByPath(path: String)

    // AI Messages
    @Query("SELECT * FROM ai_messages ORDER BY timestamp ASC")
    fun getAllAiMessages(): Flow<List<AiMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiMessage(message: AiMessageEntity)

    @Query("DELETE FROM ai_messages")
    suspend fun clearAiHistory()
}
