package com.rafarg.ecogardengame.model

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.ui.SpriteAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

/**
 * Data class representing a single flying particle in the animation.
 */
data class FlyingParticle(
    val id: Long,
    val animatableX: Animatable<Float, *> = Animatable(0f),
    val animatableY: Animatable<Float, *> = Animatable(0f),
    val animatableAlpha: Animatable<Float, *> = Animatable(1f)
)

/**
 * The core interface for all interactable items in the game.
 * Refactored to support polymorphic rendering of different gameplay styles.
 */
interface GameItem {
    val id: String
    val name: String
    val resource: DrawableResource
    val price: Int
    var unlocked: Boolean
    val particleEmoji: String

    /**
     * Main rendering entry point for the item.
     * Implementing classes can decide if they are static, moving, etc.
     */
    @Composable
    fun Content(
        modifier: Modifier,
        onVegetableClick: () -> Unit
    )

    @Composable
    fun ParticleEffect(particles: List<FlyingParticle>, updateParticles: (List<FlyingParticle>) -> Unit)
}

/**
 * Base class providing common behaviors like the standard particle effect and default static content.
 */
abstract class BaseVegetable : GameItem {
    
    @Composable
    override fun Content(
        modifier: Modifier,
        onVegetableClick: () -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val scale = remember { Animatable(1f) }
        var flyingParticles by remember { mutableStateOf<List<FlyingParticle>>(emptyList()) }

        Box(contentAlignment = Alignment.Center) {
            SpriteAnimation(
                painter = painterResource(resource),
                frameCount = 3,
                modifier = modifier
                    .size(220.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onVegetableClick()
                        scope.launch {
                            scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                            scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                        }
                        flyingParticles = List(5) {
                            FlyingParticle(id = Random.nextLong())
                        }
                    }
            )

            ParticleEffect(flyingParticles) {
                flyingParticles = it
            }
        }
    }
    
    @Composable
    override fun ParticleEffect(
        particles: List<FlyingParticle>,
        updateParticles: (List<FlyingParticle>) -> Unit
    ) {
        particles.forEach { particle ->
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
                    updateParticles(particles.filterNot { it.id == particle.id })
                }
            }

            Text(
                text = particleEmoji,
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
