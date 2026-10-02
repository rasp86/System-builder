package com.example.data.game.engine

import com.example.data.game.DmesgEntry

/**
 * Manages the simulated Linux / GenesisOS kernel message ring buffer (/dev/kmsg / dmesg).
 */
class DmesgRingBuffer {

    val entries = mutableListOf<DmesgEntry>().apply {
        add(DmesgEntry(0.000000, "kernel", "INFO", "Linux / GenesisOS Kernel 1.0.0-SMP (x86_32-pc-elf)"))
        add(DmesgEntry(0.001420, "x86/cpu", "OK", "Intel(R) Core(TM) i486/Pentium compatible CPU found @ 3.40 GHz"))
        add(DmesgEntry(0.003180, "bios/e820", "INFO", "BIOS-e820: [mem 0x0000000000000000-0x000000000009fbff] usable"))
        add(DmesgEntry(0.004920, "bios/e820", "INFO", "BIOS-e820: [mem 0x000000000009fc00-0x000000000009ffff] reserved"))
        add(DmesgEntry(0.006500, "bios/e820", "INFO", "BIOS-e820: [mem 0x00000000000b8000-0x00000000000c0000] VGA text video buffer"))
        add(DmesgEntry(0.008910, "x86/gdt", "OK", "Global Descriptor Table (GDT) loaded at physical 0x00000800 [3 entries]"))
        add(DmesgEntry(0.012400, "x86/idt", "OK", "Interrupt Descriptor Table (IDT) loaded with 256 vector slots"))
        add(DmesgEntry(0.016830, "x86/mmu", "OK", "Page Directory initialized @ 0x0009C000. 4MB Identity Paging active (CR0.PG=1)"))
        add(DmesgEntry(0.021050, "pci/bus", "INFO", "PCI host bridge initialized. 00:01.0 VGA Display Controller"))
        add(DmesgEntry(0.025400, "vfs/root", "OK", "Mounted root VFS from initial ramdisk (/dev/ram0) on /"))
        add(DmesgEntry(0.031200, "scheduler", "OK", "Round-Robin Preemptive Task Scheduler online. PIT Timer frequency set to 100Hz"))
    }

    fun log(subsystem: String, level: String, message: String) {
        val timestamp = (System.currentTimeMillis() % 100000) / 1000.0
        entries.add(DmesgEntry(timestamp, subsystem, level, message))
    }

    fun clear() {
        entries.clear()
        log("kmsg", "OK", "Kernel ring buffer cleared by user request.")
    }

    fun getFiltered(kernelOnly: Boolean = false): List<DmesgEntry> {
        return if (kernelOnly) {
            entries.filter { it.subsystem.startsWith("x86") || it.subsystem == "kernel" || it.subsystem == "scheduler" }
        } else {
            entries.toList()
        }
    }
}
