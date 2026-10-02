package com.example.data.game.engine

import com.example.data.game.FsNodeType
import com.example.data.game.QuestsData
import com.example.data.game.VirtualFsNode

/**
 * Manages the in-memory Virtual File System (VFS) nodes, aliases configuration, and file operations.
 */
class VirtualFileSystemManager {

    val customAliases = mutableMapOf<String, String>().apply {
        put("c", "clear")
        put("b", "build")
        put("t", "test")
        put("m", "mem")
        put("l", "ls")
        put("ll", "ls -la")
        put("q", "quests")
        put("bench", "benchmark")
        put("syscall", "int 0x80")
        put("timer", "int 0x20")
        put("p", "ps")
        put("d", "dmesg")
        put("k", "kill")
    }

    val virtualFileSystem: MutableMap<String, VirtualFsNode> = mutableMapOf<String, VirtualFsNode>().apply {
        put("/", VirtualFsNode("/", "", FsNodeType.DIRECTORY, "drwxr-xr-x", "root", "root", 4096))
        put("/boot", VirtualFsNode("/boot", "boot", FsNodeType.DIRECTORY, "drwxr-xr-x", "root", "root", 4096))
        put("/kernel", VirtualFsNode("/kernel", "kernel", FsNodeType.DIRECTORY, "drwxr-xr-x", "root", "root", 4096))
        put("/mm", VirtualFsNode("/mm", "mm", FsNodeType.DIRECTORY, "drwxr-xr-x", "root", "root", 4096))
        put("/fs", VirtualFsNode("/fs", "fs", FsNodeType.DIRECTORY, "drwxr-xr-x", "root", "root", 4096))
        put("/drivers", VirtualFsNode("/drivers", "drivers", FsNodeType.DIRECTORY, "drwxr-xr-x", "root", "root", 4096))
        put("/gui", VirtualFsNode("/gui", "gui", FsNodeType.DIRECTORY, "drwxr-xr-x", "root", "root", 4096))
        put("/net", VirtualFsNode("/net", "net", FsNodeType.DIRECTORY, "drwxr-xr-x", "root", "root", 4096))
        put("/etc", VirtualFsNode("/etc", "etc", FsNodeType.DIRECTORY, "drwxr-xr-x", "root", "root", 4096))
        put("/proc", VirtualFsNode("/proc", "proc", FsNodeType.DIRECTORY, "dr-xr-xr-x", "root", "root", 0))
        put("/dev", VirtualFsNode("/dev", "dev", FsNodeType.DIRECTORY, "drwxr-xr-x", "root", "root", 4096))

        // Files inside /boot
        val q1 = QuestsData.allQuests.find { it.id == "Q1_1_MBR_MAGIC" }
        put("/boot/boot.asm", VirtualFsNode("/boot/boot.asm", "boot.asm", FsNodeType.FILE, "-rw-r--r--", "dev", "dev", q1?.defaultCode?.length?.toLong() ?: 512, q1?.defaultCode ?: "; MBR Bootloader\nBITS 16\nORG 0x7C00\n\nstart:\n    cli\n    xor ax, ax\n    mov ds, ax\n    mov es, ax\n    mov ss, ax\n    mov sp, 0x7C00\n    sti\n\n    mov si, msg\nprint_loop:\n    lodsb\n    or al, al\n    jz hang\n    mov ah, 0x0E\n    int 0x10\n    jmp print_loop\n\nhang:\n    hlt\n    jmp hang\n\nmsg db 'GenesisOS Booting...', 0\ntimes 510-($-$$) db 0\ndw 0xAA55\n"))

        val q2 = QuestsData.allQuests.find { it.id == "Q1_2_GDT_PROTECTED_MODE" }
        put("/boot/gdt.asm", VirtualFsNode("/boot/gdt.asm", "gdt.asm", FsNodeType.FILE, "-rw-r--r--", "dev", "dev", q2?.defaultCode?.length?.toLong() ?: 640, q2?.defaultCode ?: "; GDT Descriptor & 32-bit Protected Mode Switch\nBITS 16\ninit_gdt:\n    cli\n    lgdt [gdt_descriptor]\n    mov eax, cr0\n    or eax, 1\n    mov cr0, eax\n    jmp 0x08:init_pm32\n\nBITS 32\ninit_pm32:\n    mov ax, 0x10\n    mov ds, ax\n    mov es, ax\n    mov fs, ax\n    mov gs, ax\n    mov ss, ax\n    mov esp, 0x90000\n    ret\n"))

        put("/boot/grub.cfg", VirtualFsNode("/boot/grub.cfg", "grub.cfg", FsNodeType.FILE, "-rw-r--r--", "root", "root", 128, "set timeout=0\nset default=0\nmenuentry \"GenesisOS 1.0 (freestanding)\" {\n    multiboot /boot/genesis_kernel.elf\n    boot\n}\n"))

        // Files inside /kernel
        val qVga = QuestsData.allQuests.find { it.id == "Q2_1_VGA_TEXT_MODE" }
        put("/kernel/vga.c", VirtualFsNode("/kernel/vga.c", "vga.c", FsNodeType.FILE, "-rw-r--r--", "dev", "dev", qVga?.defaultCode?.length?.toLong() ?: 540, qVga?.defaultCode ?: "// VGA Text Buffer Driver 0xB8000\n#include <stdint.h>\n#define VGA_BUFFER ((volatile uint16_t*)0xB8000)\n\nvoid vga_puts(const char* str, uint8_t color) {\n    volatile uint16_t* vga = VGA_BUFFER;\n    for (int i = 0; str[i] != 0; i++) {\n        vga[i] = (uint16_t)str[i] | ((uint16_t)color << 8);\n    }\n}\n"))

        val qIdt = QuestsData.allQuests.find { it.id == "Q2_2_IDT_INTERRUPTS" }
        put("/kernel/idt.c", VirtualFsNode("/kernel/idt.c", "idt.c", FsNodeType.FILE, "-rw-r--r--", "dev", "dev", qIdt?.defaultCode?.length?.toLong() ?: 610, qIdt?.defaultCode ?: "// Interrupt Descriptor Table (IDT)\n#include <stdint.h>\n\nstruct idt_entry {\n    uint16_t base_low;\n    uint16_t sel;\n    uint8_t always0;\n    uint8_t flags;\n    uint16_t base_high;\n} __attribute__((packed));\n\nstruct idt_ptr {\n    uint16_t limit;\n    uint32_t base;\n} __attribute__((packed));\n\nstruct idt_entry idt[256];\n"))

        val qSched = QuestsData.allQuests.find { it.id == "Q4_1_ROUND_ROBIN_SCHEDULER" }
        put("/kernel/scheduler.c", VirtualFsNode("/kernel/scheduler.c", "scheduler.c", FsNodeType.FILE, "-rw-r--r--", "dev", "dev", qSched?.defaultCode?.length?.toLong() ?: 780, qSched?.defaultCode ?: "// Preemptive Round Robin Task Scheduler\n#include <stdint.h>\n\ntypedef struct task {\n    uint32_t esp;\n    uint32_t pid;\n    int state;\n    struct task* next;\n} task_t;\n\nvolatile task_t* current_task = 0;\n"))

        val qSys = QuestsData.allQuests.find { it.id == "Q7_1_SYSCALL_INT80" }
        put("/kernel/syscall.c", VirtualFsNode("/kernel/syscall.c", "syscall.c", FsNodeType.FILE, "-rw-r--r--", "dev", "dev", qSys?.defaultCode?.length?.toLong() ?: 600, qSys?.defaultCode ?: "// POSIX INT 0x80 System Call Dispatcher\n#include <stdint.h>\n\nint syscall_dispatcher(int syscall_num, int arg1, int arg2, int arg3) {\n    switch(syscall_num) {\n        case 1: /* SYS_exit */ return 0;\n        case 4: /* SYS_write */ return arg3;\n        default: return -1;\n    }\n}\n"))

        // Files inside /mm
        val qPaging = QuestsData.allQuests.find { it.id == "Q3_1_PAGING_MMU" }
        put("/mm/paging.c", VirtualFsNode("/mm/paging.c", "paging.c", FsNodeType.FILE, "-rw-r--r--", "dev", "dev", qPaging?.defaultCode?.length?.toLong() ?: 700, qPaging?.defaultCode ?: "// Two-Level Paging Architecture (x86 CR3 MMU)\n#include <stdint.h>\n\n__attribute__((aligned(4096))) uint32_t page_directory[1024];\n__attribute__((aligned(4096))) uint32_t first_page_table[1024];\n\nvoid init_paging() {\n    for(int i = 0; i < 1024; i++) {\n        first_page_table[i] = (i * 0x1000) | 3;\n        page_directory[i] = 0x00000002;\n    }\n    page_directory[0] = ((uint32_t)first_page_table) | 3;\n}\n"))

        // Files inside /fs
        val qVfs = QuestsData.allQuests.find { it.id == "Q5_1_VFS_INODES" }
        put("/fs/vfs.c", VirtualFsNode("/fs/vfs.c", "vfs.c", FsNodeType.FILE, "-rw-r--r--", "dev", "dev", qVfs?.defaultCode?.length?.toLong() ?: 680, qVfs?.defaultCode ?: "// Virtual File System (VFS)\n#include <stdint.h>\n\ntypedef struct vfs_node {\n    char name[32];\n    uint32_t flags;\n    uint32_t length;\n} vfs_node_t;\n"))

        // Files inside /drivers
        val qKey = QuestsData.allQuests.find { it.id == "Q6_1_KEYBOARD_DRIVER" }
        put("/drivers/keyboard.c", VirtualFsNode("/drivers/keyboard.c", "keyboard.c", FsNodeType.FILE, "-rw-r--r--", "dev", "dev", qKey?.defaultCode?.length?.toLong() ?: 550, qKey?.defaultCode ?: "// PS/2 Keyboard Controller Driver (Port 0x60)\n#include <stdint.h>\n\nchar keyboard_scancode_to_ascii(uint8_t scancode) {\n    static const char kbd_us[128] = {\n        0,  27, '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '-', '=', '\\b',\n        '\\t', 'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', '[', ']', '\\n',\n        0, 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';', '\'', '`', 0,\n        '\\\\', 'z', 'x', 'c', 'v', 'b', 'n', 'm', ',', '.', '/', 0, '*', 0, ' '\n    };\n    return scancode < 128 ? kbd_us[scancode] : 0;\n}\n"))

        // Files inside /gui
        val qGui = QuestsData.allQuests.find { it.id == "Q8_1_GUI_FRAMEBUFFER" }
        put("/gui/wm.c", VirtualFsNode("/gui/wm.c", "wm.c", FsNodeType.FILE, "-rw-r--r--", "dev", "dev", qGui?.defaultCode?.length?.toLong() ?: 620, qGui?.defaultCode ?: "// VBE Framebuffer Window Manager\n#include <stdint.h>\n\nvoid draw_pixel_32bpp(uint32_t* fb, int width, int x, int y, uint32_t color) {\n    fb[y * width + x] = color;\n}\n"))

        // Files inside /net
        val qNet = QuestsData.allQuests.find { it.id == "Q9_1_TCP_IP_STACK" }
        put("/net/ipv4.c", VirtualFsNode("/net/ipv4.c", "ipv4.c", FsNodeType.FILE, "-rw-r--r--", "dev", "dev", qNet?.defaultCode?.length?.toLong() ?: 590, qNet?.defaultCode ?: "// TCP/IP IPv4 Packet Parser\n#include <stdint.h>\n\nstruct ipv4_header {\n    uint8_t  ihl:4, version:4;\n    uint8_t  tos;\n    uint16_t total_length;\n    uint16_t id;\n    uint16_t fragment_offset;\n    uint8_t  ttl;\n    uint8_t  protocol;\n    uint16_t checksum;\n    uint32_t src_ip;\n    uint32_t dst_ip;\n} __attribute__((packed));\n"))

        // Files inside /etc
        put("/etc/os-release", VirtualFsNode("/etc/os-release", "os-release", FsNodeType.FILE, "-rw-r--r--", "root", "root", 180, "NAME=\"GenesisOS\"\nVERSION=\"1.0.0-LTS (Freestanding x86)\"\nID=genesis\nID_LIKE=unix\nVERSION_ID=\"1.0\"\nPRETTY_NAME=\"Genesis OS Architect Developer Environment\"\nHOME_URL=\"https://genesis-os.io\"\n"))
        put("/etc/motd", VirtualFsNode("/etc/motd", "motd", FsNodeType.FILE, "-rw-r--r--", "root", "root", 140, "===============================================================\n  Witamy w GenesisOS - Architektura Systemu Operacyjnego x86  \n===============================================================\n"))

        // Files inside /proc
        put("/proc/cpuinfo", VirtualFsNode("/proc/cpuinfo", "cpuinfo", FsNodeType.FILE, "-r--r--r--", "root", "root", 256, "processor\t: 0\nvendor_id\t: GenuineIntel\ncpu family\t: 6\nmodel\t\t: 42\nmodel name\t: Intel(R) Core(TM) i486/Pentium x86 @ 3.40GHz\ncpu MHz\t\t: 3400.000\ncache size\t: 8192 KB\nflags\t\t: fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 mmx fxsr sse sse2\n"))
        put("/proc/meminfo", VirtualFsNode("/proc/meminfo", "meminfo", FsNodeType.FILE, "-r--r--r--", "root", "root", 192, "MemTotal:\t   65536 kB\nMemFree:\t   48120 kB\nMemAvailable:\t   52400 kB\nBuffers:\t    3420 kB\nCached:\t\t    8960 kB\nActive:\t\t   12300 kB\nInactive:\t    5116 kB\n"))
        put("/proc/version", VirtualFsNode("/proc/version", "version", FsNodeType.FILE, "-r--r--r--", "root", "root", 112, "GenesisOS version 1.0.0-SMP (dev@genesis-workstation) (gcc 13.2.0, nasm 2.16.01) #1 SMP PREEMPT 2026\n"))

        // Device nodes inside /dev
        put("/dev/null", VirtualFsNode("/dev/null", "null", FsNodeType.DEVICE, "crw-rw-rw-", "root", "root", 0))
        put("/dev/tty0", VirtualFsNode("/dev/tty0", "tty0", FsNodeType.DEVICE, "crw--w----", "root", "tty", 0))
        put("/dev/ram0", VirtualFsNode("/dev/ram0", "ram0", FsNodeType.DEVICE, "brw-rw----", "root", "disk", 67108864))
    }

