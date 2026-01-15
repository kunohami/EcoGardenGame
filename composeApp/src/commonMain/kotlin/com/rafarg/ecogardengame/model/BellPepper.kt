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
import ecogardengame.composeapp.generated.resources.bellpepper_strip
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.*
import kotlin.random.Random

/**
 * Bell Pepper implementation with a "Dash and Wait" movement.
 * It stays still for a short duration, then dashes fast in random directions,
 * following the "Slow In and Slow Out" animation principle for natural movement.
 */
class BellPepper : BaseVegetable() {
    override val id: String = "bell_pepper"
    override val name: String = "Bell Pepper"
    override val resource = Res.drawable.bellpepper_strip
    override val price: Int = 100
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🫑"

    // Bell Pepper is hard to catch, so it gives 3 coins and 1 pepper
    override val baseRewards: List<Reward> get() = listOf(
        Reward(emoji = particleEmoji, countValue = 1),
        Reward(emoji = "🪙", moneyValue = 3, countValue = 0)
    )

    @Composable
    override fun Content(
        modifier: Modifier,
        onVegetableClick: (List<Reward>) -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val scale = remember { Animatable(1f) }
        var flyingParticles by remember { mutableStateOf<List<FlyingParticle>>(emptyList()) }

        // --- MOVEMENT STATE ---
        var posX by remember { mutableStateOf(0f) }
        var posY by remember { mutableStateOf(0f) }
        
        // Velocity components
        var dirX by remember { mutableStateOf(0f) }
        var dirY by remember { mutableStateOf(0f) }
        
        var isMoving by remember { mutableStateOf(false) }
        var phaseStartTime by remember { mutableStateOf(0L) }

        var parentWidth by remember { mutableStateOf(0f) }
        var parentHeight by remember { mutableStateOf(0f) }

        val itemSize = 120.dp
        val itemSizePx = with(LocalDensity.current) { itemSize.toPx() }
        
        val stationaryDuration = 1000L
        val movingDuration = 2000L
        val maxBaseSpeed = 100f

        LaunchedEffect(parentWidth, parentHeight) {
            if (parentWidth > 0 && parentHeight > 0) {
                // Initialize random direction
                val angle = Random.nextFloat() * 2 * PI.toFloat()
                dirX = cos(angle)
                dirY = sin(angle)
                
                while (true) {
                    withFrameMillis { frameTime ->
                        if (phaseStartTime == 0L) phaseStartTime = frameTime
                        val elapsed = frameTime - phaseStartTime

                        if (!isMoving) {
                            // STATIONARY PHASE
                            if (elapsed >= stationaryDuration) {
                                isMoving = true
                                phaseStartTime = frameTime
                                // Pick a new random direction when starting to move
                                val newAngle = Random.nextFloat() * 2 * PI.toFloat()
                                dirX = cos(newAngle)
                                dirY = sin(newAngle)
                            }
                        } else {
                            // MOVING PHASE (Slow In and Slow Out)
                            if (elapsed >= movingDuration) {
                                isMoving = false
                                phaseStartTime = frameTime
                            } else {
                                // Apply Sine easing: starts at 0, peaks in the middle, returns to 0
                                val progress = elapsed.toFloat() / movingDuration
                                val speedFactor = sin(progress * PI.toFloat()) 
                                val speed = speedFactor * maxBaseSpeed

                                posX += dirX * speed
                                posY += dirY * speed

                                // Bounce logic with randomization to be "erratic"
                                val limitX = (parentWidth - itemSizePx) / 2
                                val limitY = (parentHeight - itemSizePx) / 2

                                if (abs(posX) >= limitX) {
                                    dirX *= -1
                                    dirY += (Random.nextFloat() - 0.5f) * 0.2f
                                    val norm = sqrt(dirX*dirX + dirY*dirY)
                                    dirX /= norm
                                    dirY /= norm
                                    posX = posX.coerceIn(-limitX, limitX)
                                }
                                if (abs(posY) >= limitY) {
                                    dirY *= -1
                                    dirX += (Random.nextFloat() - 0.5f) * 0.2f
                                    val norm = sqrt(dirX*dirX + dirY*dirY)
                                    dirX /= norm
                                    dirY /= norm
                                    posY = posY.coerceIn(-limitY, limitY)
                                }
                            }
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
                            flyingParticles = baseRewards.map { reward ->
                                FlyingParticle(id = Random.nextLong(), emoji = reward.emoji)
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
