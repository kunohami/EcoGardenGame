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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.ui.SpriteAnimation
import com.rafarg.ecogardengame.util.startListeningForProximity
import com.rafarg.ecogardengame.util.stopListeningForProximity
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.broccoli_strip
import ecogardengame.composeapp.generated.resources.item_broccoli
import ecogardengame.composeapp.generated.resources.tutorial_broccoli
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

/**
 * --- GAMEPLAY MECHANIC: MOTION & PROXIMITY ---
 * The Broccoli is a moving target. It bounces around the screen, requiring the player
 * to track it visually. It also introduces the "Proximity Sensor" mechanic.
 *
 * --- OOP PRINCIPLES ---
 * - INHERITANCE: Inherits from 'BaseVegetable' to leverage common reward/particle logic.
 * - POLYMORPHISM: Implements the 'Content' composable with specialized motion logic.
 * - ENCAPSULATION: Hides the complexity of physics (velocity, bouncing) within the class.
 */
class Broccoli : BaseVegetable() {
    override val id: String = "broccoli"
    override val nameRes = Res.string.item_broccoli
    override val resource = Res.drawable.broccoli_strip
    override val price: Int = 50
    override val unlockCost: ItemCost = GamePrices.UNLOCK_BROCCOLI
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🥦"
    override val tutorialRes = Res.string.tutorial_broccoli

    override val baseRewards: List<Reward> get() = listOf(
        Reward(emoji = particleEmoji, countValue = 1, resource = resource),
        Reward(emoji = "🪙", moneyValue = GamePrices.REWARD_MONEY_BROCCOLI, countValue = 0)
    )

    override val modifiers: List<GameplayModifier> = listOf(
        GameplayModifier(
            id = "broccoli_giant",
            nameRes = Res.string.mod_broccoli_giant_name,
            descriptionRes = Res.string.mod_broccoli_giant_desc,
            unlockCost = GamePrices.MOD_BROCCOLI_GIANT,
            targetItemId = "broccoli"
        ),
        GameplayModifier(
            id = "broccoli_speed",
            nameRes = Res.string.mod_broccoli_overclocked_name,
            descriptionRes = Res.string.mod_broccoli_overclocked_desc,
            unlockCost = GamePrices.MOD_BROCCOLI_OVERCLOCKED,
            targetItemId = "broccoli"
        ),
        GameplayModifier(
            id = "broccoli_proximity",
            nameRes = Res.string.mod_broccoli_air_name,
            descriptionRes = Res.string.mod_broccoli_air_desc,
            unlockCost = GamePrices.MOD_BROCCOLI_AIR,
            targetItemId = "broccoli"
        )
    )

