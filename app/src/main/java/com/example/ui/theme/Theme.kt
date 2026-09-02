package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun getThemeColorScheme(preset: AppThemePreset, isDark: Boolean) = when (preset) {
    AppThemePreset.BLUE -> if (isDark) {
        darkColorScheme(
            primary = BluePrimaryDark,
            onPrimary = Color(0xFF00315F),
            primaryContainer = BlueContainerDark,
            onPrimaryContainer = BlueContainerLight,
            secondary = Color(0xFFB1C8E8),
            onSecondary = Color(0xFF1B3149),
            secondaryContainer = Color(0xFF263548),
            onSecondaryContainer = Color(0xFFB1C8E8),
            background = DarkBackground,
            onBackground = Color(0xFFE2E8F0),
            surface = DarkSurface,
            onSurface = Color(0xFFE2E8F0),
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF334155),
            outlineVariant = Color(0xFF1E293B)
        )
    } else {
        lightColorScheme(
            primary = BluePrimaryLight,
            onPrimary = Color.White,
            primaryContainer = BlueContainerLight,
            onPrimaryContainer = OnBlueContainerLight,
            secondary = Color(0xFF4A607A),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE2E8F0),
            onSecondaryContainer = Color(0xFF1A1C1E),
            background = LightBackground,
            onBackground = Color(0xFF1A1C1E),
            surface = LightSurface,
            onSurface = Color(0xFF1A1C1E),
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = Color(0xFF64748B),
            outline = Color(0xFFCBD5E1),
            outlineVariant = Color(0xFFE2E8F0)
        )
    }

    AppThemePreset.EMERALD -> if (isDark) {
        darkColorScheme(
            primary = EmeraldPrimaryDark,
            onPrimary = Color(0xFF003825),
            primaryContainer = EmeraldContainerDark,
            onPrimaryContainer = EmeraldContainerLight,
            secondary = Color(0xFFB3CCBE),
            onSecondary = Color(0xFF1F352B),
            secondaryContainer = Color(0xFF354C41),
            onSecondaryContainer = Color(0xFFCFE9D9),
            background = DarkBackground,
            onBackground = Color(0xFFE2E8F0),
            surface = DarkSurface,
            onSurface = Color(0xFFE2E8F0),
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF334155),
            outlineVariant = Color(0xFF1E293B)
        )
    } else {
        lightColorScheme(
            primary = EmeraldPrimaryLight,
            onPrimary = Color.White,
            primaryContainer = EmeraldContainerLight,
            onPrimaryContainer = OnEmeraldContainerLight,
            secondary = Color(0xFF4C6358),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFCEE9DA),
            onSecondaryContainer = Color(0xFF092017),
            background = LightBackground,
            onBackground = Color(0xFF1A1C1E),
            surface = LightSurface,
            onSurface = Color(0xFF1A1C1E),
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = Color(0xFF64748B),
            outline = Color(0xFFCBD5E1),
            outlineVariant = Color(0xFFE2E8F0)
        )
    }

    AppThemePreset.PURPLE -> if (isDark) {
        darkColorScheme(
            primary = PurplePrimaryDark,
            onPrimary = Color(0xFF3B1E77),
            primaryContainer = PurpleContainerDark,
            onPrimaryContainer = PurpleContainerLight,
            secondary = Color(0xFFCBC2DB),
            onSecondary = Color(0xFF332D41),
            secondaryContainer = Color(0xFF4A4358),
            onSecondaryContainer = Color(0xFFE8DEF8),
            background = DarkBackground,
            onBackground = Color(0xFFE2E8F0),
            surface = DarkSurface,
            onSurface = Color(0xFFE2E8F0),
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF334155),
            outlineVariant = Color(0xFF1E293B)
        )
    } else {
        lightColorScheme(
            primary = PurplePrimaryLight,
            onPrimary = Color.White,
            primaryContainer = PurpleContainerLight,
            onPrimaryContainer = OnPurpleContainerLight,
            secondary = Color(0xFF625B71),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE8DEF8),
            onSecondaryContainer = Color(0xFF1D192B),
            background = LightBackground,
            onBackground = Color(0xFF1A1C1E),
            surface = LightSurface,
            onSurface = Color(0xFF1A1C1E),
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = Color(0xFF64748B),
            outline = Color(0xFFCBD5E1),
            outlineVariant = Color(0xFFE2E8F0)
        )
    }

    AppThemePreset.AMBER -> if (isDark) {
        darkColorScheme(
            primary = AmberPrimaryDark,
            onPrimary = Color(0xFF4A2800),
            primaryContainer = AmberContainerDark,
            onPrimaryContainer = AmberContainerLight,
            secondary = Color(0xFFDCC2A8),
            onSecondary = Color(0xFF3E2D1B),
            secondaryContainer = Color(0xFF564330),
            onSecondaryContainer = Color(0xFFF9DEB2),
            background = DarkBackground,
            onBackground = Color(0xFFE2E8F0),
            surface = DarkSurface,
            onSurface = Color(0xFFE2E8F0),
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF334155),
            outlineVariant = Color(0xFF1E293B)
        )
    } else {
        lightColorScheme(
            primary = AmberPrimaryLight,
            onPrimary = Color.White,
            primaryContainer = AmberContainerLight,
            onPrimaryContainer = OnAmberContainerLight,
            secondary = Color(0xFF705B40),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFCDDA7),
            onSecondaryContainer = Color(0xFF281804),
            background = LightBackground,
            onBackground = Color(0xFF1A1C1E),
            surface = LightSurface,
            onSurface = Color(0xFF1A1C1E),
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = Color(0xFF64748B),
            outline = Color(0xFFCBD5E1),
            outlineVariant = Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    themePreset: AppThemePreset = AppThemePreset.BLUE,
    content: @Composable () -> Unit
) {
    val colorScheme = getThemeColorScheme(themePreset, darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}


