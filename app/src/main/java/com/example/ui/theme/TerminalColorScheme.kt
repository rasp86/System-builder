package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class TerminalThemeId(val key: String, val displayName: String, val subtitle: String) {
    CYBER_MATRIX("CYBER_MATRIX", "Cyberpunk Matrix", "Neon cyan, purple & dark slate"),
    RETRO_GREEN("RETRO_GREEN", "Retro Green (CRT P1)", "Klasyczny zielony kineskop phosphor P1"),
    CLASSIC_AMBER("CLASSIC_AMBER", "Classic Amber (VT220)", "Ciepły bursztynowy monitor Unix VT220"),
    MONOCHROME("MONOCHROME", "Monochrome (VT100)", "Czysta biel i czerń terminala VT100"),
    SOLARIZED_DARK("SOLARIZED_DARK", "Solarized Dark", "Klasyczna paleta programistyczna Ethan Schoonover")
}

data class TerminalThemePalette(
    val id: TerminalThemeId,
    val name: String,
    val backgroundColor: Color,
    val windowHeaderColor: Color,
    val terminalBoxBg: Color,
    val borderColor: Color,
    val inputPromptColor: Color,
    val defaultTextColor: Color,
    val successColor: Color,
    val errorColor: Color,
    val warningColor: Color,
    val headerColor: Color,
    val systemColor: Color,
    val dmesgColor: Color,
    val cursorColor: Color,
    val chipBgColor: Color,
    val chipBorderColor: Color,
    val chipTextColor: Color
)

object TerminalThemes {

    val CyberMatrix = TerminalThemePalette(
        id = TerminalThemeId.CYBER_MATRIX,
        name = "Cyberpunk Matrix",
        backgroundColor = Color(0xFF050811),
        windowHeaderColor = Color(0xFF0D1527),
        terminalBoxBg = Color(0xFF060913),
        borderColor = Color(0xFF1E3A5F),
        inputPromptColor = Color(0xFF38BDF8),
        defaultTextColor = Color(0xFFF3F4F6),
        successColor = Color(0xFF4ADE80),
        errorColor = Color(0xFFF43F5E),
        warningColor = Color(0xFFFBBF24),
        headerColor = Color(0xFFA855F7),
        systemColor = Color(0xFF93C5FD),
        dmesgColor = Color(0xFF8BA2C4),
        cursorColor = Color(0xFF38BDF8),
        chipBgColor = Color(0xFF131D31),
        chipBorderColor = Color(0xFF1E3A5F),
        chipTextColor = Color(0xFF38BDF8)
    )

    val RetroGreen = TerminalThemePalette(
        id = TerminalThemeId.RETRO_GREEN,
        name = "Retro Green (CRT P1)",
        backgroundColor = Color(0xFF020E04),
        windowHeaderColor = Color(0xFF061A09),
        terminalBoxBg = Color(0xFF031205),
        borderColor = Color(0xFF1B5E20),
        inputPromptColor = Color(0xFF39FF14),
        defaultTextColor = Color(0xFF4ADE80),
        successColor = Color(0xFF22C55E),
        errorColor = Color(0xFFFF5252),
        warningColor = Color(0xFFFFEE58),
        headerColor = Color(0xFF69F0AE),
        systemColor = Color(0xFF81C784),
        dmesgColor = Color(0xFFA5D6A7),
        cursorColor = Color(0xFF39FF14),
        chipBgColor = Color(0xFF0A2B0E),
        chipBorderColor = Color(0xFF2E7D32),
        chipTextColor = Color(0xFF4ADE80)
    )

    val ClassicAmber = TerminalThemePalette(
        id = TerminalThemeId.CLASSIC_AMBER,
        name = "Classic Amber (VT220)",
        backgroundColor = Color(0xFF100801),
        windowHeaderColor = Color(0xFF221203),
        terminalBoxBg = Color(0xFF140B02),
        borderColor = Color(0xFF78350F),
        inputPromptColor = Color(0xFFFFB000),
        defaultTextColor = Color(0xFFFDE68A),
        successColor = Color(0xFFF59E0B),
        errorColor = Color(0xFFEF4444),
        warningColor = Color(0xFFFDE047),
        headerColor = Color(0xFFFBBF24),
        systemColor = Color(0xFFFCD34D),
        dmesgColor = Color(0xFFFEF3C7),
        cursorColor = Color(0xFFFFB000),
        chipBgColor = Color(0xFF2B1804),
        chipBorderColor = Color(0xFF92400E),
        chipTextColor = Color(0xFFFBBF24)
    )

    val Monochrome = TerminalThemePalette(
        id = TerminalThemeId.MONOCHROME,
        name = "Monochrome (VT100)",
        backgroundColor = Color(0xFF08080A),
        windowHeaderColor = Color(0xFF141418),
        terminalBoxBg = Color(0xFF0B0B0E),
        borderColor = Color(0xFF3F3F46),
        inputPromptColor = Color(0xFFFAFAFA),
        defaultTextColor = Color(0xFFE4E4E7),
        successColor = Color(0xFFF4F4F5),
        errorColor = Color(0xFFFDA4AF),
        warningColor = Color(0xFFFEF08A),
        headerColor = Color(0xFFFFFFFF),
        systemColor = Color(0xFFA1A1AA),
        dmesgColor = Color(0xFFD4D4D8),
        cursorColor = Color(0xFFFFFFFF),
        chipBgColor = Color(0xFF1C1C22),
        chipBorderColor = Color(0xFF52525B),
        chipTextColor = Color(0xFFF4F4F5)
    )

    val SolarizedDark = TerminalThemePalette(
        id = TerminalThemeId.SOLARIZED_DARK,
        name = "Solarized Dark",
        backgroundColor = Color(0xFF00212B),
        windowHeaderColor = Color(0xFF073642),
        terminalBoxBg = Color(0xFF002B36),
        borderColor = Color(0xFF586E75),
        inputPromptColor = Color(0xFF2AA198),
        defaultTextColor = Color(0xFF93A1A1),
        successColor = Color(0xFF859900),
        errorColor = Color(0xFFDC322F),
        warningColor = Color(0xFFB58900),
        headerColor = Color(0xFF6C71C4),
        systemColor = Color(0xFF268BD2),
        dmesgColor = Color(0xFF839496),
        cursorColor = Color(0xFF2AA198),
        chipBgColor = Color(0xFF073642),
        chipBorderColor = Color(0xFF2AA198),
        chipTextColor = Color(0xFF2AA198)
    )

    val allPalettes: List<TerminalThemePalette> = listOf(
        CyberMatrix,
        RetroGreen,
        ClassicAmber,
        Monochrome,
        SolarizedDark
    )

    fun getPaletteById(idName: String?): TerminalThemePalette {
        return allPalettes.find { it.id.name.equals(idName, ignoreCase = true) || it.id.key.equals(idName, ignoreCase = true) }
            ?: CyberMatrix
    }
}
