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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.ui.SpriteAnimation
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.bellpepper_strip
import ecogardengame.composeapp.generated.resources.item_bell_pepper
import ecogardengame.composeapp.generated.resources.tutorial_bell_pepper
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.*
import kotlin.random.Random

/**
 * --- GAMEPLAY MECHANIC: BURST MOTION ---
 * The Bell Pepper moves in "bursts". It remains stationary for a short period,
 * then suddenly dashes in a random direction before stopping again.
 * This tests the player's reaction time and "sniping" skills.
 *
 * --- OOP PRINCIPLES ---
 * - INHERITANCE: Inherits core vegetable properties from 'BaseVegetable'.
 * - ENCAPSULATION: The logic for calculating random directions and phase
 *   durations is kept private within the UI loop.
 */
class BellPepper : BaseVegetable() {
    override val id: String = "bell_pepper"
    override val nameRes = Res.string.item_bell_pepper
    override val resource = Res.drawable.bellpepper_strip
    override val price: Int = 100
    override val unlockCost: ItemCost = GamePrices.UNLOCK_BELL_PEPPER
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🫑"
    override val tutorialRes = Res.string.tutorial_bell_pepper

    override val baseRewards: List<Reward> get() =
        listOf(
            Reward(emoji = particleEmoji, countValue = 1, resource = resource),
            Reward(emoji = "🪙", moneyValue = GamePrices.REWARD_MONEY_BELL_PEPPER, countValue = 0),
        )

    override val modifiers: List<GameplayModifier> =
        listOf(
            GameplayModifier(
                id = "bell_pepper_turbo",
                nameRes = Res.string.mod_bell_pepper_turbo_name,
                descriptionRes = Res.string.mod_bell_pepper_turbo_desc,
                unlockCost = GamePrices.MOD_BELL_PEPPER_TURBO,
                targetItemId = "bell_pepper",
            ),
        )

    /**
     * The Bell Pepper's UI implementation.
     * Manages a state machine for movement phases (Stationary vs Moving).
     */
    @Composable
    override fun Content(
        modifier: Modifier,
        onVegetableClick: (List<Reward>) -> List<Reward>,
        activeModifiers: List<GameplayModifier>,
        vibrationEnabled: Boolean,
        vibrationIntensity: Float,
    ) {
        val scope = rememberCoroutineScope()
        val scale = remember { Animatable(1f) }
        val flyingParticles = remember { mutableStateListOf<FlyingParticle>() }

        // --- MOTION STATE ---
        var posX by remember { mutableStateOf(0f) }
        var posY by remember { mutableStateOf(0f) }
        var dirX by remember { mutableStateOf(0f) }
        var dirY by remember { mutableStateOf(0f) }

        // Machine state: true = dashing, false = resting.
        var isMoving by remember { mutableStateOf(false) }
        var phaseStartTime by remember { mutableStateOf(0L) }
        var parentWidth by remember { mutableStateOf(0f) }
        var parentHeight by remember { mutableStateOf(0f) }

        val isTurbo = activeModifiers.any { it.id == "bell_pepper_turbo" && it.isEnabled }

        val itemSize = 120.dp
        val itemSizePx = with(LocalDensity.current) { itemSize.toPx() }

        // --- PHASE DURATIONS ---
        // Modifiers change the base rhythm of the logic.
        val stationaryDuration = if (isTurbo) 500L else 1000L
        val movingDuration = if (isTurbo) 1000L else 2000L
        val maxBaseSpeed = 100f

        /**
         * --- MOTION LOGIC LOOP (Coroutines) ---
         * Handles the switching between "Stationary" and "Moving" states.
         */
        LaunchedEffect(parentWidth, parentHeight, isTurbo) {
            if (parentWidth > 0 && parentHeight > 0) {
                // Initialize a random starting direction using trigonometry.
                val angle = Random.nextFloat() * 2 * PI.toFloat()
                dirX = cos(angle)
                dirY = sin(angle)

                while (true) {
                    withFrameMillis { frameTime ->
                        if (phaseStartTime == 0L) phaseStartTime = frameTime
                        val elapsed = frameTime - phaseStartTime

                        if (!isMoving) {
                            // Phase: STATIONARY
                            if (elapsed >= stationaryDuration) {
                                isMoving = true
                                phaseStartTime = frameTime
                                // Pick a new random direction for the next dash.
                                val newAngle = Random.nextFloat() * 2 * PI.toFloat()
                                dirX = cos(newAngle)
                                dirY = sin(newAngle)
                            }
                        } else {
                            // Phase: MOVING (Dashing)
                            if (elapsed >= movingDuration) {
                                isMoving = false
                                phaseStartTime = frameTime
                            } else {
                                // Calculate speed using a sine curve for a smooth dash effect.
                                val progress = elapsed.toFloat() / movingDuration
                                val speedFactor = sin(progress * PI.toFloat())
                                val speed = speedFactor * maxBaseSpeed
                                posX += dirX * speed
                                posY += dirY * speed

                                val limitX = (parentWidth - itemSizePx) / 2
                                val limitY = (parentHeight - itemSizePx) / 2

                                // Collision & Bounce Logic
                                if (abs(posX) >= limitX) {
                                    dirX = -dirX // Flip horizontal direction
                                    posX = posX.coerceIn(-limitX, limitX)
                                }
                                if (abs(posY) >= limitY) {
                                    dirY = -dirY // Flip vertical direction
                                    posY = posY.coerceIn(-limitY, limitY)
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .onGloballyPositioned {
                        parentWidth = it.size.width.toFloat()
                        parentHeight = it.size.height.toFloat()
                    },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                        .size(itemSize),
                contentAlignment = Alignment.Center,
            ) {
                SpriteAnimation(
                    painter = painterResource(resource),
                    frameCount = 3,
                    modifier =
                        modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                val currentX = posX
                                val currentY = posY

                                // Apply turbo multiplier if the modifier is active.
                                val rewards =
                                    if (isTurbo) {
                                        baseRewards.map {
                                            it.copy(
                                                moneyValue = it.moneyValue * GamePrices.MULTIPLIER_BELL_PEPPER_TURBO,
                                                countValue = it.countValue * GamePrices.MULTIPLIER_BELL_PEPPER_TURBO,
                                            )
                                        }
                                    } else {
                                        baseRewards
                                    }

                                // Trigger global logic in ViewModel.
                                val finalRewards = onVegetableClick(rewards)

                                // Visual feedback animation.
                                scope.launch {
                                    scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                    scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                }

                                // Create reward particles at the current position.
                                val newOnes =
                                    createRewardParticles(
                                        rewards = finalRewards,
                                        offsetX = currentX,
                                        offsetY = currentY,
                                    )

                                // Particle lifecycle management.
                                val activeCount = flyingParticles.count { !it.isManuallyRemoved }
                                val overflow = (activeCount + newOnes.size) - 20
                                if (overflow > 0) {
                                    flyingParticles.filter { !it.isManuallyRemoved }
                                        .take(overflow)
                                        .forEach { it.isManuallyRemoved = true }
                                }
                                flyingParticles.addAll(newOnes)
                            },
                )
            }

            // Layer for flying reward particles.
            ParticleEffect(flyingParticles)
        }
    }
}
