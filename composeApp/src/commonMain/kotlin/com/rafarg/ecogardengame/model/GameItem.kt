package com.rafarg.ecogardengame.model

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
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
    val text: String = "",
    val animatableX: Animatable<Float, *> = Animatable(0f),
    val animatableY: Animatable<Float, *> = Animatable(0f),
    val animatableAlpha: Animatable<Float, *> = Animatable(0f)
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

    /**
     * Helper to generate particles with consistent visual style and dispersion.
     */
    fun createRewardParticles(rewards: List<Reward>): List<FlyingParticle> {
        return rewards.map { reward ->
            val isMoney = reward.moneyValue > 0
            val amount = if (isMoney) reward.moneyValue else reward.countValue
            
            val angle = Random.nextDouble(0.0, 360.0)
            val radius = Random.nextFloat() * 80f + 40f
            val radians = angle * (PI / 180.0)
            val startX = (cos(radians) * radius).toFloat()
            val startY = (sin(radians) * radius).toFloat()

            FlyingParticle(
                id = Random.nextLong(), 
                emoji = reward.emoji,
                resource = if (isMoney) null else reward.resource,
                text = "+$amount",
                animatableX = Animatable(startX),
                animatableY = Animatable(startY),
                animatableAlpha = Animatable(0f)
            )
        }
    }

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
                        
                        flyingParticles = flyingParticles + createRewardParticles(baseRewards)
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
                val moveDistance = 100f
                val animationDuration = 1200

                launch {
                    particle.animatableAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 300)
                    )
                    delay(500)
                    particle.animatableAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 400)
                    )
                }
                
                launch {
                    particle.animatableY.animateTo(
                        targetValue = particle.animatableY.value - moveDistance,
                        animationSpec = tween(durationMillis = animationDuration, easing = LinearEasing)
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
                    .alpha(particle.animatableAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = particle.text,
                        fontSize = 24.sp,
                        color = androidx.compose.ui.graphics.Color.White,
                        style = androidx.compose.material3.LocalTextStyle.current.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = androidx.compose.ui.graphics.Color.Black,
                                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    if (particle.resource != null) {
                        SpriteAnimation(
                            painter = painterResource(particle.resource),
                            frameCount = 3,
                            modifier = Modifier.size(30.dp)
                        )
                    } else {
                        Text(
                            text = particle.emoji,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    }
}
