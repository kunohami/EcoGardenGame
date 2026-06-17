package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.add_resources
import ecogardengame.composeapp.generated.resources.add_resources_confirm
import ecogardengame.composeapp.generated.resources.back_to_misc
import ecogardengame.composeapp.generated.resources.cancel
import ecogardengame.composeapp.generated.resources.confirm
import ecogardengame.composeapp.generated.resources.debug_options
import ecogardengame.composeapp.generated.resources.general_section
import ecogardengame.composeapp.generated.resources.intensity_label
import ecogardengame.composeapp.generated.resources.reset_progress
import ecogardengame.composeapp.generated.resources.reset_progress_confirm
import ecogardengame.composeapp.generated.resources.reset_progress_toast
import ecogardengame.composeapp.generated.resources.resources_added_toast
import ecogardengame.composeapp.generated.resources.settings_title
import ecogardengame.composeapp.generated.resources.vibration_label
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * SettingsScreen allows players to customize their experience and manage their game data.
 */
@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
) {
    // Visibility states for confirmation dialogs
    var showResetDialog by remember { mutableStateOf(false) }
    var showDebugDialog by remember { mutableStateOf(false) }
    // Holds the text currently typed into the password field
    var passwordInput by remember { mutableStateOf("") }
    // Controls the visibility and content of the custom toast message
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Coroutine scope is needed to run background tasks like the toast timer
    val scope = rememberCoroutineScope()
    // rememberScrollState allows the Column to be scrollable if content overflows
    val scrollState = rememberScrollState()

    /**
     * Shows a temporary message at the bottom of the screen.
     */
    fun showToast(message: String) {
        scope.launch {
            toastMessage = message
            delay(2000) // Message stays for 2 seconds
            toastMessage = null
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
        ) {
            Text(stringResource(Res.string.settings_title), style = MaterialTheme.typography.displayMedium)

            Spacer(modifier = Modifier.height(8.dp))

            // --- GENERAL SECTION ---
            Text(stringResource(Res.string.general_section), style = MaterialTheme.typography.titleMedium, color = Color.Gray)

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Vibration Toggle: Communicates directly with the ViewModel
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(Res.string.vibration_label))
                        Switch(
                            checked = viewModel.vibrationEnabled,
                            onCheckedChange = { viewModel.setVibration(it) },
                        )
                    }

                    // Only show the Intensity slider if vibration is ON
                    if (viewModel.vibrationEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(Res.string.intensity_label, viewModel.vibrationIntensity.toInt()),
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Slider(
                            value = viewModel.vibrationIntensity,
                            onValueChange = { viewModel.updateVibrationIntensity(it) },
                            valueRange = 5f..100f,
                            steps = 19, // Results in 20 distinct points on the slider
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- DEBUG / TESTING SECTION ---
            // Hidden behind a simple password for developer/tester use.
            Text(stringResource(Res.string.debug_options), style = MaterialTheme.typography.titleMedium, color = Color.Gray)

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(), // Masks text with dots
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.weight(1f),
                    )

                    Button(
                        onClick = {
                            if (passwordInput == "vip3aa") {
                                showDebugDialog = true
                            } else {
                                showToast("Incorrect password")
                            }
                        },
                        enabled = passwordInput.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text("OK")
                    }
                }
            }

            // Danger Zone: Resetting progress
            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
                Text(stringResource(Res.string.reset_progress))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onBack) {
                Text(stringResource(Res.string.back_to_misc))
            }
        }

        // --- DIALOGS ---

        // Debug Confirmation Dialog
        if (showDebugDialog) {
            val resourcesAddedToast = stringResource(Res.string.resources_added_toast)
            AlertDialog(
                onDismissRequest = { showDebugDialog = false },
                title = { Text(stringResource(Res.string.add_resources)) },
                text = { Text(stringResource(Res.string.add_resources_confirm)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.debugAddResources() // Cheat: adds coins and veggies
                        showDebugDialog = false
                        passwordInput = "" // Clear password for security
                        showToast(resourcesAddedToast)
                    }) { Text(stringResource(Res.string.confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDebugDialog = false }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }

        // Reset Confirmation Dialog
        if (showResetDialog) {
            val resetProgressToast = stringResource(Res.string.reset_progress_toast)
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(stringResource(Res.string.reset_progress)) },
                text = { Text(stringResource(Res.string.reset_progress_confirm)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetGame() // Wipes all local data
                            showResetDialog = false
                            showToast(resetProgressToast)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) { Text(stringResource(Res.string.reset_progress)) }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }

        // --- CUSTOM TOAST UI ---
        // A simple overlay that appears at the bottom center.
        toastMessage?.let { message ->
            Surface(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
