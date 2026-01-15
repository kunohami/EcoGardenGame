package com.rafarg.ecogardengame.model

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.ui.SpriteAnimation
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.purpleonion_strip
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Purple Onion implementation with a "Teleport and Rotate" gameplay.
 * It fades out when clicked, reappears in a random location, and rotates.
 * Clicking while rotating gives bonus coins.
 */
class PurpleOnion : BaseVegetable() {
    override val id: String = "purple_onion"
    override val name: String = "Purple Onion"
    override val resource = Res.drawable.purpleonion_strip
    override val price: Int = 200
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🧅"

    // Default is 2 coins, but we'll override this in the click handler for the bonus
    override val baseRewards: List<Reward> get() = listOf(
        Reward(emoji = particleEmoji, countValue = 1),
        Reward(emoji = "🪙", moneyValue = 1, countValue = 0)
    )

    @Composable
    override fun Content(
        modifier: Modifier,
        onVegetableClick: (List<Reward>) -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val alpha = remember { Animatable(1f) }
        val rotation = remember { Animatable(0f) }
        val scale = remember { Animatable(1f) }
        var flyingParticles by remember { mutableStateOf<List<FlyingParticle>>(emptyList()) }

        var posX by remember { mutableStateOf(0f) }
        var posY by remember { mutableStateOf(0f) }
        
        var parentWidth by remember { mutableStateOf(0f) }
        var parentHeight by remember { mutableStateOf(0f) }

        val itemSize = 130.dp
        val itemSizePx = with(LocalDensity.current) { itemSize.toPx() }

        // Function to handle the teleportation
        suspend fun teleport() {
            // Fade Out
            alpha.animateTo(0f, tween(300))
            
            // Randomize position within bounds
            if (parentWidth > 0 && parentHeight > 0) {
                val limitX = (parentWidth - itemSizePx) / 2
                val limitY = (parentHeight - itemSizePx) / 2
                posX = Random.nextFloat() * (limitX * 2) - limitX
                posY = Random.nextFloat() * (limitY * 2) - limitY
            }
            
            // Fade In
            alpha.animateTo(1f, tween(300))
            
            // Start Rotation (Teleport finished, now "vulnerable" for bonus)
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(300, easing = LinearEasing)
            )
            rotation.snapTo(0f) // Reset rotation after finishing
        }

        // Initial teleport to a random spot when first loaded
        LaunchedEffect(parentWidth, parentHeight) {
            if (parentWidth > 0 && parentHeight > 0) {
                teleport()
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
            Box(
                modifier = Modifier
                    .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                    .size(itemSize),
                contentAlignment = Alignment.Center
            ) {
                SpriteAnimation(
                    painter = painterResource(resource),
                    frameCount = 3,
                    modifier = modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            this.alpha = alpha.value
                            rotationZ = rotation.value
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = alpha.value > 0.8f // Disable clicking while fading
                        ) {
                            // Determine rewards: Bonus if rotating
                            val isRotating = rotation.value > 0 && rotation.value < 360
                            val currentRewards = if (isRotating) {
                                listOf(
                                    Reward(emoji = particleEmoji, countValue = 1),
                                    Reward(emoji = "🪙", moneyValue = 4, countValue = 0)
                                )
                            } else {
                                baseRewards
                            }

                            onVegetableClick(currentRewards)
                            
                            // Visuals
                            scope.launch {
                                // Small bounce
                                scale.animateTo(0.8f, spring())
                                scale.animateTo(1f, spring())
                                
                                // Teleport to new location
                                teleport()
                            }

                            // Emission
                            flyingParticles = currentRewards.flatMap { reward ->
                                List(if (reward.moneyValue > 0) reward.moneyValue else 1) {
                                    FlyingParticle(id = Random.nextLong(), emoji = reward.emoji)
                                }
                            }
                        }
                )

                ParticleEffect(flyingParticles) {
                    flyingParticles = it
                }
            }
        }
    }
}
