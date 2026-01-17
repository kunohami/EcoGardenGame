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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.ui.SpriteAnimation
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.garlic_strip
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.random.Random

class GarlicPiece(
    val id: Long,
    val animatableX: Animatable<Float, *>,
    val animatableY: Animatable<Float, *>,
    isVisibleInitial: Boolean = true
) {
    var isVisible by mutableStateOf(isVisibleInitial)
}

class Garlic : BaseVegetable() {
    override val id: String = "garlic"
    override val name: String = "Garlic"
    override val resource = Res.drawable.garlic_strip
    override val price: Int = 150
    override val unlockCost: ItemCost = ItemCost(
        money = 750,
        vegetableCosts = mapOf("tomato" to 150, "broccoli" to 50, "bell_pepper" to 20, "purple_onion" to 5)
    )
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🧄"

    @Composable
    override fun Content(
        modifier: Modifier,
        onVegetableClick: (List<Reward>) -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val scale = remember { Animatable(1f) }
        var clickCount by remember { mutableStateOf(0) }
        var isExploded by remember { mutableStateOf(false) }
        val pieces = remember { mutableStateListOf<GarlicPiece>() }
        var flyingParticles by remember { mutableStateOf<List<FlyingParticle>>(emptyList()) }

        val vibrationIntensity = (clickCount.toFloat() / 10f) * 10f
        val infiniteTransition = rememberInfiniteTransition()
        val vibX by infiniteTransition.animateFloat(
            initialValue = -vibrationIntensity,
            targetValue = vibrationIntensity,
            animationSpec = infiniteRepeatable(
                animation = tween(50, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (!isExploded) {
                // Main Garlic
                SpriteAnimation(
                    painter = painterResource(resource),
                    frameCount = 3,
                    modifier = modifier
                        .size(200.dp)
                        .offset { IntOffset(if (clickCount > 0) vibX.roundToInt() else 0, 0) }
                        .graphicsLayer {
                            // Calculate growth based on click count (up to 1.5x at 10 clicks)
                            val growth = 1f + (clickCount.toFloat() / 10f) * 0.5f
                            scaleX = scale.value * growth
                            scaleY = scale.value * growth
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            clickCount++
                            if (clickCount >= 10) {
                                pieces.clear()
                                repeat(10) {
                                    val targetX = (Random.nextFloat() - 0.5f) * 500f
                                    val targetY = (Random.nextFloat() - 0.5f) * 700f
                                    val piece = GarlicPiece(
                                        id = Random.nextLong(),
                                        animatableX = Animatable(0f),
                                        animatableY = Animatable(0f)
                                    )
                                    pieces.add(piece)
                                    scope.launch { piece.animatableX.animateTo(targetX, tween(600, easing = EaseOutBack)) }
                                    scope.launch { piece.animatableY.animateTo(targetY, tween(600, easing = EaseOutBack)) }
                                }
                                isExploded = true
                            } else {
                                onVegetableClick(baseRewards)
                                scope.launch {
                                    scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                    scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                }
                                flyingParticles = flyingParticles + createRewardParticles(baseRewards)
                            }
                        }
                )
            } else {
                pieces.forEach { piece ->
                    key(piece.id) {
                        if (piece.isVisible) {
                            val pieceScale = remember { Animatable(1f) }
                            
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(piece.animatableX.value.roundToInt(), piece.animatableY.value.roundToInt()) }
                                    .size(70.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                SpriteAnimation(
                                    painter = painterResource(resource),
                                    frameCount = 3,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = pieceScale.value
                                            scaleY = pieceScale.value
                                        }
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            piece.isVisible = false
                                            
                                            if (pieces.none { it.isVisible }) {
                                                val bonusRewards = listOf(
                                                    Reward(emoji = "🪙", moneyValue = 20, countValue = 0),
                                                    Reward(emoji = particleEmoji, countValue = 10, resource = resource)
                                                )
                                                onVegetableClick(bonusRewards)
                                                
                                                // Create bonus particles and offset them to piece position
                                                val bonusParticles = createRewardParticles(bonusRewards).map { p ->
                                                    p.copy(
                                                        animatableX = Animatable(p.animatableX.value + piece.animatableX.value),
                                                        animatableY = Animatable(p.animatableY.value + piece.animatableY.value)
                                                    )
                                                }
                                                flyingParticles = flyingParticles + bonusParticles
                                                
                                                // Reset instantly
                                                isExploded = false
                                                clickCount = 0
                                                pieces.clear()
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
            
            // Always show particles at the top level
            ParticleEffect(flyingParticles) { flyingParticles = it }
        }
    }
}
