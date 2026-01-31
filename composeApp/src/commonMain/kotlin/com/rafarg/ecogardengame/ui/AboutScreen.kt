package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified
    val surfaceColor = if (wavy) Color.Black.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)

    val currentLang = stringResource(Res.string.language_title)
    val isSpanish = currentLang.contains("Idioma")
    val backText = if (isSpanish) "Volver" else "Back"

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // --- TOP BACK BUTTON ---
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backText, tint = primaryText)
            Spacer(modifier = Modifier.width(8.dp))
            Text(backText, color = primaryText)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // --- SPEECH BUBBLE CONTENT ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(SpeechBubbleShape())
                    .background(surfaceColor)
                    .padding(24.dp),
                color = Color.Transparent
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.about_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(Res.string.app_version),
                        style = MaterialTheme.typography.bodyLarge,
                        color = primaryText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.developed_with),
                        style = MaterialTheme.typography.bodyMedium,
                        color = primaryText,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(Res.string.human_made_art),
                        style = MaterialTheme.typography.bodyMedium,
                        color = primaryText,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- CLICKY AT THE BOTTOM ---
            SpriteAnimation(
                painter = painterResource(Res.drawable.clickyexplain_strip),
                frameCount = 3,
                modifier = Modifier.size(180.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
