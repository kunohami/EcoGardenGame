package com.rafarg.ecogardengame.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- LIGHT THEME (Dry Sage Palette) ---
val SageColor = Color(0xFFCCD5AE)
val BeigeColor = Color(0xFFE9EDC9)
val CornsilkColor = Color(0xFFFEFAE0)
val PapayaColor = Color(0xFFFAEDCD)
val BronzeColor = Color(0xFFD4A373)

private val LightColorScheme = lightColorScheme(
    primary = BronzeColor,
    onPrimary = Color.White,
    primaryContainer = SageColor,
    onPrimaryContainer = Color(0xFF3E442B), // Very dark sage
    
    secondary = SageColor,
    onSecondary = Color(0xFF3E442B),
    secondaryContainer = PapayaColor,
    onSecondaryContainer = Color(0xFF5D4037), // Darker bronze/brown
    
    tertiary = Color(0xFFB7B7A4), // A muted complementary tone
    
    background = BeigeColor,
    onBackground = Color(0xFF1C1C1C),
    
    surface = CornsilkColor,
    onSurface = Color(0xFF1C1C1C),
    
    surfaceVariant = SageColor.copy(alpha = 0.3f), // Used for cards in store
    onSurfaceVariant = Color(0xFF3E442B)
)

// --- DARK THEME (Ink Black Palette) ---
val InkBlack = Color(0xFF0D0E14)
val JetBlack = Color(0xFF252933)
val CharcoalBlue = Color(0xFF404556)
val DeepTeal = Color(0xFF597D7C)
val DarkSlateGrey = Color(0xFF20504E)
val Evergreen = Color(0xFF193D31)

private val DarkColorScheme = darkColorScheme(
    primary = DeepTeal,
    onPrimary = Color.White,
    primaryContainer = Evergreen,
    onPrimaryContainer = Color.White,
    
    secondary = CharcoalBlue,
    onSecondary = Color.White,
    secondaryContainer = DarkSlateGrey,
    onSecondaryContainer = Color.White,
    
    background = InkBlack,
    onBackground = Color.White,
    
    surface = JetBlack,
    onSurface = Color.White,
    
    surfaceVariant = CharcoalBlue.copy(alpha = 0.5f),
    onSurfaceVariant = Color.White
)

// --- WAVY THEME (Special Shader Palette) ---
private val WavyColorScheme = darkColorScheme(
    primary = Color(0xFFEADDFF), // Light purple, almost white
    onPrimary = Color(0xFF21005D),
    primaryContainer = Color(0xFF4A00E0).copy(alpha = 0.5f),
    onPrimaryContainer = Color(0xFFEADDFF),
    
    secondary = Color(0xFFD0BCFF),
    onSecondary = Color(0xFF381E72),
    secondaryContainer = Color(0xFF0D0221).copy(alpha = 0.5f),
    onSecondaryContainer = Color(0xFFEADDFF),
    
    background = Color(0xFF0D0221),
    onBackground = Color.White,
    
    surface = Color(0xFF1B065E).copy(alpha = 0.8f),
    onSurface = Color.White,
    
    surfaceVariant = Color(0xFF4A00E0).copy(alpha = 0.3f),
    onSurfaceVariant = Color.White
)

@Composable
fun EcoGardenTheme(
    useDarkTheme: Boolean = false,
    useWavyTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        useWavyTheme -> WavyColorScheme
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
