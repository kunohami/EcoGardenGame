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
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.tomato_strip
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.math.sin

class Tomato : BaseVegetable() {
    override val id: String = "tomato"
    override val name: String = "Tomato"
    override val resource = Res.drawable.tomato_strip
    override val price: Int = 0
    override val unlockCost: ItemCost = ItemCost(money = 0)
    override var unlocked: Boolean = true
    override val particleEmoji: String = "🍅"

    // Track critical hits for achievement
    var criticalHits by mutableStateOf(0)

    override val modifiers: List<GameplayModifier> = listOf(
        GameplayModifier(
            id = "tomato_precision_vibration",
            name = "Haptic Timing",
            description = "Vibrates when the super reward is ready.",
            unlockCost = ItemCost(money = 300, vegetableCosts = mapOf("tomato" to 150)),
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

        val hasPrecisionVibration = activeModifiers.any { it.id == "tomato_precision_vibration" && it.isEnabled }

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

        val cycleDuration = 5000f // 5 seconds
        val elapsed = (currentTime - cycleStartTime).coerceAtLeast(0L)
        val cycleProgress = (elapsed % cycleDuration.toLong()) / cycleDuration
        
        // Super reward check for visual feedback
        val isPrecisionWindowActive = cycleProgress > 0.90f && (currentTime - lastClickTime) > 1500

        // Continuous vibration logic if modifier is enabled
        LaunchedEffect(isPrecisionWindowActive) {
            if (isPrecisionWindowActive && hasPrecisionVibration && vibrationEnabled) {
                vibrate(30) // Small pulse when it starts being active
            }
        }

        // Vibration and visuals based on progress
        val vibrationIntensityVal = cycleProgress * 15f
        val vibrationValue = if (cycleProgress > 0.1f) {
            (sin(currentTime.toDouble() / 30.0) * vibrationIntensityVal.toDouble()).toFloat()
        } else 0f
        
        val growthScale = 1f + (cycleProgress * 0.4f)
        val shineAlpha = cycleProgress

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // --- SHINING UNDERNEATH ---
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

            // --- THE TOMATO ---
            SpriteAnimation(
                painter = painterResource(resource),
                frameCount = 3,
                modifier = modifier
                    .size(220.dp)
                    // Vibration becomes vertical when the super reward is active
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
                        
                        val rewards = if (isPrecisionClick) {
                            criticalHits++
                            listOf(
                                Reward(emoji = "🪙", moneyValue = 30, countValue = 0),
                                Reward(emoji = particleEmoji, countValue = 20, resource = resource)
                            )
                        } else {
                            baseRewards
                        }

                        val finalRewards = onVegetableClick(rewards)
                        lastClickTime = now
                        cycleStartTime = now // Restart the 5s cycle

                        scope.launch {
                            punchScale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                            punchScale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                        }
                        
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
