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

private val LightColorScheme =
    lightColorScheme(
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
        onSurfaceVariant = Color(0xFF3E442B),
    )

// --- DARK THEME (Ink Black Palette) ---
val InkBlack = Color(0xFF0D0E14)
val JetBlack = Color(0xFF252933)
val CharcoalBlue = Color(0xFF404556)
val DeepTeal = Color(0xFF597D7C)
val DarkSlateGrey = Color(0xFF20504E)
val Evergreen = Color(0xFF193D31)

private val DarkColorScheme =
    darkColorScheme(
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
        onSurfaceVariant = Color.White,
    )

// --- WAVY THEME (Special Shader Palette) ---
private val WavyColorScheme =
    darkColorScheme(
        primary = Color(0xFFEADDFF), // Light Lilac - Very Light
        onPrimary = Color(0xFF21005D), // Deep Violet - Very Dark
        primaryContainer = Color(0xFF4A00E0).copy(alpha = 0.5f), // Electric Indigo (50% opacity) - Medium/Dark
        onPrimaryContainer = Color(0xFFEADDFF), // Light Lilac - Very Light
        secondary = Color(0xFFD0BCFF), // Lavender - Light
        onSecondary = Color(0xFF381E72), // Dark Purple - Dark
        secondaryContainer = Color(0xFF381E72).copy(alpha = 0.5f), // Dark Purple (50% opacity) - Dark
        onSecondaryContainer = Color(0xFFEADDFF), // Light Lilac - Very Light
        background = Color(0xFF0D0221), // Midnight Violet - Very Dark (Almost Black)
        onBackground = Color.White,
        surface = Color(0xFF1B065E).copy(alpha = 0.8f), // Deep Indigo (80% opacity) - Dark
        onSurface = Color.White,
        surfaceVariant = Color(0xFF4A00E0).copy(alpha = 0.3f), // Electric Indigo (30% opacity) - Medium/Dark
        onSurfaceVariant = Color.White,
    )

// --- AUTUMN WOODS THEME (Earth Tones Palette) ---
val DarkWalnut = Color(0xFF582F0E)
val SaddleBrown = Color(0xFF7F4F24)
val ToffeeBrown = Color(0xFF936639)
val Camel = Color(0xFFA68A64)
val KhakiBeige = Color(0xFFB6AD90)
val DrySageLight = Color(0xFFC2C5AA)
val DrySageDark = Color(0xFFA4AC86)
val DustyOlive = Color(0xFF656D4A)
val Ebony = Color(0xFF414833)
val CharcoalBrown = Color(0xFF333D29)

private val AutumnWoodsColorScheme =
    darkColorScheme(
        primary = ToffeeBrown,
        onPrimary = KhakiBeige,
        primaryContainer = DarkWalnut,
        onPrimaryContainer = SageColor,
        secondary = DustyOlive,
        onSecondary = KhakiBeige,
        secondaryContainer = Ebony,
        onSecondaryContainer = DrySageLight,
        tertiary = Camel,
        onTertiary = DarkWalnut,
        background = CharcoalBrown,
        onBackground = DrySageLight,
        surface = Ebony,
        onSurface = DrySageLight,
        surfaceVariant = SaddleBrown.copy(alpha = 0.4f),
        onSurfaceVariant = KhakiBeige,
    )

@Composable
fun EcoGardenTheme(
    useDarkTheme: Boolean = false,
    useWavyTheme: Boolean = false,
    useAutumnTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            useWavyTheme -> WavyColorScheme
            useAutumnTheme -> AutumnWoodsColorScheme
            useDarkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
