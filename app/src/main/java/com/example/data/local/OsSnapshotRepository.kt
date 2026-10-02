package com.example.data.local

import com.example.data.game.CpuRegisters
import com.example.data.game.FsNodeType
import com.example.data.game.VirtualFsNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class OsSnapshotRepository(private val dao: OsSnapshotDao) {

    suspend fun saveState(
        slotName: String,
        vfs: Map<String, VirtualFsNode>,
        memoryMap: Map<Long, ByteArray>,
        registers: CpuRegisters,
        totalCycles: Long,
        lastOpMnemonic: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Serialize VFS to JSON
            val vfsArray = JSONArray()
            vfs.values.forEach { node ->
                val obj = JSONObject().apply {
                    put("path", node.path)
                    put("name", node.name)
                    put("type", node.type.name)
                    put("permissions", node.permissions)
                    put("owner", node.owner)
                    put("group", node.group)
                    put("sizeBytes", node.sizeBytes)
                    put("content", node.content)
                }
                vfsArray.put(obj)
            }

            // Serialize Memory Map (address -> hex bytes)
            val memObj = JSONObject()
            memoryMap.forEach { (addr, bytes) ->
                val hexStr = bytes.joinToString("") { String.format("%02X", it) }
                memObj.put(addr.toString(), hexStr)
            }

            val entity = OsSnapshotEntity(
                slotName = slotName,
                timestamp = System.currentTimeMillis(),
                totalCycles = totalCycles,
                lastOpMnemonic = lastOpMnemonic,
                eax = registers.eax,
                ebx = registers.ebx,
                ecx = registers.ecx,
                edx = registers.edx,
                esp = registers.esp,
                ebp = registers.ebp,
                eip = registers.eip,
                eflags = registers.eflags,
                cr0 = registers.cr0,
                cr3 = registers.cr3,
                cs = registers.cs,
                ds = registers.ds,
                serializedVfs = vfsArray.toString(),
                serializedMemory = memObj.toString()
            )

            dao.saveSnapshot(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadState(slotName: String): Result<Pair<Map<String, VirtualFsNode>, Pair<Map<Long, ByteArray>, Pair<CpuRegisters, Long>>>> = withContext(Dispatchers.IO) {
        try {
            val entity = dao.getSnapshot(slotName)
                ?: return@withContext Result.failure(NoSuchElementException("Snapshot slot '$slotName' not found"))

            // Parse VFS
            val restoredVfs = mutableMapOf<String, VirtualFsNode>()
            if (entity.serializedVfs.isNotEmpty()) {
                val array = JSONArray(entity.serializedVfs)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val path = obj.getString("path")
                    val name = obj.getString("name")
                    val type = FsNodeType.valueOf(obj.getString("type"))
                    val permissions = obj.getString("permissions")
                    val owner = obj.optString("owner", "root")
                    val group = obj.optString("group", "root")
                    val sizeBytes = obj.optLong("sizeBytes", 0L)
                    val content = obj.optString("content", "")
                    restoredVfs[path] = VirtualFsNode(path, name, type, permissions, owner, group, sizeBytes, content)
                }
            }

            // Parse Memory Map
            val restoredMem = mutableMapOf<Long, ByteArray>()
            if (entity.serializedMemory.isNotEmpty()) {
                val memObj = JSONObject(entity.serializedMemory)
                val keys = memObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val addr = key.toLong()
                    val hexStr = memObj.getString(key)
                    val bytes = ByteArray(hexStr.length / 2) { idx ->
                        hexStr.substring(idx * 2, idx * 2 + 2).toInt(16).toByte()
                    }
                    restoredMem[addr] = bytes
                }
            }

            val restoredRegs = CpuRegisters(
                eax = entity.eax,
                ebx = entity.ebx,
                ecx = entity.ecx,
                edx = entity.edx,
                esp = entity.esp,
                ebp = entity.ebp,
                eip = entity.eip,
                eflags = entity.eflags,
                cr0 = entity.cr0,
                cr3 = entity.cr3,
                cs = entity.cs,
                ds = entity.ds
            )

            Result.success(Pair(restoredVfs, Pair(restoredMem, Pair(restoredRegs, entity.totalCycles))))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
