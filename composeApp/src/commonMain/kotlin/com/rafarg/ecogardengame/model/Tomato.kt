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

    // Track critical hits for achievement
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

        // Acceder a la información del clima a través de una composición local o similar
        // Para simplificar, asumiremos que el ViewModel está accesible o pasamos el estado
        // Pero como Content es una función de la interfaz, usaremos un truco visual o esperaremos a que el GameViewModel maneje la lógica de recompensas.
        // ACTUALIZACIÓN: La duración del ciclo la manejaremos aquí si podemos detectar la tormenta.
        
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

        // --- DINAMIC CYCLE DURATION (Thunderstorm Bonus) ---
        // Buscamos si hay una tormenta activa en el ViewModel (esto requiere que el ViewModel sea accesible)
        // Por consistencia con el diseño actual, el ciclo de 5s es visual.
        val cycleDuration = 5000f // Default
        
        val elapsed = (currentTime - cycleStartTime).coerceAtLeast(0L)
        val cycleProgress = (elapsed % cycleDuration.toLong()) / cycleDuration
        
        val isPrecisionWindowActive = cycleProgress > 0.90f && (currentTime - lastClickTime) > 1500

        LaunchedEffect(isPrecisionWindowActive) {
            if (isPrecisionWindowActive && hasPrecisionVibration && vibrationEnabled) {
                vibrate(30)
            }
        }

        val vibrationIntensityVal = cycleProgress * 15f
        val vibrationValue = if (cycleProgress > 0.1f) {
            (sin(currentTime.toDouble() / 30.0) * vibrationIntensityVal.toDouble()).toFloat()
        } else 0f
        
        val growthScale = 1f + (cycleProgress * 0.4f)
        val shineAlpha = cycleProgress

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                        cycleStartTime = now

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
