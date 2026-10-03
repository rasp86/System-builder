# OS Architect – Architecture & System Guide

## 1. Architectural Layers & Structure

OS Architect is designed using a clean, unidirectional, layered architecture:

```
UI / Presentation Layer (Jetpack Compose & ViewModels)
                ↓
Domain Layer (Use Cases, Domain Models, Providers)
                ↓
Data Layer (Repositories, Room Database, Gemini AI Service, VFS)
                ↓
Game Engine Layer (CPU Simulator, Process Scheduler, VFS, QEMU Emulation)
```

### Layer Details:
- **Presentation Layer (`com.example.ui`)**:
  - `GameScreenViewModel`: Top-level coordinator managing global active tabs, CRT screen effects, and notifications.
  - `TerminalViewModel`: Dedicated to host shell interaction, command execution history, and console streaming.
  - `QuestViewModel`: Manages player progression, unlocked kernel milestones, XP, and bit rewards.
  - `EditorViewModel`: Manages assembly/C code buffer, unit test validation, and reference solutions.
  - `VirtualMachineViewModel`: Manages simulated BIOS/QEMU boot sequence, kernel logs, and guest CLI/GUI console.
  - `AiMentorViewModel`: Manages Ada mentor consultations, step-by-step thinking traces, and chat persistence.
  - `components/`: Modular Compose widgets (`TerminalView`, `CodeEditorView`, `AiMentorView`, `VirtualMachineView`, `CpuHardwareVisualizer`).

- **Domain Layer (`com.example.domain`)**:
  - `provider/QuestProvider`: Abstraction providing access to all modular quest definitions.
  - `usecase/`: Pure business logic interactors (`RunQuestTestsUseCase`, `CompleteQuestUseCase`, `SaveUserCodeUseCase`, `ExecuteTerminalCommandUseCase`, `AskAiMentorUseCase`, `BootVirtualMachineUseCase`).
  - `model/GameResult`: Sealed type safety for domain operations (`Success`, `Error`, `Loading`).
  - `validator/GameStateValidator`: Invariant verification for saves and quest records.

- **Data Layer (`com.example.data`)**:
  - `di/AppContainer`: Application-scoped Dependency Injection container providing lazily initialized singletons.
  - `db/AppDatabase`: Room persistence with explicit version migrations (`MIGRATION_1_2`, `MIGRATION_2_3`).
  - `ai/GeminiAiMentorService`: Secure AI integration with multi-tier fallback (Online Thinking → Online Flash → Offline Kernel Engine).

- **Game Engine Layer (`com.example.game` & `com.example.data.game`)**:
  - `terminal/TerminalCommandRegistry`: Registry pattern mapping commands to modular `TerminalCommand` classes.
  - `terminal/commands/`: Individual, isolated command handlers (`HelpCommand`, `LsCommand`, `DfCommand`, `PsCommand`, etc.).
  - `vm/BootSequenceGenerator`: Data-driven boot stage simulation.
  - `engine/HardwareCpuSimulator`: x86 register file, 64MB memory bank, and instruction interpreter.

---

## 2. How to Add a New Quest

1. **Define the Quest**:
   Add a new `Quest` object to `QuestsData.kt` with its ID, phase, description, default template, reference solution, and test cases.
2. **Implement Test Verification** (if custom validation needed):
   Add specific test assertion rules in `QuestVerificationEngine.kt`.
3. **Add Hardware & Boot Hook** (if required):
   Add a corresponding `BootStage` entry to `BootSequenceGenerator.DEFAULT_BOOT_STAGES`.

---

## 3. How to Add a New Terminal Command

1. Create a new command file in `com.example.game.terminal.commands`:
   ```kotlin
   class PingCommand : TerminalCommand {
       override val name = "ping"
       override val description = "Sends ICMP echo packets to virtual host"
       override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
           return listOf(TerminalLine("64 bytes from 127.0.0.1: icmp_seq=1 ttl=64 time=0.04 ms", TerminalLineType.OUTPUT))
       }
   }
   ```
2. Register the command in `DefaultTerminalCommandRegistry.create()`:
   ```kotlin
   registry.register(PingCommand())
   ```
3. Add a unit test in `TerminalCommandRegistryTest.kt`.
