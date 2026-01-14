package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.viewmodel.GameViewModel

/**
 * Screen for miscellaneous settings and actions like resetting progress.
 * Now contains navigation to Settings and About screens.
 */
@Composable
fun MiscScreen(
    viewModel: GameViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Misc", style = MaterialTheme.typography.displayMedium)
            
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onNavigateToSettings, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text("Settings")
            }

            Button(onClick = onNavigateToAbout, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text("About")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.resetGame() },
                modifier = Modifier.fillMaxWidth(0.6f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Reset Game Progress")
            }
        }
    }
}
