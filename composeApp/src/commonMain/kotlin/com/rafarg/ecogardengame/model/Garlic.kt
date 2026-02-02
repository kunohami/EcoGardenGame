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
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.garlic_strip
import ecogardengame.composeapp.generated.resources.item_garlic
import ecogardengame.composeapp.generated.resources.tutorial_garlic
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Data class representing a small piece of garlic that appears after an explosion.
 */
class GarlicPiece(
    val id: Long,
    val animatableX: Animatable<Float, *>,
    val animatableY: Animatable<Float, *>,
    isVisibleInitial: Boolean = true
) {
    var isVisible by mutableStateOf(isVisibleInitial)
}

/**
 * Garlic vegetable implementation.
 * Mechanics: Requires 10 clicks to "explode" into multiple small cloves. 
 * The player must then click all cloves to receive a large bonus.
 * Modifier "Shake to Harvest" allows collecting multiple cloves by shaking the device.
 */
class Garlic : BaseVegetable() {
    override val id: String = "garlic"
    override val nameRes = Res.string.item_garlic
    override val resource = Res.drawable.garlic_strip
    override val price: Int = 150
    override val unlockCost: ItemCost = GamePrices.UNLOCK_GARLIC
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🧄"
    override val tutorialRes = Res.string.tutorial_garlic

    override val baseRewards: List<Reward> get() = listOf(
        Reward(emoji = particleEmoji, countValue = 1, resource = resource),
        Reward(emoji = "🪙", moneyValue = GamePrices.REWARD_MONEY_GARLIC, countValue = 0)
    )

    override val modifiers: List<GameplayModifier> = listOf(
        GameplayModifier(
            id = "garlic_cluster",
            nameRes = Res.string.mod_garlic_cluster_name,
            descriptionRes = Res.string.mod_garlic_cluster_desc,
            unlockCost = GamePrices.MOD_GARLIC_CLUSTER,
            targetItemId = "garlic"
        ),
        GameplayModifier(
            id = "garlic_shake",
            nameRes = Res.string.mod_garlic_shake_name,
            descriptionRes = Res.string.mod_garlic_shake_desc,
            unlockCost = GamePrices.MOD_GARLIC_SHAKE,
            targetItemId = "garlic"
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
        val scale = remember { Animatable(1f) }
        var clickCount by remember { mutableStateOf(0) }
        var isExploded by remember { mutableStateOf(false) }
        val pieces = remember { mutableStateListOf<GarlicPiece>() }
        val flyingParticles = remember { mutableStateListOf<FlyingParticle>() }

        val isClusterActive = activeModifiers.any { it.id == "garlic_cluster" && it.isEnabled }
        val isShakeActive = activeModifiers.any { it.id == "garlic_shake" && it.isEnabled }

        // Visual vibration effect that increases as clickCount approach 10
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

        /**
         * Collects garlic cloves and handles the final reward logic once all pieces are gone.
         * @param count Number of pieces to collect (used for shake).
         * @param pieceToCollect Specific piece if clicked manually.
         */
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
                
                // When the last piece is collected, grant the big explosion reward
                if (pieces.none { it.isVisible }) {
                    val multiplier = if (isClusterActive) GamePrices.MULTIPLIER_GARLIC_CLUSTER else 1.0f
                    val bonusRewards = listOf(
                        Reward(emoji = "🪙", moneyValue = (GamePrices.REWARD_GARLIC_EXPLOSION_MONEY * multiplier).toInt(), countValue = 0),
                        Reward(emoji = particleEmoji, countValue = (GamePrices.REWARD_GARLIC_EXPLOSION_COUNT * multiplier).toInt(), resource = resource)
                    )
                    
                    val finalRewards = onVegetableClick(bonusRewards)
                    
                    val captureX = piece.animatableX.value
                    val captureY = piece.animatableY.value
                    
                    val newOnes = createRewardParticles(
                        rewards = finalRewards,
                        offsetX = captureX,
                        offsetY = captureY
                    )
                    
                    val activeCount = flyingParticles.count { !it.isManuallyRemoved }
                    val overflow = (activeCount + newOnes.size) - 20
                    if (overflow > 0) {
                        flyingParticles.filter { !it.isManuallyRemoved }.take(overflow).forEach { it.isManuallyRemoved = true }
                    }
                    flyingParticles.addAll(newOnes)
                    
                    // Reset state
                    isExploded = false
                    clickCount = 0
                    pieces.clear()
                }
            }
        }

        // Setup accelerometer listener for "Shake to Harvest"
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
                // Render main Garlic bulb
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
                                    // Trigger explosion
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
                                    // Normal click reward
                                    val finalRewards = onVegetableClick(baseRewards)
                                    scope.launch {
                                        scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                        scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                    }
                                    
                                    val newOnes = createRewardParticles(finalRewards)
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
                // Render scattered cloves
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
