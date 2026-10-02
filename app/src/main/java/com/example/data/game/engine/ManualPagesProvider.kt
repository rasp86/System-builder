package com.example.data.game.engine

import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType

/**
 * Provides comprehensive Unix-style man pages for all GenesisOS tools and kernel interfaces.
 */
object ManualPagesProvider {

    fun getManualPage(command: String): List<TerminalLine> {
        val target = command.trim().lowercase()
        val lines = mutableListOf<TerminalLine>()

        when (target) {
            "alias", "unalias" -> {
                lines.add(TerminalLine("ALIAS(1)                       Genesis Manual Pages                       ALIAS(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    alias, unalias - twórz, wyświetlaj i usuwaj skróty poleceń powłoki z trwałą synchronizacją VFS (/etc/aliases.cfg)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    alias [NAZWA='POLECENIE'] | alias [OPCJE] | unalias <NAZWA>", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("OPCJE:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    alias                  ➔ Wyświetl wszystkie aktywne aliasy", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("    alias -r, --reload     ➔ Przeładuj aliasy z konfiguracji /etc/aliases.cfg", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("    alias -d <nazwa>       ➔ Usuń wybrany alias", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("    unalias <nazwa>        ➔ Usuń wybrany alias", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("    unalias -a, --all      ➔ Usuń wszystkie zdefiniowane aliasy", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("PRZYKŁADY:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    alias c='clear'", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("    alias ll='ls -l'", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("    alias k9='kill -9'", TerminalLineType.OUTPUT))
            }

            "clear", "cls" -> {
                lines.add(TerminalLine("CLEAR(1)                       Genesis Manual Pages                       CLEAR(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    clear, cls - wyczyść ekran i bufor linii terminala w interfejsie użytkownika", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    clear | cls", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("OPIS", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    Resetuje zawartość widoku terminala, usuwając dotychczasowe linie wyjścia z ekranu.", TerminalLineType.OUTPUT))
            }

            "kill", "pkill", "killall" -> {
                lines.add(TerminalLine("KILL(1)                        Genesis Manual Pages                        KILL(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    kill, pkill, killall - zakończ działanie procesu o podanym PID lub nazwie i zaktualizuj planistę", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    kill [-SYGNAŁ] <PID...> | pkill <NAZWA> | killall <NAZWA>", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SYGNAŁY:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    -9,  -SIGKILL  ➔ Natychmiastowe bezwarunkowe zakończenie procesu (Ring 0 force kill)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("    -15, -SIGTERM  ➔ Domyślny sygnał żądania zakończenia (graceful shutdown)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("    -2,  -SIGINT   ➔ Przerwanie programu przez użytkownika (Ctrl+C)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("    -l,  -list     ➔ Wyświetl listę sygnałów jądra", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("OCHRONA JĄDRA:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    Procesy bazowe jądra (PID 1 /sbin/init oraz PID 2 [kthreadd]) są chronione przed ubiciem.", TerminalLineType.OUTPUT))
            }

            "man" -> {
                lines.add(TerminalLine("MAN(1)                         Genesis Manual Pages                         MAN(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    man - interfejs do internetowych podręczników systemowych i dokumentacji jądra", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    man [POLECENIE]", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("OPIS", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    man formatuje i wyświetla szczegółowe strony podręcznika systemowego dla wszystkich narzędzi w toolchainie GenesisOS.", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("DOSTĘPNE STRONY PODRĘCZNIKA:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    man ps, man kill, man alias, man clear, man dmesg, man uptime, man chmod, man df, man grep, man save, man load, man benchmark, man mkdir, man rm, man touch, man tree, man ls, man cat, man theme", TerminalLineType.OUTPUT))
            }

            "uptime" -> {
                lines.add(TerminalLine("UPTIME(1)                     Genesis Manual Pages                     UPTIME(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    uptime - wyświetla czas pracy systemu operacyjnego od momentu rozruchu jądra", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    uptime", TerminalLineType.OUTPUT))
            }

            "chmod" -> {
                lines.add(TerminalLine("CHMOD(1)                      Genesis Manual Pages                      CHMOD(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    chmod - zmień prawa dostępu do pliku w wirtualnym systemie plików", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    chmod <755|644|+x|-x|+w> <ŚCIEŻKA_PLIKU> (np. chmod 755 /boot/boot.asm)", TerminalLineType.OUTPUT))
            }

            "df" -> {
                lines.add(TerminalLine("DF(1)                         Genesis Manual Pages                         DF(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    df - raport o wykorzystaniu przestrzeni dyskowej i pamięci RAM ramdisk", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    df [-h]", TerminalLineType.OUTPUT))
            }

            "grep" -> {
                lines.add(TerminalLine("GREP(1)                        Genesis Manual Pages                        GREP(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    grep - przeszukuj pliki wirtualnego systemu plików pod kątem wzorców tekstowych", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    grep [-i] [-n] <WZORZEC> [ŚCIEŻKA_PLIKU] (np. grep 'start' /boot/boot.asm)", TerminalLineType.OUTPUT))
            }

            "save" -> {
                lines.add(TerminalLine("SAVE(1)                        Genesis Manual Pages                        SAVE(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    save - utrwal bieżący stan VFS, pamięci RAM i rejestrów w bazie danych Room", TerminalLineType.OUTPUT))
            }

            "load" -> {
                lines.add(TerminalLine("LOAD(1)                        Genesis Manual Pages                        LOAD(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    load - przywróć zapisany stan VFS i procesora z lokalnej bazy Room", TerminalLineType.OUTPUT))
            }

            "benchmark" -> {
                lines.add(TerminalLine("BENCHMARK(1)                   Genesis Manual Pages                   BENCHMARK(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    benchmark - uruchom test wydajnościowy procesora, MMU, IDT i sterowników VGA", TerminalLineType.OUTPUT))
            }

            "mkdir" -> {
                lines.add(TerminalLine("MKDIR(1)                      Genesis Manual Pages                      MKDIR(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    mkdir - twórz nowe katalogi w wirtualnym systemie plików (VFS)", TerminalLineType.OUTPUT))
            }

            "rm" -> {
                lines.add(TerminalLine("RM(1)                         Genesis Manual Pages                         RM(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    rm - usuwaj pliki lub katalogi z wirtualnego systemu plików", TerminalLineType.OUTPUT))
            }

            "history" -> {
                lines.add(TerminalLine("HISTORY(1)                    Genesis Manual Pages                    HISTORY(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    history - wyświetla listę ostatnich 20 wprowadzonych poleceń terminala", TerminalLineType.OUTPUT))
            }

            "touch" -> {
                lines.add(TerminalLine("TOUCH(1)                       Genesis Manual Pages                       TOUCH(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    touch - twórz nowe puste pliki lub aktualizuj znaczniki czasu w wirtualnym systemie plików (VFS)", TerminalLineType.OUTPUT))
            }

            "tree" -> {
                lines.add(TerminalLine("TREE(1)                        Genesis Manual Pages                        TREE(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    tree - wyświetl hierarchiczne drzewo katalogów i plików wirtualnego systemu plików", TerminalLineType.OUTPUT))
            }

            "ls" -> {
                lines.add(TerminalLine("LS(1)                          Genesis Manual Pages                          LS(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    ls - wylistuj zawartość katalogów wirtualnego systemu plików (VFS)", TerminalLineType.OUTPUT))
            }

            "cat" -> {
                lines.add(TerminalLine("CAT(1)                         Genesis Manual Pages                         CAT(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    cat - wyświetl zawartość pliku tekstowego z wirtualnego systemu plików", TerminalLineType.OUTPUT))
            }

            "ps" -> {
                lines.add(TerminalLine("PS(1)                          Genesis Manual Pages                          PS(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    ps - raport o stanie aktywnych procesów i planisty zadań jądra (PID, STAT, %CPU, %MEM, RSS)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    ps [aux|-ef|-a]", TerminalLineType.OUTPUT))
            }

            "dmesg" -> {
                lines.add(TerminalLine("DMESG(1)                       Genesis Manual Pages                       DMESG(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    dmesg - zrzut i kontrola bufora cyklicznego komunikatów jądra (kernel ring buffer)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    dmesg [-k|-c|-H]", TerminalLineType.OUTPUT))
            }

            "theme", "colorscheme", "scheme" -> {
                lines.add(TerminalLine("THEME(1)                       Genesis Manual Pages                       THEME(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    theme - zmiana schematu kolorystycznego terminala (Retro Green, Classic Amber, Monochrome, Cyberpunk Matrix, Solarized Dark)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    theme [green|amber|mono|cyber|solarized]", TerminalLineType.OUTPUT))
            }

            else -> {
                lines.add(TerminalLine("Brak wpisu podręcznika systemowego dla '$target'.", TerminalLineType.WARNING))
                lines.add(TerminalLine("Wpisz 'man' aby wyświetlić listę wszystkich dostępnych stron dokumentacji.", TerminalLineType.OUTPUT))
            }
        }

        return lines
    }
}
