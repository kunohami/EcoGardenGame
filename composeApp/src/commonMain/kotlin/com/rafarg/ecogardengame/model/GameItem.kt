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
 * Represent a single item gained from a click.
 */
data class Reward(
    val emoji: String, 
    val moneyValue: Int = 0, 
    val countValue: Int = 1,
    val resource: DrawableResource? = null
)

/**
 * Represent the cost to unlock an item.
 */
data class ItemCost(
    val money: Int = 0,
    val vegetableCosts: Map<String, Int> = emptyMap() // id to amount
)

/**
 * Now each particle carries its own emoji and optional resource for animation!
 */
data class FlyingParticle(
    val id: Long,
    val emoji: String,
    val resource: DrawableResource? = null,
    val animatableX: Animatable<Float, *> = Animatable(0f),
    val animatableY: Animatable<Float, *> = Animatable(0f),
    val animatableAlpha: Animatable<Float, *> = Animatable(1f)
)

interface GameItem {
    val id: String
    val name: String
    val resource: DrawableResource
    val price: Int
    val unlockCost: ItemCost
    var unlocked: Boolean
    val particleEmoji: String
    
    val baseRewards: List<Reward>

    @Composable
    fun Content(
        modifier: Modifier,
        onVegetableClick: (List<Reward>) -> Unit
    )

    @Composable
    fun ParticleEffect(particles: List<FlyingParticle>, updateParticles: (List<FlyingParticle>) -> Unit)
}

abstract class BaseVegetable : GameItem {
    
    override val baseRewards: List<Reward> get() = listOf(
        Reward(emoji = particleEmoji, countValue = 1, resource = resource),
        Reward(emoji = "🪙", moneyValue = 1, countValue = 0)
    )

    @Composable
    override fun Content(
        modifier: Modifier,
        onVegetableClick: (List<Reward>) -> Unit
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
                        onVegetableClick(baseRewards)
                        
                        scope.launch {
                            scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                            scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                        }
                        
                        flyingParticles = baseRewards.flatMap { reward ->
                            List(if (reward.moneyValue > 0) reward.moneyValue else 1) {
                                FlyingParticle(
                                    id = Random.nextLong(), 
                                    emoji = reward.emoji,
                                    resource = reward.resource
                                )
                            }
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

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = particle.animatableX.value.toInt(),
                            y = particle.animatableY.value.toInt()
                        )
                    }
                    .alpha(particle.animatableAlpha.value)
            ) {
                if (particle.resource != null) {
                    SpriteAnimation(
                        painter = painterResource(particle.resource),
                        frameCount = 3,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Text(
                        text = particle.emoji,
                        fontSize = 32.sp
                    )
                }
            }
        }
    }
}
