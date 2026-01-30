package com.rafarg.ecogardengame.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TutorialScreen(viewModel: GameViewModel) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 7

    val stepText = when (currentStep) {
        1 -> stringResource(Res.string.tutorial_step1)
        2 -> stringResource(Res.string.tutorial_step2)
        3 -> stringResource(Res.string.tutorial_step3)
        4 -> stringResource(Res.string.tutorial_step4)
        5 -> stringResource(Res.string.tutorial_step5)
        6 -> stringResource(Res.string.tutorial_step6)
        7 -> stringResource(Res.string.tutorial_step7)
        else -> ""
    }

    val clickySprite = when (currentStep) {
        1, 6 -> Res.drawable.clickyopenarms_strip
        4, 7 -> Res.drawable.clickycheekykneel_strip
        else -> Res.drawable.clickyexplain_strip
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) { } // Intercept clicks
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Clicky Sprite
            Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.BottomCenter) {
                key(clickySprite) {
                    SpriteAnimation(
                        painter = painterResource(clickySprite),
                        frameCount = 3,
                        modifier = Modifier.size(250.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text Box (Visual Novel Style) - Centered and smaller height
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight() // Auto-adjust to content
                    .padding(horizontal = 8.dp)
                    .clip(SpeechBubbleShape()),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stepText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep < totalSteps) {
                            TextButton(onClick = { viewModel.completeTutorial() }) {
                                Text(stringResource(Res.string.skip), color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(onClick = { currentStep++ }) {
                                Text(stringResource(Res.string.next))
                            }
                        } else {
                            Button(onClick = { viewModel.completeTutorial() }) {
                                Text(stringResource(Res.string.finish))
                            }
                        }
                    }
                }
            }
            
            // Push everything slightly up towards the center
            Spacer(modifier = Modifier.weight(0.4f))
        }
    }
}
