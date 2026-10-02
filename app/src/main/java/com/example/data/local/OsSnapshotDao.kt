package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OsSnapshotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSnapshot(snapshot: OsSnapshotEntity)

    @Query("SELECT * FROM os_snapshots WHERE slotName = :slotName LIMIT 1")
    suspend fun getSnapshot(slotName: String): OsSnapshotEntity?

    @Query("SELECT * FROM os_snapshots ORDER BY timestamp DESC")
    fun getAllSnapshots(): Flow<List<OsSnapshotEntity>>

    @Query("DELETE FROM os_snapshots WHERE slotName = :slotName")
    suspend fun deleteSnapshot(slotName: String)

    @Query("SELECT COUNT(*) FROM os_snapshots")
    suspend fun getSnapshotCount(): Int
}
