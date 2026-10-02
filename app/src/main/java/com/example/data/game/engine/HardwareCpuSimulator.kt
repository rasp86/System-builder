package com.example.data.game.engine

import com.example.data.game.CpuHardwareState
import com.example.data.game.CpuRegisters
import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType

/**
 * Handles physical memory mapping, x86 opcode assembly, registers, and cycle calculations.
 */
class HardwareCpuSimulator(
    private val dmesg: DmesgRingBuffer
) {
    var cpuHardwareState = CpuHardwareState()
    var cpuRegisters = CpuRegisters()

    // Memory simulation map (address -> byte array)
    val memoryMap = mutableMapOf<Long, ByteArray>().apply {
        // MBR 0x7C00 (512B bootloader sector snippet)
        put(0x7C00L, byteArrayOf(0x31.toByte(), 0xC0.toByte(), 0x8E.toByte(), 0xD8.toByte(), 0x8E.toByte(), 0xC0.toByte(), 0x8E.toByte(), 0xD0.toByte(), 0xBC.toByte(), 0x00.toByte(), 0x7C.toByte(), 0x00.toByte(), 0x55.toByte(), 0xAA.toByte(), 0x90.toByte(), 0x90.toByte()))
        // Page Directory 0x9C000
        put(0x9C000L, byteArrayOf(0x00.toByte(), 0x10.toByte(), 0x09.toByte(), 0x00.toByte(), 0x03.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x02.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()))
        // VGA Text Buffer 0xB8000 ("GenesisOS")
        put(0xB8000L, byteArrayOf(0x47.toByte(), 0x0A.toByte(), 0x65.toByte(), 0x0A.toByte(), 0x6E.toByte(), 0x0A.toByte(), 0x65.toByte(), 0x0A.toByte(), 0x73.toByte(), 0x0A.toByte(), 0x69.toByte(), 0x0A.toByte(), 0x73.toByte(), 0x0A.toByte(), 0x4F.toByte(), 0x0F.toByte()))
        // Kernel Entry 0x100000 (ELF Magic & entry code)
        put(0x100000L, byteArrayOf(0x7F.toByte(), 0x45.toByte(), 0x4C.toByte(), 0x46.toByte(), 0x01.toByte(), 0x01.toByte(), 0x01.toByte(), 0x00.toByte(), 0xB8.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x90.toByte(), 0x90.toByte(), 0x90.toByte()))
    }

    fun setMemoryByte(address: Long, byteOffset: Int, value: Byte) {
        val bytes = memoryMap.getOrPut(address) { ByteArray(16) { 0 } }
        if (byteOffset in bytes.indices) {
            bytes[byteOffset] = value
            dmesg.log(
                "mem/alloc",
                "OK",
                "Byte modified at 0x${java.lang.Long.toHexString(address).uppercase()}[+$byteOffset] -> 0x${String.format("%02X", value)}"
            )
        }
    }

    fun assembleInstruction(mnemonic: String): Triple<ByteArray, String, Int> {
        val clean = mnemonic.trim().lowercase()
        return when {
            clean == "nop" -> Triple(byteArrayOf(0x90.toByte()), "NOP (No Operation)", 1)
            clean == "cli" -> Triple(byteArrayOf(0xFA.toByte()), "CLI (Clear Interrupt Flag)", 4)
            clean == "sti" -> Triple(byteArrayOf(0xFB.toByte()), "STI (Set Interrupt Flag)", 4)
            clean == "hlt" -> Triple(byteArrayOf(0xF4.toByte()), "HLT (Halt CPU)", 20)
            clean == "cld" -> Triple(byteArrayOf(0xFC.toByte()), "CLD (Clear Direction Flag)", 2)
            clean == "std" -> Triple(byteArrayOf(0xFD.toByte()), "STD (Set Direction Flag)", 2)
            clean == "pushad" -> Triple(byteArrayOf(0x60.toByte()), "PUSHAD (Push All General Registers)", 18)
            clean == "popad" -> Triple(byteArrayOf(0x61.toByte()), "POPAD (Pop All General Registers)", 18)
            clean == "pushfd" -> Triple(byteArrayOf(0x9C.toByte()), "PUSHFD (Push EFLAGS)", 4)
            clean == "popfd" -> Triple(byteArrayOf(0x9D.toByte()), "POPFD (Pop EFLAGS)", 4)
            clean == "iret" -> Triple(byteArrayOf(0xCF.toByte()), "IRET (Interrupt Return)", 24)
            clean == "ret" -> Triple(byteArrayOf(0xC3.toByte()), "RET (Near Return)", 4)
            clean == "xor eax, eax" || clean == "xor eax,eax" -> Triple(byteArrayOf(0x31.toByte(), 0xC0.toByte()), "XOR EAX, EAX (Zero EAX)", 1)
            clean == "xor ebx, ebx" || clean == "xor ebx,ebx" -> Triple(byteArrayOf(0x31.toByte(), 0xDB.toByte()), "XOR EBX, EBX (Zero EBX)", 1)
            clean == "xor ecx, ecx" || clean == "xor ecx,ecx" -> Triple(byteArrayOf(0x31.toByte(), 0xC9.toByte()), "XOR ECX, ECX (Zero ECX)", 1)
            clean == "xor edx, edx" || clean == "xor edx,edx" -> Triple(byteArrayOf(0x31.toByte(), 0xD2.toByte()), "XOR EDX, EDX (Zero EDX)", 1)
            clean == "mov cr0, eax" || clean == "mov cr0,eax" -> Triple(byteArrayOf(0x0F.toByte(), 0x22.toByte(), 0xC0.toByte()), "MOV CR0, EAX (Update Control Register 0)", 16)
            clean == "mov eax, cr0" || clean == "mov eax,cr0" -> Triple(byteArrayOf(0x0F.toByte(), 0x20.toByte(), 0xC0.toByte()), "MOV EAX, CR0 (Read Control Register 0)", 4)
            clean == "mov cr3, eax" || clean == "mov cr3,eax" -> Triple(byteArrayOf(0x0F.toByte(), 0x22.toByte(), 0xD8.toByte()), "MOV CR3, EAX (Set Page Directory Pointer)", 22)
            clean == "mov eax, cr3" || clean == "mov eax,cr3" -> Triple(byteArrayOf(0x0F.toByte(), 0x20.toByte(), 0xD8.toByte()), "MOV EAX, CR3 (Read Page Directory Pointer)", 4)
            clean == "jmp $" -> Triple(byteArrayOf(0xEB.toByte(), 0xFE.toByte()), "JMP $ (Infinite Hang Loop)", 3)

            clean.startsWith("int ") -> {
                val numStr = clean.removePrefix("int ").trim()
                val num = try { java.lang.Long.decode(numStr).toInt() } catch (e: Exception) { 0x10 }
                Triple(byteArrayOf(0xCD.toByte(), num.toByte()), "INT 0x${Integer.toHexString(num).uppercase()} (Software Interrupt)", 35)
            }

            clean.startsWith("dw ") -> {
                val valStr = clean.removePrefix("dw ").trim()
                val v = try { java.lang.Long.decode(valStr).toInt() } catch (e: Exception) { 0xAA55 }
                val low = (v and 0xFF).toByte()
                val high = ((v shr 8) and 0xFF).toByte()
                Triple(byteArrayOf(low, high), "DW 0x${Integer.toHexString(v).uppercase()} (Define Word)", 2)
            }

            clean.startsWith("db ") -> {
                val valStr = clean.removePrefix("db ").trim()
                val v = try { java.lang.Long.decode(valStr).toInt() } catch (e: Exception) { 0 }
                Triple(byteArrayOf((v and 0xFF).toByte()), "DB 0x${Integer.toHexString(v).uppercase()} (Define Byte)", 1)
            }

            clean.startsWith("mov eax,") || clean.startsWith("mov eax ,") -> {
                val valStr = clean.substringAfter("mov eax,").trim().removePrefix("0x")
                val v = try { valStr.toLong(16) } catch (e: Exception) { 0L }
                val b0 = (v and 0xFF).toByte()
                val b1 = ((v shr 8) and 0xFF).toByte()
                val b2 = ((v shr 16) and 0xFF).toByte()
                val b3 = ((v shr 24) and 0xFF).toByte()
                cpuRegisters = cpuRegisters.copy(eax = "0x" + v.toString(16).uppercase().padStart(8, '0'))
                Triple(byteArrayOf(0xB8.toByte(), b0, b1, b2, b3), "MOV EAX, 0x${v.toString(16).uppercase()}", 1)
            }

            clean.startsWith("mov ebx,") || clean.startsWith("mov ebx ,") -> {
                val valStr = clean.substringAfter("mov ebx,").trim().removePrefix("0x")
                val v = try { valStr.toLong(16) } catch (e: Exception) { 0L }
                val b0 = (v and 0xFF).toByte()
                val b1 = ((v shr 8) and 0xFF).toByte()
                val b2 = ((v shr 16) and 0xFF).toByte()
                val b3 = ((v shr 24) and 0xFF).toByte()
                cpuRegisters = cpuRegisters.copy(ebx = "0x" + v.toString(16).uppercase().padStart(8, '0'))
                Triple(byteArrayOf(0xBB.toByte(), b0, b1, b2, b3), "MOV EBX, 0x${v.toString(16).uppercase()}", 1)
            }

            clean.startsWith("mov esp,") || clean.startsWith("mov esp ,") -> {
                val valStr = clean.substringAfter("mov esp,").trim().removePrefix("0x")
                val v = try { valStr.toLong(16) } catch (e: Exception) { 0x90000L }
                val b0 = (v and 0xFF).toByte()
                val b1 = ((v shr 8) and 0xFF).toByte()
                val b2 = ((v shr 16) and 0xFF).toByte()
                val b3 = ((v shr 24) and 0xFF).toByte()
                cpuRegisters = cpuRegisters.copy(esp = "0x" + v.toString(16).uppercase().padStart(8, '0'))
                Triple(byteArrayOf(0xBC.toByte(), b0, b1, b2, b3), "MOV ESP, 0x${v.toString(16).uppercase()}", 1)
            }

            else -> {
                Triple(byteArrayOf(0x90.toByte()), "GENERIC x86 OP: $mnemonic (1 cycle)", 1)
            }
        }
    }
}