    init {
        syncAliasesToVfs()
    }

    fun syncAliasesToVfs() {
        val builder = StringBuilder()
        builder.append("# GenesisOS Shell Aliases Configuration (/etc/aliases.cfg)\n")
        builder.append("# Managed automatically by the 'alias' & 'unalias' builtin shell commands.\n\n")
        customAliases.toSortedMap().forEach { (k, v) ->
            builder.append("alias $k='$v'\n")
        }
        val content = builder.toString()
        virtualFileSystem["/etc/aliases.cfg"] = VirtualFsNode(
            path = "/etc/aliases.cfg",
            name = "aliases.cfg",
            type = FsNodeType.FILE,
            permissions = "-rw-r--r--",
            owner = "root",
            group = "root",
            sizeBytes = content.length.toLong(),
            content = content
        )
    }

    fun loadAliasesFromVfs() {
        val node = virtualFileSystem["/etc/aliases.cfg"] ?: return
        for (line in node.content.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#") || trimmed.isBlank()) continue
            if (trimmed.startsWith("alias ")) {
                val rest = trimmed.removePrefix("alias ").trim()
                if (rest.contains("=")) {
                    val key = rest.substringBefore("=").trim().lowercase()
                    var value = rest.substringAfter("=").trim()
                    if ((value.startsWith("'") && value.endsWith("'")) || (value.startsWith("\"") && value.endsWith("\""))) {
                        value = value.substring(1, value.length - 1)
                    }
                    if (key.isNotEmpty() && value.isNotEmpty()) {
                        customAliases[key] = value
                    }
                }
            }
        }
    }
}
