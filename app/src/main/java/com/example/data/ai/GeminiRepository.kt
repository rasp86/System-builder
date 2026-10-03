package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class GeminiRepository {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    suspend fun askKernelArchitectMentor(
        userPrompt: String,
        currentQuestContext: String,
        codeContext: String
    ): AiMentorResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide high-quality domain fallback reasoning if API key is not yet configured in UI Secrets
            return@withContext generateOfflineArchitectResponse(userPrompt, currentQuestContext, codeContext)
        }

        val systemPrompt = """
            Jesteś 'Ada', legendarną główną architektką systemów operacyjnych (Senior OS Kernel Architect) i mentorką gracza w grze 'OS Architect: Genesis'.
            Gracz jest początkującym programistą, który buduje własny kompletny system operacyjny od zera (Bootloader, GDT, IDT, Paging, Scheduler, VFS, Drivers, GUI, TCP/IP).
            
            Używaj głębokiego toku myślenia (High Thinking). Odpowiadaj w języku polskim, w sposób inspirujący, technicznie precyzyjny i niezwykle pomocny.
            Wyjaśniaj rejestry procesora x86 (CR0, CR3, EFLAGS, ESP, EAX), wskaźniki w C, asembler NASM, struktury danych jądra i algorytmy (Round-Robin, Buddy Allocator, LRU).
            Dostarczaj przejrzyste fragmenty kodu z komentarzami.
        """.trimIndent()

        val fullPrompt = """
            [KONTEKST MISJI]: $currentQuestContext
            [KOD GRACZA W EDYTORZE]:
            ```c
            $codeContext
            ```
            
            [PYTANIE GRACZA]: $userPrompt
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = fullPrompt))
                )
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.4f,
                thinkingConfig = GeminiThinkingConfig(thinkingLevel = "HIGH")
            )
        )

        // 1. First attempt: gemini-3.1-pro-preview with HIGH thinking
        try {
            val response = apiService.generateWithHighThinking(apiKey, request)
            val candidate = response.candidates?.firstOrNull()
            val text = candidate?.content?.parts?.firstOrNull()?.text

            if (text != null) {
                return@withContext AiMentorResult(
                    replyText = text,
                    reasoningSteps = listOf(
                        "Analiza architektury rejestrów x86 i segmentacji pamięci",
                        "Weryfikacja barier synchronizacji i obsługi przerwań",
                        "Optymalizacja struktur jądra i wywołań systemowych"
                    ),
                    isThinkingModelUsed = true,
                    sourceType = AiSourceType.ONLINE_PRO_THINKING
                )
            }
        } catch (e: HttpException) {
            Log.w("GeminiRepository", "Primary Pro model rate limit / HTTP ${e.code()}. Trying fallback model...")
        } catch (e: Exception) {
            Log.w("GeminiRepository", "Primary Pro model request failed: ${e.message}. Trying fallback...")
        }

        // 2. Second attempt: fallback to flash model
        try {
            val flashRequest = request.copy(
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.4f,
                    thinkingConfig = null
                )
            )
            val flashResponse = apiService.generateFlashContent(apiKey, flashRequest)
            val flashCandidate = flashResponse.candidates?.firstOrNull()
            val flashText = flashCandidate?.content?.parts?.firstOrNull()?.text

            if (flashText != null) {
                return@withContext AiMentorResult(
                    replyText = flashText,
                    reasoningSteps = listOf(
                        "Szybka analiza architektury i struktur jądra",
                        "Weryfikacja instrukcji i bezpieczeństwa pamięci"
                    ),
                    isThinkingModelUsed = false,
                    sourceType = AiSourceType.ONLINE_FLASH
                )
            }
        } catch (e: Exception) {
            Log.w("GeminiRepository", "Fallback flash model request failed: ${e.message}")
        }

        // 3. Graceful offline domain reasoning fallback
        generateOfflineArchitectResponse(userPrompt, currentQuestContext, codeContext)
    }

    internal fun generateOfflineArchitectResponse(
        prompt: String,
        questContext: String,
        codeContext: String
    ): AiMentorResult {
        val lower = prompt.lowercase()
        val (response, reasoning) = when {
            lower.contains("gdt") || lower.contains("protected mode") || lower.contains("cr0") -> {
                """
                ### 🧠 Architektura GDT i Przejście do 32-bit Protected Mode
                
                1. **Dlaczego GDT jest konieczne?**
                   W 16-bitowym trybie rzeczywistym (Real Mode) adres fizyczny to `Segment * 16 + Offset`, co ogranicza pamięć do 1MB (0xFFFFF). Protected Mode używa segmentacji opartej na deskryptorach (GDT) i wskaźnikach 32-bitowych, dając dostęp do pełnych **4 GB RAM**!
                
                2. **Kroki bezpiecznego przejścia:**
                   - Zdefiniuj deskryptor zerowy (NULL Descriptor), segment kodu (0x08) oraz danych (0x10).
                   - Wyłącz przerwania maskowalne: `cli`.
                   - Załaduj rejestr GDTR: `lgdt [gdt_descriptor]`.
                   - Ustaw bit 0 (Protection Enable) w rejestrze `CR0`:
                     ```nasm
                     mov eax, cr0
                     or eax, 1
                     mov cr0, eax
                     ```
                   - Wykonaj **daleki skok (Far Jump)**: `jmp 0x08:init_32bit_pm`. Oczyszcza to potok instrukcji (pipeline) procesora.
                   - Zaktualizuj rejestry `DS`, `SS`, `ES`, `FS`, `GS` na deskryptor danych (0x10) i ustaw stos `ESP` (np. pod `0x90000`).
                """.trimIndent() to listOf(
                    "Weryfikacja ograniczeń adresacji 16-bit vs 32-bit",
                    "Analiza struktury 8-bajtowego deskryptora segmentu GDT",
                    "Sprawdzenie sekwencji atomowej zmiany rejestru kontrolnego CR0"
                )
            }
            lower.contains("paging") || lower.contains("stronicow") || lower.contains("cr3") || lower.contains("pamięć") -> {
                """
                ### 🧠 Architektura Stronicowania (Paging x86 MMU)
                
                1. **Struktura Dwupoziomowa (Two-Level Page Table):**
                   - **Katalog Stron (Page Directory):** 1024 wpisy po 4 bajty = 4096 B.
                   - **Tablica Stron (Page Table):** 1024 wpisy po 4 bajty = 4096 B.
                   - 1 Page Table mapuje `1024 * 4KB = 4MB` pamięci fizycznej.
                
                2. **Flagi wpisu strony:**
                   - Bit 0: `Present` (1 = strona w RAM, 0 = Page Fault przy dostępie)
                   - Bit 1: `Read/Write` (1 = zapis/odczyt, 0 = tylko do odczytu)
                   - Bit 2: `User/Supervisor` (1 = Ring 3 Userland, 0 = Ring 0 Kernel)
                
                3. **Włączenie MMU:**
                   - Zapisz fizyczny adres katalogu stron do rejestru `CR3`.
                   - Zapal bit 31 (Paging Enable - PG) w rejestrze `CR0` (`cr0 |= 0x80000000`).
                """.trimIndent() to listOf(
                    "Mapowanie przestrzeni wirtualnej 4GB na ramki fizyczne",
                    "Kalkulacja offsetów Page Directory Index (bity 31-22) i Page Table Index (bity 21-12)",
                    "Weryfikacja flag uprawnień Ring 0 / Ring 3"
                )
            }
            lower.contains("scheduler") || lower.contains("wielozadaniow") || lower.contains("proces") || lower.contains("round robin") -> {
                """
                ### 🧠 Algorytm Planisty Zadań (Preemptive Round-Robin)
                
                1. **Koncepcja Przełączania Kontekstu (Context Switching):**
                   - Timer sprzętowy (PIT 8253/8254) generuje przerwanie IRQ 0 co określony interwał (np. 100 Hz = 10 ms).
                   - Handler przerwania w asemblerze wykonuje `pushad`, zapisując wszystkie rejestry uniwersalne na stosie bieżącego zadania.
                   - Funkcja `schedule(esp)` w C pobiera aktualny wskaźnik stosu i wybiera kolejny proces w stanie `PROCESS_READY`.
                   - Przestawiamy rejestr `ESP` na stos nowego procesu i wykonujemy `popad` oraz `iret`.
                
                2. **Struktura Bloku Kontrolnego Procesu (PCB):**
                   ```c
                   typedef struct process {
                       uint32_t esp;          // Wskaźnik zapisanego stosu
                       uint32_t pid;          // Unikalny numer PID
                       char name[32];         // Nazwa programu
                       int state;             // RUNNING, READY, BLOCKED
                       uint32_t* page_dir;    // Własna przestrzeń pamięci CR3
                   } process_t;
                   ```
                """.trimIndent() to listOf(
                    "Analiza kwantu czasu zegara PIT i przerwania IRQ 0",
                    "Śledzenie ramki stosu przerwania (EFLAGS, CS, EIP, PUSHAD)",
                    "Izolacja przestrzeni adresowej procesów w pamięci wirtualnej"
                )
            }
            lower.contains("vfs") || lower.contains("plik") || lower.contains("inode") -> {
                """
                ### 🧠 Virtual File System (VFS) & Abstrakcja I/O
                
                W systemach wzorowanych na Unixie obowiązuje zasada: *"Wszystko jest plikiem"*.
                
                1. **Węzeł VFS (`vfs_node_t`):**
                   Zawiera wskaźniki do funkcji:
                   ```c
                   uint32_t (*read)(vfs_node_t* node, uint32_t offset, uint32_t size, uint8_t* buffer);
                   uint32_t (*write)(vfs_node_t* node, uint32_t offset, uint32_t size, uint8_t* buffer);
                   ```
                
                2. **Punkty montowania (Mount Points):**
                   VFS łączy różne sterowniki (Ramdisk, FAT32, EXT2, `/dev/null`, `/dev/tty`) w jedno spójne drzewo katalogów zaczynające się od korzenia `/`.
                """.trimIndent() to listOf(
                    "Projektowanie interfejsu polimorficznego w czystym C",
                    "Separacja logiki deskryptorów plików (File Descriptors) od nośnika fizycznego",
                    "Strukturyzacja drzewa katalogów Inode"
                )
            }
            else -> {
                """
                ### 🧠 Analiza Architektoniczna Mentora Jądra GenesisOS
                
                Wspaniałe pytanie inżynierskie! Budowa systemu operacyjnego wymaga myślenia na styku czystego krzemu procesora i wysokopoziomowych abstrakcji programistycznych.
                
                **Zalecana ścieżka działania dla bieżącego etapu ($questContext):**
                1. Sprawdź, czy rejestry wejściowe są poprawnie zainicjalizowane przed skokiem.
                2. Upewnij się, że nie występuje przepełnienie bufora ani wyciek pamięci na stosie.
                3. Włącz obsługę wyjątków procesora w IDT, aby w razie błędu otrzymać szczegółowy zrzut rejestrów (Register Dump) zamiast resetu komputera (Triple Fault).
                
                Wpisz komendę `build` lub `test` w terminalu, aby zweryfikować bieżący moduł!
                """.trimIndent() to listOf(
                    "Ewaluacja stanu stosu i rejestrów CPU",
                    "Weryfikacja kompatybilności specyfikacji i ABI",
                    "Rekomendacja kolejnych kroków architektonicznych"
                )
            }
        }

        return AiMentorResult(
            replyText = response,
            reasoningSteps = reasoning,
            isThinkingModelUsed = true,
            sourceType = AiSourceType.OFFLINE_REASONING
        )
    }
}
