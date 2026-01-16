package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.viewmodel.GameViewModel

/**
 * Screen for game settings (sound, notifications, etc.).
 */
@Composable
fun SettingsScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.displayMedium)
            
            Spacer(modifier = Modifier.height(8.dp))

            // Debug/Testing Section
            Text("Debug Options", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            
            Button(
                onClick = { viewModel.debugAddResources() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Add +100k All Resources")
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(onClick = onBack) {
                Text("Back to Misc")
            }
        }
    }
}
