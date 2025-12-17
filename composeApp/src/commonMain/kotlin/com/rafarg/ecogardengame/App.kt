package com.rafarg.ecogardengame

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.red_apple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.random.Random

data class FlyingParticle(
    val id: Long, // Unique ID for each particle
    val animatableX: Animatable<Float, *> = Animatable(0f), // X-position for animation
    val animatableY: Animatable<Float, *> = Animatable(0f),   // Y-position for animation
    val animatableAlpha: Animatable<Float, *> = Animatable(1f) // Alpha for fade animation
)

// This counter will provide a unique ID without using System.nanoTime()
private var particleIdCounter = 0L

@Composable
@Preview
fun App() {
    MaterialTheme {
        var count by remember { mutableStateOf(0) }
        val scope = rememberCoroutineScope()
        val scale = remember { Animatable(1f) }
        val translationY = remember { Animatable(0f) }
        val rotation = remember { Animatable(0f) }

        // State list to hold the apple emojis currently on screen
        var flyingApples by remember { mutableStateOf<List<FlyingParticle>>(emptyList()) }

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Apples: $count", style = MaterialTheme.typography.headlineMedium)
                Image(
                    painter = painterResource(Res.drawable.red_apple),
                    contentDescription = "Red Apple",
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                            this.translationY = translationY.value
                            rotationZ = rotation.value
                        }
                        .clickable {
                            count++
                            // --- Existing Animations (unchanged) ---
                            scope.launch {
                                scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                            }
                            if (count > 0 && count % 10 == 0) {
                                scope.launch { translationY.animateTo(-100f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
                                scope.launch {
                                    delay(100L)
                                    translationY.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                                }
                            }
                            if (count > 0 && count % 25 == 0) {
                                scope.launch {
                                    rotation.animateTo(rotation.value + 360f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessVeryLow))
                                    rotation.snapTo(0f)
                                }
                            }

                            // --- Logic for Flying Apples (unchanged) ---
                            if (count > 0 && count % 35 == 0) {
                                // Create more particles for a bigger explosion
                                val newParticles = (1..10).map {
                                    FlyingParticle(id = particleIdCounter++)
                                }
                                flyingApples = flyingApples + newParticles
                            }
                        }
                )
            }

            // Render and animate each flying apple emoji
            flyingApples.forEach { particle ->
                // --- UPDATE: EXPLOSION ANIMATION LOGIC ---
                LaunchedEffect(particle.id) {
                    val explosionDistance = 1900f // How far the apples will travel
                    val animationDuration = 1000 // How fast the explosion happens in milliseconds

                    // Launch three parallel animations
                    launch {
                        // Animate Y position to a random vertical direction
                        particle.animatableY.animateTo(
                            targetValue = (Random.nextFloat() * explosionDistance) - (explosionDistance / 2),
                            animationSpec = tween(durationMillis = animationDuration, easing = EaseOut)
                        )
                    }
                    launch {
                        // Animate X position to a random horizontal direction
                        particle.animatableX.animateTo(
                            targetValue = (Random.nextFloat() * explosionDistance) - (explosionDistance / 2),
                            animationSpec = tween(durationMillis = animationDuration, easing = EaseOut)
                        )
                    }
                    launch {
                        // Animate alpha (fade out)
                        // Start fading out a bit after the explosion starts
                        delay((animationDuration / 2).toLong())
                        particle.animatableAlpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = (animationDuration / 2))
                        )
                        // Clean up: remove the particle from the list when done
                        flyingApples = flyingApples.filterNot { it.id == particle.id }
                    }
                }

                // This Text composable displays the emoji
                Text(
                    text = "🍎",
                    fontSize = 32.sp, // Slightly smaller for a denser explosion
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = particle.animatableX.value.toInt(), // Use animated X value
                                y = particle.animatableY.value.toInt()
                            )
                        }
                        .alpha(particle.animatableAlpha.value)
                )
            }
        }
    }
}
