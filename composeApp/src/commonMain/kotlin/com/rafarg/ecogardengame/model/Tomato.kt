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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * --- GAMEPLAY MECHANIC: PRECISION TIMING ---
 * The Tomato is the first vegetable and teaches the player about "Critical Hits".
 * It pulsates in a 5-second cycle. Clicking at the very end of the cycle (when it's 
 * largest and glowing) grants a huge bonus compared to spam-clicking.
 *
 * --- OOP PRINCIPLES ---
 * - INHERITANCE: Inherits from 'BaseVegetable' to get standard rewards and particle behavior.
 * - ENCAPSULATION: Maintains its own 'criticalHits' counter internally.
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

    /** 
     * Internal state to track precision hits for achievements.
     * In Compose, 'mutableStateOf' ensures that any UI observing this value updates 
     * when the count increases.
     */
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

    /**
     * The Tomato's UI implementation.
     * Demonstrates time-based animations and visual feedback.
     */
    @Composable
    override fun Content(
        modifier: Modifier,
        onVegetableClick: (List<Reward>) -> List<Reward>,
        activeModifiers: List<GameplayModifier>,
        vibrationEnabled: Boolean,
        vibrationIntensity: Float
    ) {
        val scope = rememberCoroutineScope()
        // Animation for the "punch" feel when clicking
        val punchScale = remember { Animatable(1f) }
        val flyingParticles = remember { mutableStateListOf<FlyingParticle>() }
        
        // --- TIMING LOGIC ---
        var cycleStartTime by remember { mutableStateOf(0L) }
        var currentTime by remember { mutableStateOf(0L) }
        var lastClickTime by remember { mutableStateOf(0L) }

        /**
         * --- THE GAME LOOP (Frame-sync) ---
         * 'withFrameMillis' is a low-level Compose API that provides the timestamp
         * of every single frame rendered by the screen. This allows us to create
         * perfectly smooth animations and precision timers.
         */
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

        // --- CYCLE CALCULATION ---
        val cycleDuration = 5000f // 5 seconds
        val elapsed = (currentTime - cycleStartTime).coerceAtLeast(0L)
        val cycleProgress = (elapsed % cycleDuration.toLong()) / cycleDuration
        
        // The precision window is the last 10% of the pulsation cycle.
        val isPrecisionWindowActive = cycleProgress > 0.90f && (currentTime - lastClickTime) > 1500

        /**
         * --- HAPTIC FEEDBACK (Modifier Logic) ---
         * If the 'Haptic Timing' modifier is active, the phone vibrates slightly
         * exactly when the tomato enters its critical state.
         */
        val hasPrecisionVibration = activeModifiers.any { it.id == "tomato_precision_vibration" && it.isEnabled }
        LaunchedEffect(isPrecisionWindowActive) {
            if (isPrecisionWindowActive && hasPrecisionVibration && vibrationEnabled) {
                vibrate(30) // Short feedback pulse
            }
        }

        // Visual "heartbeat" vibration effect
        val vibrationIntensityVal = cycleProgress * 15f
        val vibrationValue = if (cycleProgress > 0.1f) {
            (sin(currentTime.toDouble() / 30.0) * vibrationIntensityVal.toDouble()).toFloat()
        } else 0f
        
        // Growth factor: the tomato gets bigger as it gets closer to harvest.
        val growthScale = 1f + (cycleProgress * 0.4f)
        val shineAlpha = cycleProgress

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            /**
             * --- CANVAS (Drawing Custom Shapes) ---
             * We use Canvas to draw a radial gradient glow behind the tomato.
             * The glow color changes if the critical window is active.
             */
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
                        indication = null // Disables default gray ripple
                    ) {
                        val now = currentTime
                        val isPrecisionClick = isPrecisionWindowActive
                        
                        /**
                         * --- REWARD BRANCHING ---
                         * Based on the state, we decide which reward list to send to the ViewModel.
                         */
                        val rewards = if (isPrecisionClick) {
                            criticalHits++
                            listOf(
                                Reward(emoji = "🪙", moneyValue = GamePrices.REWARD_TOMATO_CRIT_MONEY, countValue = 0),
                                Reward(emoji = particleEmoji, countValue = GamePrices.REWARD_TOMATO_CRIT_COUNT, resource = resource)
                            )
                        } else {
                            baseRewards
                        }

                        // Update business logic in ViewModel and get processed rewards back.
                        val finalRewards = onVegetableClick(rewards)
                        lastClickTime = now
                        cycleStartTime = now // Reset the cycle on every click

                        // Kick off the "punch" animation
                        scope.launch {
                            punchScale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                            punchScale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                        }
                        
                        // Particle management: Create new ones and remove old ones if count > 20
                        val newOnes = createRewardParticles(finalRewards)
                        val activeCount = flyingParticles.count { !it.isManuallyRemoved }
                        val overflow = (activeCount + newOnes.size) - 20
                        if (overflow > 0) {
                            flyingParticles.filter { !it.isManuallyRemoved }.take(overflow).forEach { it.isManuallyRemoved = true }
                        }
                        flyingParticles.addAll(newOnes)
                    }
            )

            // Render the particles layer on top
            ParticleEffect(flyingParticles)
        }
    }
}
