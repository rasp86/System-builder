package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "os_snapshots")
data class OsSnapshotEntity(
    @PrimaryKey
    val slotName: String = "default",
    val timestamp: Long = System.currentTimeMillis(),
    val totalCycles: Long = 0L,
    val lastOpMnemonic: String = "NOP",
    val eax: String = "0x00000000",
    val ebx: String = "0x00000000",
    val ecx: String = "0x00000000",
    val edx: String = "0x00000000",
    val esp: String = "0x00090000",
    val ebp: String = "0x00000000",
    val eip: String = "0x00100000",
    val eflags: String = "0x00000202",
    val cr0: String = "0x80000011",
    val cr3: String = "0x0009C000",
    val cs: String = "0x08",
    val ds: String = "0x10",
    val serializedVfs: String = "",
    val serializedMemory: String = ""
)
