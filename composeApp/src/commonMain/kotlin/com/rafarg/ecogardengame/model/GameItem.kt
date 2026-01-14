package com.rafarg.ecogardengame.model

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.ui.SpriteAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

/**
 * Data class representing a single flying particle in the animation.
 *
 * It holds the individual animatable states (X, Y, and Alpha) for a particle
 * that appears when an item is clicked.
 */
data class FlyingParticle(
    val id: Long,
    val animatableX: Animatable<Float, *> = Animatable(0f),
    val animatableY: Animatable<Float, *> = Animatable(0f),
    val animatableAlpha: Animatable<Float, *> = Animatable(1f)
)

/**
 * The core interface for all interactable items in the game (vegetables, fruits, etc.).
 *
 * It defines the data properties (price, name, resource) and the required UI
 * behaviors (Animate, animateClick, ParticleEffect).
 *
 * How to use: Implement this interface for any new game object, or inherit from [BaseVegetable].
 */
interface GameItem {
    val id: String
    val name: String
    val resource: DrawableResource
    val price: Int
    var unlocked: Boolean
    val particleEmoji: String

    @Composable
    fun Animate(modifier: Modifier)

    fun animateClick(
        scope: CoroutineScope,
        scale: Animatable<Float, *>,
        translationY: Animatable<Float, *>,
        rotation: Animatable<Float, *>,
        clicks: Int,
        onEmitParticles: (List<FlyingParticle>) -> Unit
    )

    @Composable
    fun ParticleEffect(particles: List<FlyingParticle>, updateParticles: (List<FlyingParticle>) -> Unit)
}

/**
 * An abstract implementation of [GameItem] providing default behaviors.
 *
 * This class handles the standard 3-frame sprite animation and the default 
 * particle explosion effect.
 *
 * How to use: Create a sub-class and override only the properties (id, name, etc.)
 * or specific animation logic if unique behavior is needed.
 */
abstract class BaseVegetable : GameItem {
    @Composable
    override fun Animate(modifier: Modifier) {
        SpriteAnimation(
            painter = painterResource(resource),
            frameCount = 3,
            modifier = modifier
        )
    }

    override fun animateClick(
        scope: CoroutineScope,
        scale: Animatable<Float, *>,
        translationY: Animatable<Float, *>,
        rotation: Animatable<Float, *>,
        clicks: Int,
        onEmitParticles: (List<FlyingParticle>) -> Unit
    ) {
        scope.launch {
            scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
            scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
        }

        // Default particle emission logic using Random for IDs in common code
        val newParticles = List(5) {
            FlyingParticle(id = Random.nextLong())
        }
        onEmitParticles(newParticles)
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
                        animationSpec = tween(durationMillis = animationDuration, easing = androidx.compose.animation.core.EaseOut)
                    )
                }
                launch {
                    particle.animatableX.animateTo(
                        targetValue = (Random.nextFloat() * explosionDistance) - (explosionDistance / 2),
                        animationSpec = tween(durationMillis = animationDuration, easing = androidx.compose.animation.core.EaseOut)
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
