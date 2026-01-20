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
import com.rafarg.ecogardengame.util.vibrate
import com.rafarg.ecogardengame.util.startListeningForShake
import com.rafarg.ecogardengame.util.stopListeningForShake
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

    override val modifiers: List<GameplayModifier> = listOf(
        GameplayModifier(
            id = "garlic_cluster",
            name = "Garlic Cluster",
            description = "Double the small garlics after explosion, +150% final reward.",
            unlockCost = ItemCost(money = 2500, vegetableCosts = mapOf("garlic" to 150, "purple_onion" to 30)),
            targetItemId = "garlic"
        ),
        GameplayModifier(
            id = "garlic_shake",
            name = "Shake to Harvest",
            description = "Collect 5 small garlics at once by shaking your phone.",
            unlockCost = ItemCost(money = 3000, vegetableCosts = mapOf("garlic" to 300, "squash" to 20)),
            targetItemId = "garlic"
        )
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
        var clickCount by remember { mutableStateOf(0) }
        var isExploded by remember { mutableStateOf(false) }
        val pieces = remember { mutableStateListOf<GarlicPiece>() }
        val flyingParticles = remember { mutableStateListOf<FlyingParticle>() }

        val isClusterActive = activeModifiers.any { it.id == "garlic_cluster" && it.isEnabled }
        val isShakeActive = activeModifiers.any { it.id == "garlic_shake" && it.isEnabled }

        val vibrationIntensityVal = (clickCount.toFloat() / 10f) * 10f
        val infiniteTransition = rememberInfiniteTransition()
        val vibX by infiniteTransition.animateFloat(
            initialValue = -vibrationIntensityVal,
            targetValue = vibrationIntensityVal,
            animationSpec = infiniteRepeatable(
                animation = tween(50, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        // Function to collect pieces (either by click or by shake)
        fun collectPieces(count: Int, pieceToCollect: GarlicPiece? = null) {
            val piecesToProcess = if (pieceToCollect != null) {
                listOf(pieceToCollect)
            } else {
                pieces.filter { it.isVisible }.take(count)
            }
            
            if (piecesToProcess.isEmpty()) return

            if (vibrationEnabled) {
                vibrate(vibrationIntensity.toLong())
            }

            piecesToProcess.forEach { piece ->
                piece.isVisible = false
                
                // Final reward logic
                if (pieces.none { it.isVisible }) {
                    val multiplier = if (isClusterActive) 2.5f else 1.0f
                    val bonusRewards = listOf(
                        Reward(emoji = "🪙", moneyValue = (20 * multiplier).toInt(), countValue = 0),
                        Reward(emoji = particleEmoji, countValue = (10 * multiplier).toInt(), resource = resource)
                    )
                    onVegetableClick(bonusRewards)
                    
                    val captureX = piece.animatableX.value
                    val captureY = piece.animatableY.value
                    
                    val newOnes = createRewardParticles(
                        rewards = bonusRewards,
                        offsetX = captureX,
                        offsetY = captureY
                    )
                    
                    val activeCount = flyingParticles.count { !it.isManuallyRemoved }
                    val overflow = (activeCount + newOnes.size) - 20
                    if (overflow > 0) {
                        flyingParticles.filter { !it.isManuallyRemoved }.take(overflow).forEach { it.isManuallyRemoved = true }
                    }
                    flyingParticles.addAll(newOnes)
                    
                    isExploded = false
                    clickCount = 0
                    pieces.clear()
                }
            }
        }

        // Shake detection logic
        DisposableEffect(isExploded, isShakeActive) {
            if (isExploded && isShakeActive) {
                startListeningForShake {
                    collectPieces(5)
                }
            }
            onDispose {
                stopListeningForShake()
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (!isExploded) {
                Box(contentAlignment = Alignment.Center) {
                    SpriteAnimation(
                        painter = painterResource(resource),
                        frameCount = 3,
                        modifier = modifier
                            .size(200.dp)
                            .offset { IntOffset(if (clickCount > 0) vibX.roundToInt() else 0, 0) }
                            .graphicsLayer {
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
                                    val pieceCount = if (isClusterActive) 20 else 10
                                    repeat(pieceCount) {
                                        val targetX = (Random.nextFloat() - 0.5f) * 600f
                                        val targetY = (Random.nextFloat() - 0.5f) * 800f
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
                                    
                                    val newOnes = createRewardParticles(baseRewards)
                                    val activeCount = flyingParticles.count { !it.isManuallyRemoved }
                                    val overflow = (activeCount + newOnes.size) - 20
                                    if (overflow > 0) {
                                        flyingParticles.filter { !it.isManuallyRemoved }.take(overflow).forEach { it.isManuallyRemoved = true }
                                    }
                                    flyingParticles.addAll(newOnes)
                                }
                            }
                    )
                }
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
                                            // Fix: Pass the specific piece to ensure the clicked one disappears
                                            collectPieces(1, piece)
                                        }
                                )
                            }
                        }
                    }
                }
            }
            
            ParticleEffect(flyingParticles)
        }
    }
}
