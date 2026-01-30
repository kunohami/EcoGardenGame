package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.stringResource

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
    onNavigateToWeather: () -> Unit
) {
    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.nav_misc), 
                style = MaterialTheme.typography.displayMedium,
                color = primaryText
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onNavigateToSettings, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text(stringResource(Res.string.settings_title))
            }

            Button(onClick = onNavigateToThemes, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text(stringResource(Res.string.themes_title))
            }

            Button(onClick = onNavigateToStats, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text(stringResource(Res.string.stats_title))
            }

            Button(onClick = onNavigateToGallery, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text("Art Gallery")
            }

            Button(onClick = onNavigateToWeather, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text("Garden Weather")
            }

            Button(onClick = onNavigateToTutorial, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text(stringResource(Res.string.tutorial_title))
            }

            Button(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text(stringResource(Res.string.login_title))
            }

            Button(onClick = onNavigateToAbout, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text(stringResource(Res.string.about_title))
            }
        }
    }
}
