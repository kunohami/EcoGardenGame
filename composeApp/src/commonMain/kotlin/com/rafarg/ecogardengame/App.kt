package com.rafarg.ecogardengame

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.random.Random

// --- RESOURCES IMPORT ---
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.tomato_strip
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

data class FlyingParticle(
    val id: Long,
    val animatableX: Animatable<Float, *> = Animatable(0f),
    val animatableY: Animatable<Float, *> = Animatable(0f),
    val animatableAlpha: Animatable<Float, *> = Animatable(1f)
)

private var particleIdCounter = 0L

@Composable
fun SpriteAnimation(
    painter: Painter,
    frameCount: Int,
    modifier: Modifier = Modifier,
    frameDurationMillis: Long = 150
) {
    var frame by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(frameDurationMillis)
            frame = (frame + 1) % frameCount
        }
    }

    Canvas(modifier = modifier) {
        // Calculate the width of the entire strip as if drawn to fill height
        val drawWidth = size.width * frameCount
        val drawHeight = size.height
        
        clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
            translate(left = -frame * size.width, top = 0f) {
                with(painter) {
                    draw(size = Size(drawWidth, drawHeight))
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        var count by remember { mutableStateOf(0) }
        val scope = rememberCoroutineScope()
        val scale = remember { Animatable(1f) }
        val translationY = remember { Animatable(0f) }
        val rotation = remember { Animatable(0f) }

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

                // Sprite Animation
                // Ensure "tomato_strip.png" exists in composeApp/src/commonMain/composeResources/drawable/
                SpriteAnimation(
                    painter = painterResource(Res.drawable.tomato_strip),
                    frameCount = 3,
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                            this.translationY = translationY.value
                            rotationZ = rotation.value
                        }
                        .clickable (
                            // 1. Create a remembered interaction source
                            interactionSource = remember { MutableInteractionSource() },
                            // 2. Set indication to null to remove the ripple effect
                            indication = null) {

                            count++
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

                            if (count > 0 && count % 35 == 0) {
                                val newParticles = (1..10).map {
                                    FlyingParticle(id = particleIdCounter++)
                                }
                                flyingApples = flyingApples + newParticles
                            }
                        }
                )
            }

            flyingApples.forEach { particle ->
                LaunchedEffect(particle.id) {
                    val explosionDistance = 1900f
                    val animationDuration = 1000

                    launch {
                        particle.animatableY.animateTo(
                            targetValue = (Random.nextFloat() * explosionDistance) - (explosionDistance / 2),
                            animationSpec = tween(durationMillis = animationDuration, easing = EaseOut)
                        )
                    }
                    launch {
                        particle.animatableX.animateTo(
                            targetValue = (Random.nextFloat() * explosionDistance) - (explosionDistance / 2),
                            animationSpec = tween(durationMillis = animationDuration, easing = EaseOut)
                        )
                    }
                    launch {
                        delay((animationDuration / 2).toLong())
                        particle.animatableAlpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = (animationDuration / 2))
                        )
                        flyingApples = flyingApples.filterNot { it.id == particle.id }
                    }
                }

                Text(
                    text = "🍎",
                    fontSize = 32.sp,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = particle.animatableX.value.toInt(),
                                y = particle.animatableY.value.toInt()
                            )
                        }
                        .alpha(particle.animatableAlpha.value)
                )
            }
        }
    }
}
