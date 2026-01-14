package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.viewmodel.GameViewModel

/**
 * Screen for game settings (sound, notifications, etc.).
 */
@Composable
fun SettingsScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Settings", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onBack) {
                Text("Back to Misc")
            }
        }
    }
}
