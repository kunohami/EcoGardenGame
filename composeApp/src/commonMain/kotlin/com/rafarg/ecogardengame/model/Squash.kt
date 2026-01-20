package com.rafarg.ecogardengame.model

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.ui.SpriteAnimation
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.squash_strip
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Squash implementation with a "Zelda Tennis" timing gameplay.
 * Moves vertically. Only clickable in the bottom 25% of the screen.
 * Hits before the bounce give better rewards.
 */
class Squash : BaseVegetable() {
    override val id: String = "squash"
    override val name: String = "Squash"
    override val resource = Res.drawable.squash_strip
    override val price: Int = 250
    override val unlockCost: ItemCost = ItemCost(
        money = 1000,
        vegetableCosts = mapOf("tomato" to 200, "broccoli" to 75, "bell_pepper" to 30, "purple_onion" to 15)
    )
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🥒"

    override val baseRewards: List<Reward> get() = listOf(
        Reward(emoji = particleEmoji, countValue = 1, resource = resource),
        Reward(emoji = "🪙", moneyValue = 1, countValue = 0)
    )

    private val bonusRewards: List<Reward> get() = listOf(
        Reward(emoji = particleEmoji, countValue = 1, resource = resource),
        Reward(emoji = "🪙", moneyValue = 6, countValue = 0)
    )

    @Composable
    override fun Content(
        modifier: Modifier,
        onVegetableClick: (List<Reward>) -> Unit,
        activeModifiers: List<GameplayModifier>,
        vibrationEnabled: Boolean,
        vibrationIntensity: Float
    ) {
        val scope = rememberCoroutineScope()
        val scale = remember { Animatable(1f) }
        val flyingParticles = remember { mutableStateListOf<FlyingParticle>() }
        
        var parentWidth by remember { mutableStateOf(0f) }
        var parentHeight by remember { mutableStateOf(0f) }
        val density = LocalDensity.current
        
        val itemSize = 150.dp
        val itemSizePx = with(density) { itemSize.toPx() }

        // --- MOVEMENT STATE ---
        var posY by remember { mutableStateOf(0f) }
        var directionY by remember { mutableStateOf(1f) }
        
        // Speed: 800 dp per second
        val speedDpPerSecond = 800.dp
        val speedPxPerSecond = with(density) { speedDpPerSecond.toPx() }

        LaunchedEffect(parentHeight) {
            if (parentHeight > 0) {
                var lastFrameTime = 0L
                while (true) {
                    withFrameMillis { frameTime ->
                        if (lastFrameTime != 0L) {
                            val deltaSeconds = (frameTime - lastFrameTime) / 1000f
                            posY += directionY * speedPxPerSecond * deltaSeconds
                            
                            val limitY = (parentHeight - itemSizePx) / 2
                            if (posY >= limitY) {
                                directionY = -1f
                                posY = limitY
                            } else if (posY <= -limitY) {
                                directionY = 1f
                                posY = -limitY
                            }
                        }
                        lastFrameTime = frameTime
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    parentWidth = it.size.width.toFloat()
                    parentHeight = it.size.height.toFloat()
                },
            contentAlignment = Alignment.Center
        ) {
            // --- BOUNDARY LINE ---
            Canvas(modifier = Modifier.fillMaxSize()) {
                val activeZoneHeight = size.height * 0.25f
                val activeZoneTop = size.height - activeZoneHeight
                
                // Draw a simple separating line
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = androidx.compose.ui.geometry.Offset(0f, activeZoneTop),
                    end = androidx.compose.ui.geometry.Offset(size.width, activeZoneTop),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // --- THE SQUASH ---
            val activeZoneHeight = parentHeight * 0.25f
            val bottomLimit = (parentHeight - itemSizePx) / 2
            val activeZoneStart = bottomLimit - activeZoneHeight
            
            val isInsideActiveZone = posY >= activeZoneStart

            Box(
                modifier = Modifier
                    .offset { IntOffset(0, posY.roundToInt()) }
                    .size(itemSize),
                contentAlignment = Alignment.Center
            ) {
                SpriteAnimation(
                    painter = painterResource(resource),
                    frameCount = 3,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (isInsideActiveZone) {
                                // Hit before reaching bottom (moving down)
                                val isBonus = directionY > 0
                                val rewards = if (isBonus) bonusRewards else baseRewards
                                
                                onVegetableClick(rewards)
                                scope.launch {
                                    scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                    scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                }
                                
                                val newOnes = createRewardParticles(
                                    rewards = rewards,
                                    offsetX = 0f,
                                    offsetY = posY
                                )
                                val activeCount = flyingParticles.count { !it.isManuallyRemoved }
                                val overflow = (activeCount + newOnes.size) - 20
                                if (overflow > 0) {
                                    flyingParticles.filter { !it.isManuallyRemoved }.take(overflow).forEach { it.isManuallyRemoved = true }
                                }
                                flyingParticles.addAll(newOnes)
                                
                                // Bounce it back!
                                directionY = -1f
                            }
                        }
                )
            }

            // Particles layer
            ParticleEffect(flyingParticles)
        }
    }
}
