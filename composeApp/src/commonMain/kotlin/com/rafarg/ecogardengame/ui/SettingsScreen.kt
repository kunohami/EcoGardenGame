package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showDebugDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun showToast(message: String) {
        scope.launch {
            toastMessage = message
            delay(2000)
            toastMessage = null
        }
    }

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
                onClick = { showDebugDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Add +100k All Resources")
            }

            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset Game Progress")
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(onClick = onBack) {
                Text("Back to Misc")
            }
        }

        // Confirmation Dialog for Debug
        if (showDebugDialog) {
            AlertDialog(
                onDismissRequest = { showDebugDialog = false },
                title = { Text("Add Resources") },
                text = { Text("Are you sure you want to add 100k to all resources? This is for testing purposes.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.debugAddResources()
                        showDebugDialog = false
                        showToast("Resources added successfully!")
                    }) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = { showDebugDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Confirmation Dialog for Reset
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset Progress") },
                text = { Text("Are you absolutely sure? This will delete all your progress, money and unlocked vegetables. This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetGame()
                            showResetDialog = false
                            showToast("Game progress reset.")
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Reset Everything") }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Simple Custom Toast
        toastMessage?.let { message ->
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
