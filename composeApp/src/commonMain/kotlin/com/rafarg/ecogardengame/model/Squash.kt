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
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
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
 * Squash implementation with a "Circular Timing Challenge" gameplay.
 * Moves vertically bouncing off the screen edges. Only clickable inside a central circle.
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
        Reward(emoji = "🪙", moneyValue = 5, countValue = 0)
    )

    @Composable
    override fun Content(
        modifier: Modifier,
        onVegetableClick: (List<Reward>) -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val scale = remember { Animatable(1f) }
        var flyingParticles by remember { mutableStateOf<List<FlyingParticle>>(emptyList()) }
        
        var parentWidth by remember { mutableStateOf(0f) }
        var parentHeight by remember { mutableStateOf(0f) }
        val density = LocalDensity.current
        
        // Circle size is half the previous square size (250/2 = 125)
        val circleSize = 125.dp
        val circleSizePx = with(density) { circleSize.toPx() }
        val itemSize = 150.dp
        val itemSizePx = with(density) { itemSize.toPx() }

        // --- MOVEMENT STATE ---
        var posY by remember { mutableStateOf(0f) }
        var velY by remember { mutableStateOf(80f) } // Speed of vertical bounce

        LaunchedEffect(parentHeight) {
            if (parentHeight > 0) {
                while (true) {
                    withFrameMillis { _ ->
                        posY += velY
                        
                        val limitY = (parentHeight - itemSizePx) / 2
                        if (kotlin.math.abs(posY) >= limitY) {
                            velY *= -1
                            posY = posY.coerceIn(-limitY, limitY)
                        }
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
            // --- THE SQUASH ---
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
                            // Hit detection: Check if within the central circle
                            // Since movement is only vertical and circle is centered, 
                            // we just check if absolute posY is within the circle's radius
                            val isInsideCircle = kotlin.math.abs(posY) < (circleSizePx / 2)
                            
                            if (isInsideCircle) {
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
                        }
                )
            }

            // --- THE OVERLAY FILTER (With Circular hole) ---
            Canvas(modifier = Modifier.fillMaxSize()) {
                val circlePath = Path().apply {
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            center = center,
                            radius = circleSizePx / 2
                        )
                    )
                }

                // Draw grey filter everywhere EXCEPT the central circle
                clipPath(circlePath, clipOp = ClipOp.Difference) {
                    drawRect(Color.Black.copy(alpha = 0.5f))
                }
            }
            
            // Central Circle Border
            Box(
                modifier = Modifier
                    .size(circleSize)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f),
                        radius = size.minDimension / 2
                    )
                }
            }

            // Particles
            ParticleEffect(flyingParticles) {
                flyingParticles = it
            }
        }
    }
}
