package com.rafarg.ecogardengame.model

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
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
 * Demonstrates the use of individual Animatable properties for complex visual effects.
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
 * --- GAMEPLAY MECHANIC: MULTI-PHASE / STATE MACHINE ---
 * The Garlic introduces a "Two-Phase" interaction. 
 * Phase 1: Tap the main bulb 10 times to "charge" it.
 * Phase 2: The bulb explodes into cloves. Collect all cloves to get a large reward.
 *
 * --- OOP PRINCIPLES ---
 * - ENCAPSULATION: Internal state variables (clickCount, isExploded) are managed 
 *   within the UI layer to maintain a clean interface.
 * - COMPOSITION: Uses a list of 'GarlicPiece' objects to manage the explosion logic.
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

    /**
     * The Garlic's UI implementation.
     * Switches between two distinct rendering modes based on 'isExploded'.
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
        val scale = remember { Animatable(1f) }
        
        // --- INTERNAL STATE ---
        var clickCount by remember { mutableStateOf(0) }
        var isExploded by remember { mutableStateOf(false) }
        val pieces = remember { mutableStateListOf<GarlicPiece>() }
        val flyingParticles = remember { mutableStateListOf<FlyingParticle>() }

        val isClusterActive = activeModifiers.any { it.id == "garlic_cluster" && it.isEnabled }
        val isShakeActive = activeModifiers.any { it.id == "garlic_shake" && it.isEnabled }

        // --- VISUAL FEEDBACK (Vibration Animation) ---
        // As the clickCount increases, the bulb shakes more intensely.
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
         * COLLECTION LOGIC
         * Handles the removal of garlic cloves and triggers the final big reward.
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
                
                // When the final piece is collected, grant the bonus reward.
                if (pieces.none { it.isVisible }) {
                    val multiplier = if (isClusterActive) GamePrices.MULTIPLIER_GARLIC_CLUSTER else 1.0f
                    val bonusRewards = listOf(
                        Reward(emoji = "🪙", moneyValue = (GamePrices.REWARD_GARLIC_EXPLOSION_MONEY * multiplier).toInt(), countValue = 0),
                        Reward(emoji = particleEmoji, countValue = (GamePrices.REWARD_GARLIC_EXPLOSION_COUNT * multiplier).toInt(), resource = resource)
                    )
                    
                    // Call the ViewModel to process the final big reward.
                    val finalRewards = onVegetableClick(bonusRewards)
                    
                    // Create reward particles at the last clove's position.
                    val captureX = piece.animatableX.value
                    val captureY = piece.animatableY.value
                    val newOnes = createRewardParticles(finalRewards, captureX, captureY)
                    
                    flyingParticles.addAll(newOnes)
                    
                    // Reset to Phase 1.
                    isExploded = false
                    clickCount = 0
                    pieces.clear()
                }
            }
        }

        /**
         * --- SENSOR INTERACTION (Accelerometer) ---
         * If "Shake to Harvest" is active, we listen for phone movement.
         */
        DisposableEffect(isExploded, isShakeActive) {
            if (isExploded && isShakeActive) {
                startListeningForShake {
                    // Shake detected! Automatically collect up to 5 pieces at once.
                    collectPieces(5)
                }
            }
            onDispose {
                stopListeningForShake()
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (!isExploded) {
                // --- PHASE 1: THE MAIN BULB ---
                Box(contentAlignment = Alignment.Center) {
                    SpriteAnimation(
                        painter = painterResource(resource),
                        frameCount = 3,
                        modifier = modifier
                            .size(200.dp)
                            .offset { IntOffset(if (clickCount > 0) vibX.roundToInt() else 0, 0) }
                            .graphicsLayer {
                                // The bulb grows as it gets closer to exploding.
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
                                    // --- TRIGGER EXPLOSION ---
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
                                        // Animate pieces flying outward.
                                        scope.launch { piece.animatableX.animateTo(targetX, tween(600, easing = EaseOutBack)) }
                                        scope.launch { piece.animatableY.animateTo(targetY, tween(600, easing = EaseOutBack)) }
                                    }
                                    isExploded = true
                                } else {
                                    // Standard click reward during Phase 1.
                                    val finalRewards = onVegetableClick(baseRewards)
                                    scope.launch {
                                        scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                        scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                    }
                                    val newOnes = createRewardParticles(finalRewards)
                                    flyingParticles.addAll(newOnes)
                                }
                            }
                    )
                }
            } else {
                // --- PHASE 2: THE CLOVES ---
                // Iterates through the list of generated pieces and renders them.
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
            
            // Render the particles layer.
            ParticleEffect(flyingParticles)
        }
    }
}
