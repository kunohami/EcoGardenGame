package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * LoginScreen handles user authentication and cloud synchronization.
 * It allows players to sign in with Google, upload their progress to the cloud,
 * or download an existing save.
 */
@Composable
fun LoginScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onGoogleSignIn: () -> Unit,
) {
    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified
    val user = viewModel.currentUser

    // Coroutine scope for showing snackbars (toasts)
    val scope = rememberCoroutineScope()
    // Manages the display of bottom notifications
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Confirmation dialog states
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    // Localization strings
    val saveUploadedMsg = stringResource(Res.string.save_uploaded)
    val saveDownloadedMsg = stringResource(Res.string.save_downloaded)
    val noCloudSaveMsg = stringResource(Res.string.no_cloud_save)
    val profileUpdatedMsg = stringResource(Res.string.profile_updated)
    val cooldownMsg = stringResource(Res.string.cooldown_wait)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Necessary to show snackbars
        containerColor = Color.Transparent,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(Res.string.login_title),
                style = MaterialTheme.typography.displaySmall,
                color = primaryText,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- USER STATUS UI ---
            if (user == null) {
                // If NOT logged in, show the Google Sign In button
                Text(
                    text = stringResource(Res.string.login_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = primaryText,
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Custom styled Google Button
                OutlinedButton(
                    onClick = onGoogleSignIn,
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF757575)),
                    border = BorderStroke(1.dp, Color(0xFFD1D1D1)),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(text = "G", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF4285F4))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = stringResource(Res.string.sign_in_google), fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                // If logged in, show cloud synchronization options
                Text(
                    text = stringResource(Res.string.logged_in_as, user.name ?: user.email ?: "User"),
                    style = MaterialTheme.typography.titleMedium,
                    color = primaryText,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.cloud_sync_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = primaryText.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (viewModel.isCloudLoading) {
                    // Show a spinner while syncing with the server
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                } else {
                    // 1. UPLOAD SAVE: Sends local data to the server
                    Button(
                        onClick = { showUploadDialog = true },
                        modifier = Modifier.fillMaxWidth(0.8f),
                    ) {
                        Text(stringResource(Res.string.upload_save_button))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. DOWNLOAD SAVE: Retrieves remote data (overwrites local progress)
                    OutlinedButton(
                        onClick = { showDownloadDialog = true },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    ) {
                        Text(stringResource(Res.string.download_save_button))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. UPDATE PROFILE: Updates leaderboard info (CPS, level, etc.)
                    Button(
                        onClick = {
                            viewModel.updatePublicProfile(
                                onSuccess = { scope.launch { snackbarHostState.showSnackbar(profileUpdatedMsg) } },
                                onError = { error ->
                                    scope.launch {
                                        // Some errors contain a cooldown time in seconds
                                        if (error.toIntOrNull() != null) {
                                            snackbarHostState.showSnackbar(cooldownMsg.replace("%1\$d", error))
                                        } else {
                                            snackbarHostState.showSnackbar("Error updating profile")
                                        }
                                    }
                                },
                            )
                        },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    ) {
                        Text(stringResource(Res.string.update_profile_button))
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Log out button
                Button(
                    onClick = { showSignOutDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth(0.5f),
                ) {
                    Text(stringResource(Res.string.sign_out))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = onBack) {
                Text(stringResource(Res.string.back), color = primaryText)
            }
        }
    }

    // --- CONFIRMATION DIALOGS ---

    // Upload Confirmation
    if (showUploadDialog) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text(stringResource(Res.string.upload_save_confirm_title)) },
            text = { Text(stringResource(Res.string.upload_save_confirm_msg)) },
            confirmButton = {
                Button(onClick = {
                    showUploadDialog = false
                    viewModel.uploadSaveToCloud(
                        onSuccess = { scope.launch { snackbarHostState.showSnackbar(saveUploadedMsg) } },
                        onError = { error ->
                            scope.launch {
                                if (error.toIntOrNull() != null) {
                                    snackbarHostState.showSnackbar(cooldownMsg.replace("%1\$d", error))
                                } else {
                                    snackbarHostState.showSnackbar("Sync error")
                                }
                            }
                        },
                    )
                }) { Text(stringResource(Res.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) { Text(stringResource(Res.string.cancel)) }
            },
        )
    }

    // Download Confirmation
    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = { Text(stringResource(Res.string.download_save_confirm_title)) },
            text = { Text(stringResource(Res.string.download_save_confirm_msg)) },
            confirmButton = {
                Button(onClick = {
                    showDownloadDialog = false
                    viewModel.downloadSaveFromCloud(
                        onSuccess = { scope.launch { snackbarHostState.showSnackbar(saveDownloadedMsg) } },
                        onError = { error ->
                            val msg = if (error == "no_save") noCloudSaveMsg else "Error downloading save"
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        },
                    )
                }) { Text(stringResource(Res.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) { Text(stringResource(Res.string.cancel)) }
            },
        )
    }

    // Sign Out Confirmation
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(Res.string.sign_out_warning_title)) },
            text = { Text(stringResource(Res.string.sign_out_warning_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onUserLoggedOut()
                        viewModel.resetGame() // Security measure: reset local state on logout
                        showSignOutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(Res.string.sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
}
