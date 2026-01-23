package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.stringResource

/**
 * Screen providing information about the game and its developers.
 */
@Composable
fun AboutScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(Res.string.about_title), 
                style = MaterialTheme.typography.displayMedium,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.app_version), 
                style = MaterialTheme.typography.bodyLarge,
                color = primaryText
            )
            Text(
                text = stringResource(Res.string.developed_with), 
                style = MaterialTheme.typography.bodyMedium,
                color = primaryText
            )
            Text(
                text = stringResource(Res.string.human_made_art),
                style = MaterialTheme.typography.bodyMedium,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onBack) {
                Text(stringResource(Res.string.back_to_misc))
            }
        }
    }
}
