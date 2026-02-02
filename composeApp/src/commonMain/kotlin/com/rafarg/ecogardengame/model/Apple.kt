package com.rafarg.ecogardengame.model

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.ui.SpriteAnimation
import com.rafarg.ecogardengame.util.startListeningForRotation
import com.rafarg.ecogardengame.util.stopListeningForRotation
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.painterResource
import kotlin.math.*
import kotlin.random.Random

/**
 * Apple vegetable implementation.
 * Mechanics: Passive harvest based on device rotation. The apple moves around a circular path.
 * The user must rotate the physical device (using the compass/gyroscope) to align a pointer
 * with the apple. When aligned, it automatically harvests rewards every 333ms.
 */
class Apple : BaseVegetable() {
    override val id: String = "apple"
    override val nameRes = Res.string.item_apple
    override val resource = Res.drawable.apple_strip
    override val price: Int = 500
    override val unlockCost: ItemCost = GamePrices.UNLOCK_APPLE
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🍎"
    override val tutorialRes = Res.string.tutorial_apple

    override val baseRewards: List<Reward> get() = listOf(
        Reward(emoji = particleEmoji, countValue = 1, resource = resource),
        Reward(emoji = "🪙", moneyValue = GamePrices.REWARD_MONEY_APPLE, countValue = 0)
    )

    override val modifiers: List<GameplayModifier> = listOf(
        GameplayModifier(
            id = "apple_overclock",
            nameRes = Res.string.mod_apple_high_freq_name,
            descriptionRes = Res.string.mod_apple_high_freq_desc,
            unlockCost = GamePrices.MOD_APPLE_HIGH_FREQ,
            targetItemId = "apple"
        )
    )

    @Composable
    override fun Content(
        modifier: Modifier,
        onVegetableClick: (List<Reward>) -> List<Reward>,
        activeModifiers: List<GameplayModifier>,
        vibrationEnabled: Boolean,
        vibrationIntensity: Float
    ) {
        val flyingParticles = remember { mutableStateListOf<FlyingParticle>() }
        
        // State for target position and device orientation
        var targetAngle by remember { mutableStateOf(0f) }
        var deviceRotation by remember { mutableStateOf(0f) }
        
        val density = LocalDensity.current
        val circleRadius = 120.dp
        val circleRadiusPx = with(density) { circleRadius.toPx() }

        val isOverclocked = activeModifiers.any { it.id == "apple_overclock" && it.isEnabled }
        
        // Setup rotation sensor listener
        DisposableEffect(Unit) {
            startListeningForRotation { azimuth ->
                deviceRotation = azimuth
            }
            onDispose {
                stopListeningForRotation()
            }
        }

        // Target movement logic: moves the apple around the circle with random speed/direction variations
        LaunchedEffect(Unit, isOverclocked) {
            var lastTime = withFrameMillis { it }
            var currentSpeed = 20f
            var currentDirection = 1f
            var lastChangeTime = 0L
            val speedMultiplier = if (isOverclocked) 2f else 1f
            
            while (isActive) {
                withFrameMillis { time ->
                    val delta = (time - lastTime) / 1000f
                    
                    // Periodically update movement parameters for unpredictability
                    if (lastChangeTime == 0L || time - lastChangeTime > (Random.nextLong(2000, 5000))) {
                        currentSpeed = (Random.nextFloat() * 40f + 15f) * speedMultiplier
                        if (Random.nextFloat() > 0.6f) { // 40% chance to flip direction
                            currentDirection *= -1f
                        }
                        lastChangeTime = time
                    }

                    targetAngle = (targetAngle + currentDirection * currentSpeed * delta + 360f) % 360f
                    lastTime = time
                }
            }
        }

        // Determine if the player is currently aligned with the apple
        val isAligned = remember(targetAngle, deviceRotation) {
            val diff = abs(targetAngle - deviceRotation)
            val shortestDiff = if (diff > 180) 360 - diff else diff
            shortestDiff < 15f
        }

        // Automatic harvest loop when aligned
        LaunchedEffect(isAligned, isOverclocked) {
            if (isAligned) {
                val currentRewards = if (isOverclocked) {
                    listOf(
                        Reward(emoji = particleEmoji, countValue = 1, resource = resource),
                        Reward(emoji = "🪙", moneyValue = GamePrices.REWARD_APPLE_HIGH_FREQ_MONEY, countValue = 0)
                    )
                } else {
                    baseRewards
                }

                while (isActive) {
                    val finalRewards = onVegetableClick(currentRewards)
                    
                    val newOnes = createRewardParticles(finalRewards)
                    val activeCount = flyingParticles.count { !it.isManuallyRemoved }
                    val overflow = (activeCount + newOnes.size) - 20
                    if (overflow > 0) {
                        flyingParticles.filter { !it.isManuallyRemoved }.take(overflow).forEach { it.isManuallyRemoved = true }
                    }
                    flyingParticles.addAll(newOnes)
                    
                    delay(333) // Harvest tick every 333ms
                }
            }
        }

        val onBackgroundColor = MaterialTheme.colorScheme.onBackground

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Circle path visualization
            Canvas(modifier = Modifier.size(circleRadius * 2)) {
                drawCircle(
                    color = onBackgroundColor.copy(alpha = 0.2f),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            
            // Pointer at the top
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .offset(y = (-circleRadius))
                    .background(if (isAligned) Color.Green else onBackgroundColor.copy(alpha = 0.5f))
            )

            // Calculate apple screen position based on target and device rotation
            val displayAngle = (targetAngle - deviceRotation + 360) % 360
            val radians = displayAngle.toDouble() * PI / 180.0
            val appleX = (circleRadiusPx * sin(radians)).toFloat()
            val appleY = (-circleRadiusPx * cos(radians)).toFloat()

            Box(
                modifier = Modifier
                    .offset { IntOffset(appleX.roundToInt(), appleY.roundToInt()) }
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                SpriteAnimation(
                    painter = painterResource(resource),
                    frameCount = 3,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            if (isAligned) {
                Text(
                    "COLLECTING!", 
                    color = Color.Green, 
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.offset(y = circleRadius + 40.dp)
                )
            }

            ParticleEffect(flyingParticles)
        }
    }
}
