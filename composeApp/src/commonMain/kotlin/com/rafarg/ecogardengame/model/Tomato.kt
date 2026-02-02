package com.rafarg.ecogardengame.model

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.ui.SpriteAnimation
import com.rafarg.ecogardengame.util.vibrate
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.tomato_strip
import ecogardengame.composeapp.generated.resources.item_tomato
import ecogardengame.composeapp.generated.resources.tutorial_tomato
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Tomato vegetable implementation.
 * Mechanics: Every 5 seconds it enters a "precision window" where it grows, vibrates, 
 * and glows. Clicking during this window grants a massive bonus.
 */
class Tomato : BaseVegetable() {
    override val id: String = "tomato"
    override val nameRes = Res.string.item_tomato
    override val resource = Res.drawable.tomato_strip
    override val price: Int = 0
    override val unlockCost: ItemCost = GamePrices.UNLOCK_TOMATO
    override var unlocked: Boolean = true
    override val particleEmoji: String = "🍅"
    override val tutorialRes = Res.string.tutorial_tomato

    override val baseRewards: List<Reward> get() = listOf(
        Reward(emoji = particleEmoji, countValue = 1, resource = resource),
        Reward(emoji = "🪙", moneyValue = GamePrices.REWARD_MONEY_TOMATO, countValue = 0)
    )

    /** Counter for critical hits (precision clicks) to track achievement progress. */
    var criticalHits by mutableStateOf(0)

    override val modifiers: List<GameplayModifier> = listOf(
        GameplayModifier(
            id = "tomato_precision_vibration",
            nameRes = Res.string.mod_tomato_haptic_name,
            descriptionRes = Res.string.mod_tomato_haptic_desc,
            unlockCost = GamePrices.MOD_TOMATO_HAPTIC,
            targetItemId = "tomato"
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
        val scope = rememberCoroutineScope()
        val punchScale = remember { Animatable(1f) }
        val flyingParticles = remember { mutableStateListOf<FlyingParticle>() }
        
        var cycleStartTime by remember { mutableStateOf(0L) }
        var currentTime by remember { mutableStateOf(0L) }
        var lastClickTime by remember { mutableStateOf(0L) }

        // Start internal timer for the precision cycle
        LaunchedEffect(Unit) {
            var firstFrame = true
            while(true) {
                withFrameMillis { time ->
                    if (firstFrame) {
                        cycleStartTime = time
                        firstFrame = false
                    }
                    currentTime = time
                }
            }
        }

        // --- DYNAMIC CYCLE DURATION ---
        // Cycle is 5 seconds. Precision window is the last 10% of the cycle.
        val cycleDuration = 5000f
        val elapsed = (currentTime - cycleStartTime).coerceAtLeast(0L)
        val cycleProgress = (elapsed % cycleDuration.toLong()) / cycleDuration
        
        // Active when progress > 90% and hasn't been clicked recently
        val isPrecisionWindowActive = cycleProgress > 0.90f && (currentTime - lastClickTime) > 1500

        // Haptic feedback for the modifier "Haptic Timing"
        val hasPrecisionVibration = activeModifiers.any { it.id == "tomato_precision_vibration" && it.isEnabled }
        LaunchedEffect(isPrecisionWindowActive) {
            if (isPrecisionWindowActive && hasPrecisionVibration && vibrationEnabled) {
                vibrate(30)
            }
        }

        // Calculate visual vibration based on cycle progress
        val vibrationIntensityVal = cycleProgress * 15f
        val vibrationValue = if (cycleProgress > 0.1f) {
            (sin(currentTime.toDouble() / 30.0) * vibrationIntensityVal.toDouble()).toFloat()
        } else 0f
        
        val growthScale = 1f + (cycleProgress * 0.4f)
        val shineAlpha = cycleProgress

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Background glow effect
            Canvas(modifier = Modifier.size(300.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isPrecisionWindowActive) Color.Cyan.copy(alpha = shineAlpha * 0.9f) 
                            else Color.Yellow.copy(alpha = shineAlpha * 0.8f),
                            Color.Transparent
                        )
                    ),
                    radius = (size.minDimension / 2) * growthScale
                )
            }

            SpriteAnimation(
                painter = painterResource(resource),
                frameCount = 3,
                modifier = modifier
                    .size(220.dp)
                    .offset { 
                        if (isPrecisionWindowActive) IntOffset(0, vibrationValue.roundToInt())
                        else IntOffset(vibrationValue.roundToInt(), 0)
                    }
                    .graphicsLayer {
                        scaleX = punchScale.value * growthScale
                        scaleY = punchScale.value * growthScale
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val now = currentTime
                        val isPrecisionClick = isPrecisionWindowActive
                        
                        // Define rewards based on whether it was a precision click or a normal click
                        val rewards = if (isPrecisionClick) {
                            criticalHits++
                            listOf(
                                Reward(emoji = "🪙", moneyValue = GamePrices.REWARD_TOMATO_CRIT_MONEY, countValue = 0),
                                Reward(emoji = particleEmoji, countValue = GamePrices.REWARD_TOMATO_CRIT_COUNT, resource = resource)
                            )
                        } else {
                            baseRewards
                        }

                        val finalRewards = onVegetableClick(rewards)
                        lastClickTime = now
                        cycleStartTime = now // Reset cycle on click

                        scope.launch {
                            punchScale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                            punchScale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                        }
                        
                        // Particle creation logic
                        val newOnes = createRewardParticles(finalRewards)
                        val activeCount = flyingParticles.count { !it.isManuallyRemoved }
                        val overflow = (activeCount + newOnes.size) - 20
                        if (overflow > 0) {
                            flyingParticles.filter { !it.isManuallyRemoved }.take(overflow).forEach { it.isManuallyRemoved = true }
                        }
                        flyingParticles.addAll(newOnes)
                    }
            )

            ParticleEffect(flyingParticles)
        }
    }
}
