package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * MiscScreen serves as the main navigation hub for secondary features of the game.
 * It uses a grid-like layout of square buttons to navigate to settings, stats, gallery, etc.
 */
@Composable
fun MiscScreen(
    viewModel: GameViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToTutorial: () -> Unit,
    onNavigateToWeather: () -> Unit,
) {
    // Check if background shaders are active to adjust text color for readability
    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- HEADER STRIP ---
        // A visual title for the "Extras" or "Misc" section using a sprite animation.
        SpriteAnimation(
            painter = painterResource(Res.drawable.miscmainmenu_strip),
            frameCount = 3,
            modifier =
                Modifier
                    .height(90.dp)
                    .fillMaxWidth(0.7f),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- BUTTON GRID ---
        // We use a Column of Rows to create a 2-column grid.
        // weight(1f) ensures the grid expands to fill available space.
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val rowModifier = Modifier.weight(1f).fillMaxWidth()

            // Row 1: Settings and Themes
            Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiscSquareButton(stringResource(Res.string.settings_title), onNavigateToSettings, Modifier.weight(1f))
                MiscSquareButton(stringResource(Res.string.themes_title), onNavigateToThemes, Modifier.weight(1f))
            }

            // Row 2: Stats and Gallery
            Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiscSquareButton(stringResource(Res.string.stats_title), onNavigateToStats, Modifier.weight(1f))
                MiscSquareButton("Art Gallery", onNavigateToGallery, Modifier.weight(1f))
            }

            // Row 3: Weather and Tutorial
            Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiscSquareButton(stringResource(Res.string.weather_title), onNavigateToWeather, Modifier.weight(1f))
                MiscSquareButton(stringResource(Res.string.tutorial_title), onNavigateToTutorial, Modifier.weight(1f))
            }

            // Row 4: Login and About
            Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiscSquareButton(stringResource(Res.string.login_title), onNavigateToLogin, Modifier.weight(1f))
                MiscSquareButton(stringResource(Res.string.about_title), onNavigateToAbout, Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

/**
 * A customized button styled as a square for the Misc menu.
 */
@Composable
fun MiscSquareButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxHeight()
                // aspectRatio(1f) tries to keep the button perfectly square
                .aspectRatio(1f, matchHeightConstraintsFirst = true),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(4.dp),
        // Shadows are removed here to match the flat 2D game aesthetic
        elevation =
            ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp,
            ),
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            softWrap = true, // Allows text to wrap into multiple lines if too long
        )
    }
}
