package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    viewModel: GameViewModel, 
    onBack: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified
    val user = viewModel.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.login_title),
            style = MaterialTheme.typography.displaySmall,
            color = primaryText
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (user == null) {
            Text(
                text = stringResource(Res.string.login_desc),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = primaryText
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Official-style Google Button
            OutlinedButton(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF757575)
                ),
                border = BorderStroke(1.dp, Color(0xFFD1D1D1)),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Placeholder for Google Icon
                    Text(
                        text = "G",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF4285F4) // Google Blue
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(Res.string.sign_in_google),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        letterSpacing = 0.25.sp
                    )
                }
            }
        } else {
            Text(
                text = stringResource(Res.string.logged_in_as, user.name ?: user.email ?: "User"),
                style = MaterialTheme.typography.headlineSmall,
                color = primaryText,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.sync_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = primaryText.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.onUserLoggedOut() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(stringResource(Res.string.sign_out))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        TextButton(onClick = onBack) {
            Text(stringResource(Res.string.back), color = primaryText)
        }
    }
}
