package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
fun ThemesScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Text(stringResource(Res.string.themes_title), style = MaterialTheme.typography.displayMedium)

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Dry Sage Option
            ThemeCard(
                name = stringResource(Res.string.theme_light),
                colors = listOf(SageColor, BeigeColor, CornsilkColor, PapayaColor, BronzeColor),
                isSelected = !viewModel.isDarkTheme && !viewModel.shaderBackgroundEnabled && !viewModel.isAutumnTheme,
                onClick = { 
                    viewModel.setTheme(false) 
                    viewModel.setShaderBackground(false)
                    viewModel.updateAutumnTheme(false)
                }
            )

            // 2. Autumn Woods Option
            ThemeCard(
                name = stringResource(Res.string.theme_autumn),
                colors = listOf(DarkWalnut, SaddleBrown, ToffeeBrown, Camel, KhakiBeige, DrySageLight),
                isSelected = viewModel.isAutumnTheme,
                onClick = { 
                    viewModel.updateAutumnTheme(true)
                    viewModel.setTheme(false)
                    viewModel.setShaderBackground(false)
                }
            )

            // 3. Ink Black Option
            ThemeCard(
                name = stringResource(Res.string.theme_dark),
                colors = listOf(InkBlack, JetBlack, CharcoalBlue, DeepTeal, DarkSlateGrey, Evergreen),
                isSelected = viewModel.isDarkTheme && !viewModel.shaderBackgroundEnabled && !viewModel.isAutumnTheme,
                onClick = { 
                    viewModel.setTheme(true) 
                    viewModel.setShaderBackground(false)
                    viewModel.updateAutumnTheme(false)
                }
            )

            // 4. Wavy Theme (Shader) Option
            ThemeCard(
                name = stringResource(Res.string.theme_wavy),
                colors = listOf(Color(0xFF0D0221), Color(0xFF1B065E), Color(0xFF4A00E0), Color(0xFF8E2DE2)),
                isSelected = viewModel.shaderBackgroundEnabled,
                onClick = { 
                    viewModel.setShaderBackground(true)
                    viewModel.updateAutumnTheme(false)
                    viewModel.setTheme(false)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text(stringResource(Res.string.back_to_misc))
            }
        }
    }
}

@Composable
fun ThemeCard(
    name: String,
    colors: List<Color>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    colors.take(6).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}
