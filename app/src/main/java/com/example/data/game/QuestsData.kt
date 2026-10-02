package com.example.data.game

object QuestsData {

    val allQuests: List<Quest> = listOf(
        // ==========================================
        // PHASE 1: BOOTLOADER & REAL MODE
        // ==========================================
        Quest(
            id = "Q1_1_MBR_MAGIC",
            phase = 1,
            phaseName = "Faza 1: Bootloader & Bare Metal",
            title = "Sektor Startowy i Sygnatura 0xAA55",
            category = "Bootloader ASM",
            storyPrompt = "Witaj w projekcie GenesisOS! Jako początkujący inżynier jądra musisz stworzyć Master Boot Record (MBR). BIOS po uruchomieniu szuka dokładnie 512 bajtów zakończonych magiczną sygnaturą 0xAA55. Bez tego komputer odmówi startu!",
            conceptExplanation = "BIOS ładuje pierwszy sektor (sektor 0) dysku pod adres pamięci 0x7C00. Sektor musi mieć dokładnie 512 bajtów, a ostatnie 2 bajty (offset 510 i 511) muszą zawierać słowo 0xAA55 (czyli bajty 0x55, 0xAA w formacie Little-Endian).",
            taskInstructions = "Napisz kod assemblera x86 dla sektora rozruchowego. Wypisz komunikat powitalny przez przerwanie BIOS `int 0x10`, dopełnij sektor zerami do 510 bajtów dyrektywą `times 510 - ($ - $$) db 0` i zdefiniuj słowo `dw 0xAA55`.",
            filePath = "/boot/boot.asm",
            defaultCode = """; GenesisOS Bootloader v0.1
[BITS 16]
[ORG 0x7C00]

start:
    ; Wyczyść rejestry segmentowe
    xor ax, ax
    mov ds, ax
    mov es, ax
    mov ss, ax
    mov sp, 0x7C00

    ; Wypisz powitanie w BIOS Teletype (int 0x10, AH=0x0E)
    mov si, msg_welcome
print_loop:
    lodsb
    or al, al
    jz halt
    mov ah, 0x0E
    int 0x10
    jmp print_loop

halt:
    cli
    hlt
    jmp halt

msg_welcome: db 'GenesisOS Bootloader Initializing...', 13, 10, 0

; TODO: Dopełnij sektor do 510 bajtów i dodaj magiczną sygnaturę bootowalności BIOS
times 510 - ($ - $$) db 0
; wpisz tutaj dw 0xAA55
""",
            referenceSolution = """; GenesisOS Bootloader v0.1
[BITS 16]
[ORG 0x7C00]

start:
    xor ax, ax
    mov ds, ax
    mov es, ax
    mov ss, ax
    mov sp, 0x7C00

    mov si, msg_welcome
print_loop:
    lodsb
    or al, al
    jz halt
    mov ah, 0x0E
    int 0x10
    jmp print_loop

halt:
    cli
    hlt
    jmp halt

msg_welcome: db 'GenesisOS Bootloader Initializing...', 13, 10, 0

times 510 - ($ - $$) db 0
dw 0xAA55
""",
            testCases = listOf(
                TestCase("Sygnatura MBR 0xAA55 obecna na końcu sektora") { code ->
                    val hasMagic = code.contains("dw 0xAA55", ignoreCase = true) || code.contains("0xAA55", ignoreCase = true)
                    if (hasMagic) true to "Znaleziono prawidłową sygnaturę MBR (0xAA55)."
                    else false to "Błąd: Brak instrukcji 'dw 0xAA55' definiującej sygnaturę bootowalną."
                },
                TestCase("Dopełnienie sektora do 512 bajtów") { code ->
                    val hasPadding = code.contains("times 510", ignoreCase = true) || code.contains("times", ignoreCase = true)
                    if (hasPadding) true to "Dopełnienie paddingu MBR (512 bajtów) poprawne."
                    else false to "Brak dyrektywy 'times 510 - ($ - $$) db 0'."
                },
                TestCase("Inicjalizacja segmentów i stosu SP") { code ->
                    val hasStack = code.contains("0x7C00", ignoreCase = true)
                    if (hasStack) true to "Stos i segmenty poprawnie skonfigurowane w 0x7C00."
                    else false to "Pamiętaj o ustawieniu adresu bazowego ORG 0x7C00."
                }
            ),
            xpReward = 150,
            bitsReward = 80,
            unlockedPerk = "BIOS MBR Boot Validated"
        ),

        Quest(
            id = "Q1_2_GDT_PROTECTED_MODE",
            phase = 1,
            phaseName = "Faza 1: Bootloader & Bare Metal",
            title = "Global Descriptor Table & Tryb 32-bit",
            category = "Bootloader ASM",
            storyPrompt = "Świetnie! Komputer ładuje bootloader, ale jesteś w 16-bitowym Real Mode z limitem 1MB pamięci. Aby zbudować nowoczesny system z dostępem do 4GB RAM, musimy skonfigurować tablicę GDT i przełączyć procesor w 32-bitowy Protected Mode!",
            conceptExplanation = "GDT (Global Descriptor Table) definiuje segmenty pamięci (Kod i Dane jądra). Po załadowaniu rejestru GDTR instrukcją `lgdt [gdt_descriptor]`, ustawiamy bit 0 (PE - Protection Enable) w rejestrze kontrolnym CR0 i wykonujemy daleki skok `jmp CODE_SEG:init_pm` aby wyczyścić kolejkę potoku CPU.",
            taskInstructions = "Zaimplementuj deskryptor GDT (Null, Kernel Code, Kernel Data), załaduj go przez `lgdt` i ustaw bit PE w rejestrze `CR0` (poprzez `mov eax, cr0; or eax, 1; mov cr0, eax`).",
            filePath = "/boot/gdt.asm",
            defaultCode = """; Przejście do 32-bit Protected Mode
[BITS 16]

switch_to_pm:
    cli                     ; 1. Wyłącz przerwania
    ; TODO: Załaduj tablicę GDT za pomocą instrukcji lgdt
    
    ; TODO: Ustaw bit Protection Enable (PE, bit 0) w rejestrze CR0
    mov eax, cr0
    ; ... twój kod tutaj ...
    mov cr0, eax

    ; 3. Daleki skok do 32-bitowego segmentu kodu (wymusza odświeżenie potoku)
    jmp 0x08:init_32bit_pm

[BITS 32]
init_32bit_pm:
    mov ax, 0x10            ; Ustaw rejestry segmentowe na deskryptor danych (0x10)
    mov ds, ax
    mov ss, ax
    mov es, ax
    mov fs, ax
    mov gs, ax
    mov ebp, 0x90000        ; Ustaw 32-bitowy stos
    mov esp, ebp
    call kernel_entry
    jmp $
""",
            referenceSolution = """; Przejście do 32-bit Protected Mode
[BITS 16]

switch_to_pm:
    cli
    lgdt [gdt_descriptor]
    mov eax, cr0
    or eax, 1
    mov cr0, eax
    jmp 0x08:init_32bit_pm

[BITS 32]
init_32bit_pm:
    mov ax, 0x10
    mov ds, ax
    mov ss, ax
    mov es, ax
    mov fs, ax
    mov gs, ax
    mov ebp, 0x90000
    mov esp, ebp
    call kernel_entry
    jmp $
""",
            testCases = listOf(
                TestCase("Ładowanie deskryptora GDT (lgdt)") { code ->
                    if (code.contains("lgdt", ignoreCase = true)) true to "Poprawne wywołanie 'lgdt [gdt_descriptor]'."
                    else false to "Brak instrukcji 'lgdt'."
                },
                TestCase("Ustawienie bitu PE w CR0") { code ->
                    val setsPE = (code.contains("or eax, 1") || code.contains("or al, 1") || code.contains("or eax, 0x1")) && code.contains("mov cr0, eax")
                    if (setsPE) true to "Bit PE (Protected Mode Enable) w rejestrze CR0 ustawiony pomyślnie."
                    else false to "Błąd: Należy ustawić bit 0 w CR0 za pomocą 'or eax, 1' i zapisać z powrotem do 'cr0'."
                },
                TestCase("Segmenty Protected Mode [BITS 32]") { code ->
                    if (code.contains("[BITS 32]", ignoreCase = true) && code.contains("0x90000")) true to "Poprawna konfiguracja 32-bitowego stosu pod 0x90000."
                    else false to "Brak konfiguracji 32-bitowego stosu."
                }
            ),
            xpReward = 200,
            bitsReward = 120,
            unlockedPerk = "32-bit Protected Mode (4GB Address Space)"
        ),

        // ==========================================
        // PHASE 2: KERNEL ENTRY & INTERRUPTS (IDT)
        // ==========================================
        Quest(
            id = "Q2_1_VGA_TERMINAL",
            phase = 2,
            phaseName = "Faza 2: Jądro & Przerwania IDT",
            title = "Sterownik Ekranu Tekstowego VGA (0xB8000)",
            category = "Kernel C",
            storyPrompt = "Witamy w języku C! Jesteśmy w trybie 32-bitowym. Pierwszym zadaniem jądra jest wypisanie tekstu bezpośrednio do bufora wideo karty VGA pod adresem 0xB8000.",
            conceptExplanation = "Karta VGA w trybie tekstowym 80x25 mapuje pamięć od 0xB8000. Każdy znak na ekranie zajmuje 2 bajty: bajt 0 to kod ASCII, a bajt 1 to atrybut koloru (np. 0x0F = biały tekst na czarnym tle, 0x0A = zielony jak w Matrixie!).",
            taskInstructions = "Uzupełnij funkcję `vga_putchar` oraz `vga_print` w języku C. Zapisuj znak i atrybut koloru pod adresem `VGA_BUFFER + offset`.",
            filePath = "/kernel/vga.c",
            defaultCode = """// Sterownik bufora VGA GenesisOS (80x25)
#include <stdint.h>

#define VGA_BUFFER (uint16_t*)0xB8000
#define VGA_WIDTH 80
#define VGA_HEIGHT 25
#define COLOR_WHITE_ON_BLACK 0x0F
#define COLOR_GREEN_ON_BLACK 0x0A

static uint16_t cursor_x = 0;
static uint16_t cursor_y = 0;

void vga_clear(void) {
    uint16_t blank = (COLOR_WHITE_ON_BLACK << 8) | ' ';
    for (int i = 0; i < VGA_WIDTH * VGA_HEIGHT; i++) {
        VGA_BUFFER[i] = blank;
    }
    cursor_x = 0;
    cursor_y = 0;
}

void vga_putchar(char c, uint8_t color) {
    if (c == '\n') {
        cursor_x = 0;
        cursor_y++;
        return;
    }
    
    // TODO: Oblicz pozycję w buforze (cursor_y * VGA_WIDTH + cursor_x)
    // Zapisz do VGA_BUFFER wpis złożony z (color << 8) | c
    // Zwiększ cursor_x, a jeśli osiągnie VGA_WIDTH, zawiń linię
    
    uint16_t offset = cursor_y * VGA_WIDTH + cursor_x;
    VGA_BUFFER[offset] = (color << 8) | (uint8_t)c;
    
    cursor_x++;
    if (cursor_x >= VGA_WIDTH) {
        cursor_x = 0;
        cursor_y++;
    }
}

void vga_print(const char* str, uint8_t color) {
    // TODO: Przeiteruj po ciągu znaków i wywołaj vga_putchar
    while (*str) {
        vga_putchar(*str++, color);
    }
}
""",
            referenceSolution = """// Sterownik bufora VGA GenesisOS (80x25)
#include <stdint.h>

#define VGA_BUFFER (uint16_t*)0xB8000
#define VGA_WIDTH 80
#define VGA_HEIGHT 25
#define COLOR_WHITE_ON_BLACK 0x0F
#define COLOR_GREEN_ON_BLACK 0x0A

static uint16_t cursor_x = 0;
static uint16_t cursor_y = 0;

void vga_clear(void) {
    uint16_t blank = (COLOR_WHITE_ON_BLACK << 8) | ' ';
    for (int i = 0; i < VGA_WIDTH * VGA_HEIGHT; i++) {
        VGA_BUFFER[i] = blank;
    }
    cursor_x = 0;
    cursor_y = 0;
}

void vga_putchar(char c, uint8_t color) {
    if (c == '\n') {
        cursor_x = 0;
        cursor_y++;
        return;
    }
    
    uint16_t offset = cursor_y * VGA_WIDTH + cursor_x;
    VGA_BUFFER[offset] = (color << 8) | (uint8_t)c;
    
    cursor_x++;
    if (cursor_x >= VGA_WIDTH) {
        cursor_x = 0;
        cursor_y++;
    }
}

void vga_print(const char* str, uint8_t color) {
    while (*str) {
        vga_putchar(*str++, color);
    }
}
""",
            testCases = listOf(
                TestCase("Mapowanie bufora VGA na 0xB8000") { code ->
                    if (code.contains("0xB8000")) true to "Prawidłowy adres bazowy pamięci VGA 0xB8000."
                    else false to "Błąd: Brak definicji adresu 0xB8000."
                },
                TestCase("Format wpisu znaku: (color << 8) | char") { code ->
                    if (code.contains("color << 8") || code.contains("(color << 8)")) true to "Prawidłowa konstrukcja 16-bitowego wpisu VGA (atrybut + ASCII)."
                    else false to "Niepoprawne łączenie koloru i znaku."
                },
                TestCase("Obsługa nowej linii '\\n' oraz zawijania wierszy") { code ->
                    if (code.contains("\\n") && code.contains("cursor_y")) true to "Prawidłowa obsługa kursora i znaków nowej linii."
                    else false to "Brak obsługi znaku nowej linii."
                }
            ),
            xpReward = 220,
            bitsReward = 140,
            unlockedPerk = "VGA 80x25 Video Output"
        ),

        Quest(
            id = "Q2_2_IDT_INTERRUPTS",
            phase = 2,
            phaseName = "Faza 2: Jądro & Przerwania IDT",
            title = "Tablica Przerwań IDT & Kontroler PIC",
            category = "Kernel Core",
            storyPrompt = "Bez przerwań procesor nie wie, kiedy użytkownik nacisnął klawisz lub kiedy upłynął kwant czasu zegara sprzętowego! Musimy zbudować Tablicę Deskryptorów Przerwań (IDT) i przeprogramować układ PIC 8259.",
            conceptExplanation = "IDT (Interrupt Descriptor Table) zawiera 256 wpisów po 8 bajtów, wskazujących na funkcje ISR (Interrupt Service Routines). Wyjątki procesora to 0-31 (np. dzielenie przez zero to 0, Page Fault to 14), a przerwania sprzętowe IRQ (klawiatura, timer) remapujemy na numery 32-47 za pomocą portów I/O układu PIC.",
            taskInstructions = "Zaimplementuj funkcję `idt_set_gate`, która wpisuje adres handlera ISR, selektor segmentu kodu (0x08), flagi uprawnień (0x8E = obecna, Ring 0) i wywołuje instrukcję `lidt`.",
            filePath = "/kernel/idt.c",
            defaultCode = """// Tablica deskryptorów przerwań (IDT)
#include <stdint.h>

struct idt_entry {
    uint16_t base_low;      // Młodsze 16 bitów adresu ISR
    uint16_t sel;            // Selektor segmentu jądra (0x08)
    uint8_t  always0;        // Zawsze 0
    uint8_t  flags;          // Flagi: Present, Ring 0, Interrupt Gate (0x8E)
    uint16_t base_high;     // Starsze 16 bitów adresu ISR
} __attribute__((packed));

struct idt_ptr {
    uint16_t limit;
    uint32_t base;
} __attribute__((packed));

struct idt_entry idt[256];
struct idt_ptr idtp;

void idt_set_gate(uint8_t num, uint32_t base, uint16_t sel, uint8_t flags) {
    // TODO: Rozbij 32-bitowy adres 'base' na base_low i base_high
    idt[num].base_low = (base & 0xFFFF);
    idt[num].base_high = (base >> 16) & 0xFFFF;
    idt[num].sel = sel;
    idt[num].always0 = 0;
    idt[num].flags = flags;
}

void idt_init(void) {
    idtp.limit = (sizeof(struct idt_entry) * 256) - 1;
    idtp.base = (uint32_t)&idt;

    // Załaduj IDT do rejestru IDTR
    __asm__ __volatile__("lidt (%0)" : : "r" (&idtp));
}
""",
            referenceSolution = """// Tablica deskryptorów przerwań (IDT)
#include <stdint.h>

struct idt_entry {
    uint16_t base_low;
    uint16_t sel;
    uint8_t  always0;
    uint8_t  flags;
    uint16_t base_high;
} __attribute__((packed));

struct idt_ptr {
    uint16_t limit;
    uint32_t base;
} __attribute__((packed));

struct idt_entry idt[256];
struct idt_ptr idtp;

void idt_set_gate(uint8_t num, uint32_t base, uint16_t sel, uint8_t flags) {
    idt[num].base_low = (base & 0xFFFF);
    idt[num].base_high = (base >> 16) & 0xFFFF;
    idt[num].sel = sel;
    idt[num].always0 = 0;
    idt[num].flags = flags;
}

void idt_init(void) {
    idtp.limit = (sizeof(struct idt_entry) * 256) - 1;
    idtp.base = (uint32_t)&idt;

    __asm__ __volatile__("lidt (%0)" : : "r" (&idtp));
}
""",
            testCases = listOf(
                TestCase("Podział adresu 32-bit na base_low i base_high") { code ->
                    if (code.contains("0xFFFF") && code.contains(">> 16")) true to "Prawidłowe maskowanie i przesunięcie adresu bazowego ISR."
                    else false to "Błąd w rozbijaniu adresu ISR na 16-bitowe części."
                },
                TestCase("Instrukcja lidt (%0) ładowania IDTR") { code ->
                    if (code.contains("lidt", ignoreCase = true)) true to "Rejestr procesora IDTR poprawnie zainicjalizowany przez 'lidt'."
                    else false to "Brak asemblerowej instrukcji 'lidt'."
                }
            ),
            xpReward = 260,
            bitsReward = 160,
            unlockedPerk = "Interrupt Subsystem (PIC & IDT Active)"
        ),

        // ==========================================
        // PHASE 3: MEMORY MANAGEMENT & PAGING
        // ==========================================
        Quest(
            id = "Q3_1_PAGING_MMU",
            phase = 3,
            phaseName = "Faza 3: Pamięć Wirtualna & Stronicowanie",
            title = "Katalog Stron i Mapowanie Tożsamościowe",
            category = "Memory Subsystem",
            storyPrompt = "Teraz kluczowy krok architektury: Stronicowanie (Paging)! Bez pamięci wirtualnej programy mogłyby nadpisywać pamięć jądra. Skonfigurujmy dwupoziomowe tablice stron x86 (Page Directory & Page Tables).",
            conceptExplanation = "Procesor x86 używa stron o rozmiarze 4KB (4096 bajtów). Katalog stron (Page Directory) zawiera 1024 wpisy, z których każdy wskazuje na Tablicę Stron (Page Table) z kolejnymi 1024 wpisami. Wpis zawiera adres ramki oraz flagi: bit 0 (Present), bit 1 (Read/Write), bit 2 (User/Supervisor). Włączamy stronicowanie ustawiając rejestr CR3 na adres Page Directory i zapalając bit 31 (PG) w CR0!",
            taskInstructions = "Zaimplementuj identity mapping dla pierwszych 4MB pamięci (od 0x00000000 do 0x003FFFFF) z flagami 0x3 (Present | Writable), załaduj adres katalogu do rejestru CR3 i włącz bit PG w CR0.",
            filePath = "/mm/paging.c",
            defaultCode = """// Moduł stronicowania pamięci (Paging x86)
#include <stdint.h>

#define PAGE_PRESENT  0x1
#define PAGE_WRITABLE 0x2
#define PAGE_USER     0x4

// Wyrównanie do granicy 4096 bajtów
__attribute__((aligned(4096))) uint32_t page_directory[1024];
__attribute__((aligned(4096))) uint32_t first_page_table[1024];

void paging_init(void) {
    // 1. Wypełnij pierwszą tablicę stron (mapuje pierwsze 4MB pamięci)
    for (int i = 0; i < 1024; i++) {
        // Każda strona ma rozmiar 4096 (i * 0x1000)
        // TODO: Ustaw adres fizyczny z flagami (PAGE_PRESENT | PAGE_WRITABLE)
        first_page_table[i] = (i * 0x1000) | (PAGE_PRESENT | PAGE_WRITABLE);
    }

    // 2. Pierwszy wpis w katalogu stron wskazuje na first_page_table
    page_directory[0] = ((uint32_t)first_page_table) | (PAGE_PRESENT | PAGE_WRITABLE);

    // Wyzeruj pozostałe 1023 wpisy katalogu stron
    for (int i = 1; i < 1024; i++) {
        page_directory[i] = 0x00000002; // Not present, but writable
    }

    // 3. Załaduj CR3 adresem page_directory
    uint32_t pd_addr = (uint32_t)page_directory;
    __asm__ __volatile__("mov %0, %%cr3" : : "r"(pd_addr));

    // 4. Włącz stronicowanie (bit 31 PG w CR0)
    uint32_t cr0;
    __asm__ __volatile__("mov %%cr0, %0" : "=r"(cr0));
    cr0 |= 0x80000000; // Bit 31 = Paging Enable
    __asm__ __volatile__("mov %0, %%cr0" : : "r"(cr0));
}
""",
            referenceSolution = """// Moduł stronicowania pamięci (Paging x86)
#include <stdint.h>

#define PAGE_PRESENT  0x1
#define PAGE_WRITABLE 0x2
#define PAGE_USER     0x4

__attribute__((aligned(4096))) uint32_t page_directory[1024];
__attribute__((aligned(4096))) uint32_t first_page_table[1024];

void paging_init(void) {
    for (int i = 0; i < 1024; i++) {
        first_page_table[i] = (i * 0x1000) | (PAGE_PRESENT | PAGE_WRITABLE);
    }

    page_directory[0] = ((uint32_t)first_page_table) | (PAGE_PRESENT | PAGE_WRITABLE);

    for (int i = 1; i < 1024; i++) {
        page_directory[i] = 0x00000002;
    }

    uint32_t pd_addr = (uint32_t)page_directory;
    __asm__ __volatile__("mov %0, %%cr3" : : "r"(pd_addr));

    uint32_t cr0;
    __asm__ __volatile__("mov %%cr0, %0" : "=r"(cr0));
    cr0 |= 0x80000000;
    __asm__ __volatile__("mov %0, %%cr0" : : "r"(cr0));
}
""",
            testCases = listOf(
                TestCase("Wyrównanie tablic stron do granicy 4096 bajtów") { code ->
                    if (code.contains("4096") || code.contains("aligned")) true to "Tablice stron poprawnie wyrównane do 4KB."
                    else false to "Brak atrybutu aligned(4096)."
                },
                TestCase("Ładowanie rejestru bazowego CR3") { code ->
                    if (code.contains("cr3", ignoreCase = true)) true to "Rejestr CR3 załadowany adresem Page Directory."
                    else false to "Brak instrukcji zapisu do rejestru CR3."
                },
                TestCase("Zapalenie bitu PG (0x80000000) w rejestrze CR0") { code ->
                    if (code.contains("0x80000000") && code.contains("cr0", ignoreCase = true)) true to "Bit stronicowania PG w CR0 poprawnie uaktywniony!"
                    else false to "Brak włączenia bitu 31 w rejestrze CR0."
                }
            ),
            xpReward = 300,
            bitsReward = 180,
            unlockedPerk = "MMU Virtual Paging (4KB Page Protection)"
        ),

        // ==========================================
        // PHASE 4: MULTITASKING & SCHEDULER
        // ==========================================
        Quest(
            id = "Q4_1_ROUND_ROBIN_SCHEDULER",
            phase = 4,
            phaseName = "Faza 4: Wielozadaniowość & Scheduler",
            title = "Planista Zadań (Round-Robin) & Blok PCB",
            category = "Process Scheduler",
            storyPrompt = "Twój system ma już pamięć wirtualną i przerwania! Czas na serce nowoczesnego OS: wielozadaniowość (Multitasking). Każdy proces ma swój blok kontrolny (PCB) i stos. Zbudujmy algorytm Round-Robin.",
            conceptExplanation = "Przy każdym tyknięciu zegara sprzętowego (PIT IRQ 0) scheduler zapisuje stan rejestrów aktualnego procesu, przechodzi cyklicznie do kolejnego procesu w kolejce gotowości (Ready Queue) i ładuje jego wskaźnik stosu ESP (Context Switch).",
            taskInstructions = "Napisz funkcję `schedule()`, która wybiera następny proces ze statusem `PROCESS_READY`, aktualizuje stan `current_process` i zwraca nowy wskaźnik stosu.",
            filePath = "/kernel/scheduler.c",
            defaultCode = """// Round-Robin Task Scheduler GenesisOS
#include <stdint.h>

#define MAX_PROCESSES 16
#define PROCESS_READY 1
#define PROCESS_RUNNING 2
#define PROCESS_BLOCKED 3

typedef struct {
    uint32_t esp;           // Wskaźnik stosu procesu
    uint32_t pid;           // ID procesu
    char name[32];          // Nazwa procesu
    int state;              // Stan procesu
    uint32_t time_slice;    // Pozostały kwant czasu
} process_t;

process_t process_table[MAX_PROCESSES];
int current_proc_idx = 0;
int total_processes = 0;

uint32_t schedule(uint32_t current_esp) {
    if (total_processes <= 1) {
        return current_esp;
    }

    // 1. Zapisz obecny stos bieżącego procesu
    process_table[current_proc_idx].esp = current_esp;
    if (process_table[current_proc_idx].state == PROCESS_RUNNING) {
        process_table[current_proc_idx].state = PROCESS_READY;
    }

    // 2. Znajdź następny proces w pętli cyklicznej Round-Robin
    int next_idx = (current_proc_idx + 1) % total_processes;
    while (process_table[next_idx].state != PROCESS_READY) {
        next_idx = (next_idx + 1) % total_processes;
    }

    current_proc_idx = next_idx;
    process_table[current_proc_idx].state = PROCESS_RUNNING;

    // 3. Zwróć nowy wskaźnik stosu do przełączenia kontekstu w ASM
    return process_table[current_proc_idx].esp;
}
""",
            referenceSolution = """// Round-Robin Task Scheduler GenesisOS
#include <stdint.h>

#define MAX_PROCESSES 16
#define PROCESS_READY 1
#define PROCESS_RUNNING 2
#define PROCESS_BLOCKED 3

typedef struct {
    uint32_t esp;
    uint32_t pid;
    char name[32];
    int state;
    uint32_t time_slice;
} process_t;

process_t process_table[MAX_PROCESSES];
int current_proc_idx = 0;
int total_processes = 0;

uint32_t schedule(uint32_t current_esp) {
    if (total_processes <= 1) {
        return current_esp;
    }

    process_table[current_proc_idx].esp = current_esp;
    if (process_table[current_proc_idx].state == PROCESS_RUNNING) {
        process_table[current_proc_idx].state = PROCESS_READY;
    }

    int next_idx = (current_proc_idx + 1) % total_processes;
    while (process_table[next_idx].state != PROCESS_READY) {
        next_idx = (next_idx + 1) % total_processes;
    }

    current_proc_idx = next_idx;
    process_table[current_proc_idx].state = PROCESS_RUNNING;

    return process_table[current_proc_idx].esp;
}
""",
            testCases = listOf(
                TestCase("Zapisywanie wskaźnika stosu ESP") { code ->
                    if (code.contains(".esp = current_esp")) true to "Wskaźnik stosu poprzedniego procesu prawidłowo zachowany w PCB."
                    else false to "Błąd: Należy zapisać current_esp do bloku procesu."
                },
                TestCase("Cykliczny wybór następnego PID (Round-Robin modulo)") { code ->
                    if (code.contains("% total_processes") || code.contains("% MAX_PROCESSES")) true to "Algorytm karuzelowy Round-Robin zaimplementowany poprawnie."
                    else false to "Brak cyklicznego przeszukiwania tablicy procesów."
                },
                TestCase("Aktualizacja stanu na PROCESS_RUNNING") { code ->
                    if (code.contains("PROCESS_RUNNING")) true to "Status nowego wątku ustawiony na RUNNING."
                    else false to "Brak aktualizacji stanu procesu."
                }
            ),
            xpReward = 350,
            bitsReward = 200,
            unlockedPerk = "Preemptive Multitasking & Task Switcher"
        ),

        // ==========================================
        // PHASE 5: VFS & FILE SYSTEM
        // ==========================================
        Quest(
            id = "Q5_1_VFS_INODES",
            phase = 5,
            phaseName = "Faza 5: System Plików VFS",
            title = "Wirtualny System Plików (VFS) & Inody",
            category = "Storage & VFS",
            storyPrompt = "Programy muszą mieć możliwość zapisywania i odczytywania plików! Stworzymy abstrakcję VFS (Virtual File System) ze wskaźnikami funkcyjnymi `read`, `write`, `open`, `close` wzorowaną na architekturze Uniksa.",
            conceptExplanation = "W architekturze VFS każdy plik, katalog, a nawet urządzenie (np. `/dev/tty`) jest reprezentowane przez strukturę `vfs_node_t` ze wskaźnikami do operacji wejścia/wyjścia (I/O).",
            taskInstructions = "Zaimplementuj funkcję `vfs_read`, która sprawdza obecność wskaźnika funkcji `read` w węźle i wywołuje odczyt danych do bufora.",
            filePath = "/fs/vfs.c",
            defaultCode = """// Virtual File System (VFS) GenesisOS
#include <stdint.h>
#include <stddef.h>

#define FS_FILE        0x01
#define FS_DIRECTORY   0x02
#define FS_CHARDEVICE  0x03

struct vfs_node;

typedef uint32_t (*read_type_t)(struct vfs_node*, uint32_t offset, uint32_t size, uint8_t* buffer);
typedef uint32_t (*write_type_t)(struct vfs_node*, uint32_t offset, uint32_t size, uint8_t* buffer);

typedef struct vfs_node {
    char name[128];
    uint32_t mask;
    uint32_t uid;
    uint32_t gid;
    uint32_t flags;
    uint32_t length;
    read_type_t read;
    write_type_t write;
} vfs_node_t;

vfs_node_t* fs_root = NULL;

uint32_t vfs_read(vfs_node_t* node, uint32_t offset, uint32_t size, uint8_t* buffer) {
    if (node == NULL || buffer == NULL) {
        return 0;
    }
    
    // TODO: Jeśli node posiada wskaźnik 'read', wywołaj go i zwróć liczbę odczytanych bajtów
    if (node->read != NULL) {
        return node->read(node, offset, size, buffer);
    }
    
    return 0;
}
""",
            referenceSolution = """// Virtual File System (VFS) GenesisOS
#include <stdint.h>
#include <stddef.h>

#define FS_FILE        0x01
#define FS_DIRECTORY   0x02
#define FS_CHARDEVICE  0x03

struct vfs_node;

typedef uint32_t (*read_type_t)(struct vfs_node*, uint32_t offset, uint32_t size, uint8_t* buffer);
typedef uint32_t (*write_type_t)(struct vfs_node*, uint32_t offset, uint32_t size, uint8_t* buffer);

typedef struct vfs_node {
    char name[128];
    uint32_t mask;
    uint32_t uid;
    uint32_t gid;
    uint32_t flags;
    uint32_t length;
    read_type_t read;
    write_type_t write;
} vfs_node_t;

vfs_node_t* fs_root = NULL;

uint32_t vfs_read(vfs_node_t* node, uint32_t offset, uint32_t size, uint8_t* buffer) {
    if (node == NULL || buffer == NULL) {
        return 0;
    }
    
    if (node->read != NULL) {
        return node->read(node, offset, size, buffer);
    }
    
    return 0;
}
""",
            testCases = listOf(
                TestCase("Walidacja wskaźnika node->read") { code ->
                    if (code.contains("node->read != NULL") || code.contains("node->read != 0") || code.contains("node->read")) true to "Zabezpieczenie przed NULL pointerem poprawne."
                    else false to "Błąd: Należy sprawdzić, czy wskaźnik read nie jest nullem."
                },
                TestCase("Delegacja wywołania I/O do sterownika") { code ->
                    if (code.contains("node->read(node, offset, size, buffer)")) true to "Prawidłowe przekazanie parametrów do sterownika systemu plików."
                    else false to "Niepoprawne wywołanie funkcji odczytu."
                }
            ),
            xpReward = 400,
            bitsReward = 220,
            unlockedPerk = "POSIX VFS Layer & Ramdisk Storage"
        ),

        // ==========================================
        // PHASE 6: DRIVERS & KEYBOARD INPUT
        // ==========================================
        Quest(
            id = "Q6_1_PS2_KEYBOARD",
            phase = 6,
            phaseName = "Faza 6: Sterowniki Urządzeń I/O",
            title = "Sterownik Klawiatury PS/2 & Scancodes",
            category = "Device Drivers",
            storyPrompt = "Czas na interakcję ze światem! Kiedy wciskasz klawisz, kontroler klawiatury 8042 generuje przerwanie IRQ 1 i wysyła scancode na port wejścia/wyjścia 0x60. Przetłumaczmy scancodes na kody ASCII.",
            conceptExplanation = "Port I/O 0x60 zwraca kody bajtowe klawiatury (np. 0x1E = 'A', 0x30 = 'B', 0x1C = Enter). Kody powyżej 0x80 oznaczają puszczenie klawisza (Key Release = Scancode | 0x80).",
            taskInstructions = "Zaimplementuj funkcję `keyboard_handler`, która czyta scancode z portu 0x60 za pomocą `inb(0x60)` i dopisuje przetłumaczony znak ASCII do bufora klawiatury.",
            filePath = "/drivers/keyboard.c",
            defaultCode = """// Sterownik klawiatury PS/2 GenesisOS
#include <stdint.h>

#define KEYBOARD_DATA_PORT 0x60

static inline uint8_t inb(uint16_t port) {
    uint8_t ret;
    __asm__ __volatile__("inb %1, %0" : "=a"(ret) : "Nd"(port));
    return ret;
}

// Tablica mapowania Scancode Set 1 do ASCII
const char scancode_to_ascii[128] = {
    0,  27, '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '-', '=', '\b',
    '\t', 'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', '[', ']', '\n',
    0, /* Ctrl */
    'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';', '\'', '`',
    0, /* Left Shift */
    '\\', 'z', 'x', 'c', 'v', 'b', 'n', 'm', ',', '.', '/',
    0, /* Right Shift */
    '*', 0, /* Alt */ ' ', 0 /* CapsLock */
};

char key_buffer[256];
int key_head = 0;

void keyboard_handler(void) {
    // 1. Odczytaj scancode z portu 0x60
    uint8_t scancode = inb(KEYBOARD_DATA_PORT);

    // 2. Ignoruj puszczenie klawisza (bit 7 jest zapalony)
    if (scancode & 0x80) {
        return;
    }

    // 3. Przetłumacz na ASCII
    char ascii = scancode_to_ascii[scancode];
    if (ascii != 0) {
        key_buffer[key_head % 256] = ascii;
        key_head++;
    }
}
""",
            referenceSolution = """// Sterownik klawiatury PS/2 GenesisOS
#include <stdint.h>

#define KEYBOARD_DATA_PORT 0x60

static inline uint8_t inb(uint16_t port) {
    uint8_t ret;
    __asm__ __volatile__("inb %1, %0" : "=a"(ret) : "Nd"(port));
    return ret;
}

const char scancode_to_ascii[128] = {
    0,  27, '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '-', '=', '\b',
    '\t', 'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', '[', ']', '\n',
    0,
    'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';', '\'', '`',
    0,
    '\\', 'z', 'x', 'c', 'v', 'b', 'n', 'm', ',', '.', '/',
    0,
    '*', 0, ' ', 0
};

char key_buffer[256];
int key_head = 0;

void keyboard_handler(void) {
    uint8_t scancode = inb(KEYBOARD_DATA_PORT);

    if (scancode & 0x80) {
        return;
    }

    char ascii = scancode_to_ascii[scancode];
    if (ascii != 0) {
        key_buffer[key_head % 256] = ascii;
        key_head++;
    }
}
""",
            testCases = listOf(
                TestCase("Odczyt z portu 0x60 (inb)") { code ->
                    if (code.contains("inb(KEYBOARD_DATA_PORT)") || code.contains("inb(0x60)")) true to "Prawidłowy odczyt rejestru kontrolera klawiatury przez inb."
                    else false to "Brak odczytu z portu 0x60."
                },
                TestCase("Filtrowanie zdarzeń Key Release (scancode & 0x80)") { code ->
                    if (code.contains("& 0x80")) true to "Zdarzenia zwolnienia klawisza poprawnie ignorowane."
                    else false to "Brak sprawdzania bitu 0x80."
                },
                TestCase("Buforowanie znaków w kołowym buforze klawiatury") { code ->
                    if (code.contains("key_buffer")) true to "Bufor wejściowy FIFO zaimplementowany."
                    else false to "Brak zapisu do key_buffer."
                }
            ),
            xpReward = 450,
            bitsReward = 250,
            unlockedPerk = "PS/2 Keyboard & Interactive Typing"
        ),

        // ==========================================
        // PHASE 7: USERLAND SHELL & SYSCALLS
        // ==========================================
        Quest(
            id = "Q7_1_SYSCALLS_AND_SHELL",
            phase = 7,
            phaseName = "Faza 7: Przestrzeń Użytkownika & Shell",
            title = "Wywołania Systemowe (Syscalls int 0x80)",
            category = "Userland & Syscalls",
            storyPrompt = "Aplikacje w Ring 3 (User Space) nie mogą bezpośrednio dotykać sprzętu! Muszą prosić jądro o pomoc poprzez wywołanie systemowe (Syscall). W architekturze x86 używamy programowego przerwania `int 0x80`.",
            conceptExplanation = "Rejestr EAX przekazuje numer syscalla (np. 1 = `sys_exit`, 2 = `sys_fork`, 3 = `sys_read`, 4 = `sys_write`), a rejestry EBX, ECX, EDX przekazują parametry funkcji.",
            taskInstructions = "Zaimplementuj dyspozytora syscalli `syscall_handler(registers_t* regs)` obsługującego `SYS_WRITE` i `SYS_EXIT`.",
            filePath = "/kernel/syscall.c",
            defaultCode = """// Dyspozytor Syscalli GenesisOS (int 0x80)
#include <stdint.h>

#define SYS_EXIT  1
#define SYS_FORK  2
#define SYS_READ  3
#define SYS_WRITE 4

typedef struct {
    uint32_t ds;
    uint32_t edi, esi, ebp, esp, ebx, edx, ecx, eax; // PUSHAD
    uint32_t int_no, err_code;
    uint32_t eip, cs, eflags, useresp, ss;           // IRET
} registers_t;

int sys_write(int fd, const char* buf, int count);
void sys_exit(int status);

void syscall_handler(registers_t* regs) {
    // Numer syscalla znajduje się w rejestrze EAX
    uint32_t syscall_num = regs->eax;

    switch (syscall_num) {
        case SYS_WRITE: {
            int fd = (int)regs->ebx;
            const char* buf = (const char*)regs->ecx;
            int count = (int)regs->edx;
            regs->eax = sys_write(fd, buf, count);
            break;
        }
        case SYS_EXIT: {
            int status = (int)regs->ebx;
            sys_exit(status);
            break;
        }
        default:
            regs->eax = -1; // Nieznany syscall (ENOSYS)
            break;
    }
}
""",
            referenceSolution = """// Dyspozytor Syscalli GenesisOS (int 0x80)
#include <stdint.h>

#define SYS_EXIT  1
#define SYS_FORK  2
#define SYS_READ  3
#define SYS_WRITE 4

typedef struct {
    uint32_t ds;
    uint32_t edi, esi, ebp, esp, ebx, edx, ecx, eax;
    uint32_t int_no, err_code;
    uint32_t eip, cs, eflags, useresp, ss;
} registers_t;

int sys_write(int fd, const char* buf, int count);
void sys_exit(int status);

void syscall_handler(registers_t* regs) {
    uint32_t syscall_num = regs->eax;

    switch (syscall_num) {
        case SYS_WRITE: {
            int fd = (int)regs->ebx;
            const char* buf = (const char*)regs->ecx;
            int count = (int)regs->edx;
            regs->eax = sys_write(fd, buf, count);
            break;
        }
        case SYS_EXIT: {
            int status = (int)regs->ebx;
            sys_exit(status);
            break;
        }
        default:
            regs->eax = -1;
            break;
    }
}
""",
            testCases = listOf(
                TestCase("Obsługa numeru syscalla w EAX") { code ->
                    if (code.contains("regs->eax")) true to "Poprawne odczytanie numeru operacji z rejestru EAX."
                    else false to "Brak odczytu z regs->eax."
                },
                TestCase("Przekazywanie argumentów w EBX, ECX, EDX") { code ->
                    if (code.contains("regs->ebx") && code.contains("regs->ecx") && code.contains("regs->edx")) true to "Standard ABI wywołań systemowych x86 zachowany."
                    else false to "Błąd w mapowaniu parametrów z rejestrów."
                }
            ),
            xpReward = 500,
            bitsReward = 300,
            unlockedPerk = "POSIX Syscall Dispatcher (int 0x80)"
        ),

        // ==========================================
        // PHASE 8: WINDOW MANAGER & GRAPHICAL GUI
        // ==========================================
        Quest(
            id = "Q8_1_GUI_FRAMEBUFFER",
            phase = 8,
            phaseName = "Faza 8: Serwer Okien & Pulpit GUI",
            title = "Kompzytor Okien & Linear Framebuffer (VBE)",
            category = "GUI & Graphics",
            storyPrompt = "Czas na rewolucję wizualną! Porzucamy tryb tekstowy na rzecz pełnego trybu graficznego VBE 800x600 32-bit RGB. Twój system otrzymuje nowoczesny interfejs okienkowy z myszą i oknami aplikacji!",
            conceptExplanation = "Karta graficzna w trybie VESA/VBE udostępnia ciągły bufor ramki (Linear Framebuffer). Każdy piksel to 4 bajty: Red, Green, Blue, Alpha (0x00RRGGBB). Kompozytor okien rysuje tło pulpitu, ramki okien, paski tytułowe i kursor myszy techniką podwójnego buforowania (Double Buffering)!",
            taskInstructions = "Zaimplementuj funkcję `gui_draw_rect` oraz `gui_draw_window`, które rysują cieniowane okno z paskiem tytułowym.",
            filePath = "/gui/wm.c",
            defaultCode = """// GenesisOS Window Compositor (VESA Framebuffer)
#include <stdint.h>

#define SCREEN_WIDTH 800
#define SCREEN_HEIGHT 600

uint32_t* framebuffer = (uint32_t*)0xE0000000; // VBE LFB Address

void gui_putpixel(int x, int y, uint32_t color) {
    if (x >= 0 && x < SCREEN_WIDTH && y >= 0 && y < SCREEN_HEIGHT) {
        framebuffer[y * SCREEN_WIDTH + x] = color;
    }
}

void gui_draw_rect(int x, int y, int w, int h, uint32_t color) {
    for (int j = y; j < y + h; j++) {
        for (int i = x; i < x + w; i++) {
            gui_putpixel(i, j, color);
        }
    }
}

void gui_draw_window(int x, int y, int w, int h, const char* title, uint32_t body_color) {
    // 1. Rysuj cień okna
    gui_draw_rect(x + 4, y + 4, w, h, 0x00111827);
    
    // 2. Rysuj tło okna
    gui_draw_rect(x, y, w, h, body_color);
    
    // 3. Rysuj pasek tytułowy (wysokość 24px)
    gui_draw_rect(x, y, w, 24, 0x001F2937);
    
    // 4. Rysuj przycisk zamknięcia 'X' (czerwony kwadrat)
    gui_draw_rect(x + w - 20, y + 4, 16, 16, 0x00EF4444);
}
""",
            referenceSolution = """// GenesisOS Window Compositor (VESA Framebuffer)
#include <stdint.h>

#define SCREEN_WIDTH 800
#define SCREEN_HEIGHT 600

uint32_t* framebuffer = (uint32_t*)0xE0000000;

void gui_putpixel(int x, int y, uint32_t color) {
    if (x >= 0 && x < SCREEN_WIDTH && y >= 0 && y < SCREEN_HEIGHT) {
        framebuffer[y * SCREEN_WIDTH + x] = color;
    }
}

void gui_draw_rect(int x, int y, int w, int h, uint32_t color) {
    for (int j = y; j < y + h; j++) {
        for (int i = x; i < x + w; i++) {
            gui_putpixel(i, j, color);
        }
    }
}

void gui_draw_window(int x, int y, int w, int h, const char* title, uint32_t body_color) {
    gui_draw_rect(x + 4, y + 4, w, h, 0x00111827);
    gui_draw_rect(x, y, w, h, body_color);
    gui_draw_rect(x, y, w, 24, 0x001F2937);
    gui_draw_rect(x + w - 20, y + 4, 16, 16, 0x00EF4444);
}
""",
            testCases = listOf(
                TestCase("Mapowanie piksela (y * SCREEN_WIDTH + x)") { code ->
                    if (code.contains("y * SCREEN_WIDTH + x")) true to "Poprawne adresowanie 2D w liniowym buforze ramki."
                    else false to "Błąd w formule indeksowania bufora wideo."
                },
                TestCase("Cieniowanie i kompozycja okna z paskiem tytułu") { code ->
                    if (code.contains("gui_draw_rect") && code.contains("0x00EF4444")) true to "Elementy kompozytora GUI (pasek, przycisk zamykania) wyrenderowane."
                    else false to "Brak kompozycji okna."
                }
            ),
            xpReward = 600,
            bitsReward = 350,
            unlockedPerk = "VESA 32-bit Graphical Window Manager"
        ),

        // ==========================================
        // PHASE 9: NETWORK STACK & TCP/IP
        // ==========================================
        Quest(
            id = "Q9_1_NET_STACK",
            phase = 9,
            phaseName = "Faza 9: Stos Sieciowy & Gniazda",
            title = "Karta Sieciowa RTL8139 & Pakiety IPv4/TCP",
            category = "Networking",
            storyPrompt = "Twój system łączy się ze światem! Sterownik karty Ethernet odbiera ramki, parsuje nagłówki IPv4 i protokołu TCP. Zbudujmy obsługę gniazd sieciowych (Sockets) i prosty serwer HTTP!",
            conceptExplanation = "Stos sieciowy analizuje ramkę warstwa po warstwie: Ethernet (14 bajtów MAC) -> IPv4 (20 bajtów IP) -> TCP (porty i flagi SYN/ACK).",
            taskInstructions = "Zaimplementuj sprawdzanie sumy kontrolnej nagłówka IPv4 (Internet Checksum) w funkcji `ipv4_checksum`.",
            filePath = "/net/ipv4.c",
            defaultCode = """// Stos sieciowy IPv4 GenesisOS
#include <stdint.h>

struct ipv4_header {
    uint8_t  version_ihl;       // Version (4 bits) + IHL (4 bits)
    uint8_t  tos;               // Type of service
    uint16_t total_length;      // Total packet length
    uint16_t id;                // Packet ID
    uint16_t flags_fragment;    // Flags + Fragment offset
    uint8_t  ttl;               // Time to live
    uint8_t  protocol;          // Protocol (6 = TCP, 17 = UDP, 1 = ICMP)
    uint16_t checksum;          // Header checksum
    uint32_t src_ip;            // Source IP
    uint32_t dst_ip;            // Destination IP
} __attribute__((packed));

uint16_t ipv4_checksum(void* data, int length) {
    uint16_t* word_ptr = (uint16_t*)data;
    uint32_t sum = 0;

    for (int i = 0; i < length / 2; i++) {
        sum += word_ptr[i];
    }

    // Dodaj przeniesienie (carry)
    while (sum >> 16) {
        sum = (sum & 0xFFFF) + (sum >> 16);
    }

    // Zwróć dopełnienie do 1
    return (uint16_t)(~sum);
}
""",
            referenceSolution = """// Stos sieciowy IPv4 GenesisOS
#include <stdint.h>

struct ipv4_header {
    uint8_t  version_ihl;
    uint8_t  tos;
    uint16_t total_length;
    uint16_t id;
    uint16_t flags_fragment;
    uint8_t  ttl;
    uint8_t  protocol;
    uint16_t checksum;
    uint32_t src_ip;
    uint32_t dst_ip;
} __attribute__((packed));

uint16_t ipv4_checksum(void* data, int length) {
    uint16_t* word_ptr = (uint16_t*)data;
    uint32_t sum = 0;

    for (int i = 0; i < length / 2; i++) {
        sum += word_ptr[i];
    }

    while (sum >> 16) {
        sum = (sum & 0xFFFF) + (sum >> 16);
    }

    return (uint16_t)(~sum);
}
""",
            testCases = listOf(
                TestCase("Algorytm 16-bit Internet Checksum RFC 791") { code ->
                    if (code.contains("sum += word_ptr") && code.contains("~sum")) true to "Prawidłowa implementacja algorytmu sumy kontrolnej IP."
                    else false to "Błąd w obliczaniu sumy kontrolnej."
                }
            ),
            xpReward = 700,
            bitsReward = 400,
            unlockedPerk = "Ethernet RTL8139 & TCP/IP Sockets"
        ),

        // ==========================================
        // PHASE 10: SOVEREIGN OS ECOSYSTEM & RELEASE
        // ==========================================
        Quest(
            id = "Q10_1_OS_RELEASE",
            phase = 10,
            phaseName = "Faza 10: Ekosystem & Oficjalne Wydanie v1.0",
            title = "Kompilator TCC & Wydanie GenesisOS v1.0",
            category = "OS Milestone",
            storyPrompt = "Ostatni krok! Twój system operacyjny jest w pełni samowystarczalny (Self-Hosting)! Portujemy kompilator C (Tiny C Compiler), aby GenesisOS mógł kompilować sam siebie i uruchamiać własne gry!",
            conceptExplanation = "System operacyjny uważa się za kompletny, gdy potrafi skompilować własne jądro bez pomocy systemu-gospodarza. GenesisOS osiąga status SUWERENNEGO SYSTEMU OPERACYJNEGO!",
            taskInstructions = "Wpisz manifest wydania wersji GenesisOS 1.0.0 w pliku `/etc/os-release` z pełnymi informacjami o autorze i architekturze.",
            filePath = "/etc/os-release",
            defaultCode = """# GenesisOS Release Manifest v1.0.0
NAME="GenesisOS"
VERSION="1.0.0-LTS Sovereign"
ID=genesisos
PRETTY_NAME="GenesisOS 1.0 (x86 Protected Mode Kernel)"
ARCH="i386-pc-elf"
FEATURES="Paging, RoundRobin, VFS, PS2Keyboard, VBE GUI, TCP/IP, TCC Compiler"
AUTHOR="Junior OS Architect"
STATUS="RELEASED"
""",
            referenceSolution = """# GenesisOS Release Manifest v1.0.0
NAME="GenesisOS"
VERSION="1.0.0-LTS Sovereign"
ID=genesisos
PRETTY_NAME="GenesisOS 1.0 (x86 Protected Mode Kernel)"
ARCH="i386-pc-elf"
FEATURES="Paging, RoundRobin, VFS, PS2Keyboard, VBE GUI, TCP/IP, TCC Compiler"
AUTHOR="Junior OS Architect"
STATUS="RELEASED"
""",
            testCases = listOf(
                TestCase("Manifest wydania v1.0.0") { code ->
                    if (code.contains("GenesisOS") && code.contains("1.0.0") && code.contains("RELEASED")) true to "Oficjalny certyfikat GenesisOS v1.0.0 wydany!"
                    else false to "Uzupełnij pola manifestu wydania."
                }
            ),
            xpReward = 1000,
            bitsReward = 1000,
            unlockedPerk = "Master OS Architect Badge & Self-Hosting Kernel"
        )
    )
}