    /**
     * The Broccoli's UI implementation.
     * Manages a 2D physics simulation for movement and sensor interaction.
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
        val flyingParticles = remember { mutableStateListOf<FlyingParticle>() }
        
        // --- MOTION STATE ---
        // We use Float values for precise sub-pixel movement.
        var posX by remember { mutableStateOf(0f) }
        var posY by remember { mutableStateOf(0f) }
        // Velocity: pixels moved per frame.
        var baseVelX by remember { mutableStateOf(4f) }
        var baseVelY by remember { mutableStateOf(4f) }

        // Screen dimensions tracked via 'onGloballyPositioned'.
        var parentWidth by remember { mutableStateOf(0f) }
        var parentHeight by remember { mutableStateOf(0f) }
        
        // Check active modifiers to adjust gameplay parameters.
        val isGiant = activeModifiers.any { it.id == "broccoli_giant" && it.isEnabled }
        val isFast = activeModifiers.any { it.id == "broccoli_speed" && it.isEnabled }
        val isAirHarvest = activeModifiers.any { it.id == "broccoli_proximity" && it.isEnabled }
        
        val itemSize = if (isGiant) 200.dp else 100.dp
        val itemSizePx = with(LocalDensity.current) { itemSize.toPx() }

        // Modifiers directly affect the physics and reward math.
        val speedMultiplier = if (isFast) GamePrices.MULTIPLIER_BROCCOLI_FAST.toFloat() else 1f
        val rewardMultiplier = if (isFast) GamePrices.MULTIPLIER_BROCCOLI_FAST else 1

        var clickCounter by remember { mutableStateOf(0) }

        /**
         * INTERACTION HANDLER
         * Shared logic for both manual clicks and proximity triggers.
         * This avoids code duplication (DRY Principle).
         */
        val handleInteraction = {
            val currentX = posX
            val currentY = posY
            
            val rewardsToGive = mutableListOf<Reward>()
            
            if (isGiant) {
                // Giant Broccoli mechanic: requires 2 "hits" to generate a reward.
                clickCounter++
                if (clickCounter >= 2) {
                    clickCounter = 0
                    rewardsToGive.addAll(baseRewards)
                }
            } else {
                rewardsToGive.addAll(baseRewards)
            }

            if (rewardsToGive.isNotEmpty()) {
                // Apply modifiers to the reward values before sending to ViewModel.
                val basePlusMultipliers = if (rewardMultiplier > 1) {
                    rewardsToGive.map { it.copy(
                        moneyValue = it.moneyValue * rewardMultiplier,
                        countValue = it.countValue * rewardMultiplier
                    ) }
                } else {
                    rewardsToGive
                }

                // Execute the global economy logic in the ViewModel.
                val finalRewards = onVegetableClick(basePlusMultipliers)
                
                // Visual feedback: Create particles at the Broccoli's CURRENT moving position.
                val newOnes = createRewardParticles(
                    rewards = finalRewards,
                    offsetX = currentX,
                    offsetY = currentY
                )
                
                // Particle pool management.
                val activeCount = flyingParticles.count { !it.isManuallyRemoved }
                val overflow = (activeCount + newOnes.size) - 20
                if (overflow > 0) {
                    flyingParticles.filter { !it.isManuallyRemoved }.take(overflow).forEach { it.isManuallyRemoved = true }
                }
                flyingParticles.addAll(newOnes)
            }
            
            // Squash and stretch animation effect.
            scope.launch {
                scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
            }
        }

        /**
         * --- PHYSICS LOOP (Coroutines) ---
         * Continuously updates the Broccoli's position based on velocity.
         * Detects collisions with the screen edges and flips the velocity vector (Bouncing).
         */
        LaunchedEffect(parentWidth, parentHeight, speedMultiplier) {
            if (parentWidth > 0 && parentHeight > 0) {
                while (true) {
                    withFrameMillis { _ ->
                        posX += baseVelX * speedMultiplier
                        posY += baseVelY * speedMultiplier

                        val limitX = (parentWidth - itemSizePx) / 2
                        val limitY = (parentHeight - itemSizePx) / 2

                        // Collision detection: Left/Right walls
                        if (posX <= -limitX || posX >= limitX) {
                            baseVelX *= -1
                            posX = posX.coerceIn(-limitX, limitX)
                        }
                        // Collision detection: Top/Bottom walls
                        if (posY <= -limitY || posY >= limitY) {
                            baseVelY *= -1
                            posY = posY.coerceIn(-limitY, limitY)
                        }
                    }
                }
            }
        }

        /**
         * --- SENSOR MANAGEMENT (DisposableEffect) ---
         * Manages the lifecycle of the proximity sensor for "Air Harvest".
         * Sensors must be properly shut down to prevent battery drain.
         */
        DisposableEffect(isAirHarvest) {
            if (isAirHarvest) {
                startListeningForProximity {
                    // Sensor triggered! We call the same interaction handler as a click.
                    handleInteraction()
                }
            }
            onDispose {
                // Critical: Stop hardware listener when the component is removed from the screen.
                stopListeningForProximity()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    // Update parent dimensions when the layout is drawn.
                    parentWidth = it.size.width.toFloat()
                    parentHeight = it.size.height.toFloat()
                },
            contentAlignment = Alignment.Center
        ) {
            // Render the Broccoli at its calculated animated position.
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
                            handleInteraction()
                        }
                )
            }

            // Layer for flying reward particles.
            ParticleEffect(flyingParticles)
        }
    }
}
