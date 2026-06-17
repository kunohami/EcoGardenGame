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
import ecogardengame.composeapp.generated.resources.item_purple_onion
import ecogardengame.composeapp.generated.resources.purpleonion_strip
import ecogardengame.composeapp.generated.resources.tutorial_purple_onion
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * --- GAMEPLAY MECHANIC: TELEPORTATION & SPINNING ---
 * The Purple Onion moves by "Teleporting" to random locations.
 * Upon appearing, it performs a quick 360-degree rotation.
 * Clicking it DURING this rotation grants a high coin bonus.
 *
 * --- OOP PRINCIPLES (OBJECT ORIENTED PROGRAMMING) ---
 * - INHERITANCE: Inherits from 'BaseVegetable' to reuse reward calculation and particle logic.
 * - POLYMORPHISM: Overrides 'Content' to provide its unique multi-instance teleporting interface.
 * - ENCAPSULATION: Animation states and teleportation logic are hidden within private components.
 * - COMPOSITION: Manages a list of modifiers and coordinates multiple 'SingleOnion' objects.
 */
class PurpleOnion : BaseVegetable() {
    // --- PROPERTIES (State & Identity) ---
    // These 'override' keywords show that we are fulfilling the contract defined by the 'GameItem' interface.
    override val id: String = "purple_onion"
    override val nameRes = Res.string.item_purple_onion
    override val resource = Res.drawable.purpleonion_strip
    override val price: Int = 200
    override val unlockCost: ItemCost = GamePrices.UNLOCK_PURPLE_ONION
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🧅"
    override val tutorialRes = Res.string.tutorial_purple_onion

    /**
     * Default rewards when clicking normally.
     * In Kotlin, 'get()' defines a custom getter, meaning this list is recreated or calculated
     * whenever it is accessed.
     */
    override val baseRewards: List<Reward> get() =
        listOf(
            Reward(emoji = particleEmoji, countValue = 1, resource = resource),
            Reward(emoji = "🪙", moneyValue = GamePrices.REWARD_MONEY_PURPLE_ONION, countValue = 0),
        )

    /**
     * List of modifiers specific to this vegetable.
     * Demonstrates COMPOSITION: The vegetable "has a" set of modifiers that change its behavior.
     */
    override val modifiers: List<GameplayModifier> =
        listOf(
            GameplayModifier(
                id = "purple_onion_plus_1",
                nameRes = Res.string.mod_onion_plus1_name,
                descriptionRes = Res.string.mod_onion_plus1_desc,
                unlockCost = GamePrices.MOD_ONION_PLUS1,
                targetItemId = "purple_onion",
            ),
            GameplayModifier(
                id = "purple_onion_plus_2",
                nameRes = Res.string.mod_onion_plus2_name,
                descriptionRes = Res.string.mod_onion_plus2_desc,
                unlockCost = GamePrices.MOD_ONION_PLUS2,
                targetItemId = "purple_onion",
            ),
            GameplayModifier(
                id = "purple_onion_long_spin",
                nameRes = Res.string.mod_onion_sturdy_name,
                descriptionRes = Res.string.mod_onion_sturdy_desc,
                unlockCost = GamePrices.MOD_ONION_STURDY,
                targetItemId = "purple_onion",
            ),
        )

    /**
     * --- UI ENTRY POINT (Compose) ---
     * This method renders the vegetable in the Garden.
     * It handles the logic for having multiple onions on screen if modifiers are enabled.
     *
     * @param onVegetableClick A lambda (function passed as parameter) that communicates with the ViewModel.
     */
    @Composable
    override fun Content(
        modifier: Modifier,
        onVegetableClick: (List<Reward>) -> List<Reward>,
        activeModifiers: List<GameplayModifier>,
        vibrationEnabled: Boolean,
        vibrationIntensity: Float,
    ) {
        // --- MODIFIER DETECTION ---
        // We check which modifiers are unlocked AND enabled by the user.
        val hasPlus1 = activeModifiers.any { it.id == "purple_onion_plus_1" && it.isEnabled }
        val hasPlus2 = activeModifiers.any { it.id == "purple_onion_plus_2" && it.isEnabled }
        val hasLongSpin = activeModifiers.any { it.id == "purple_onion_long_spin" && it.isEnabled }

        // Progression logic: +1 Onion adds one, +1 Onion II adds a second one (total 3).
        val onionCount =
            when {
                hasPlus2 -> 3
                hasPlus1 -> 2
                else -> 1
            }

        // Economic Balance: More onions mean more chances, so the individual bonus is slightly reduced.
        val coinReduction =
            when {
                hasPlus2 -> 2
                hasPlus1 -> 1
                else -> 0
            }

        // Gameplay Tuning: 'Sturdy Roots' doubles the duration of the bonus spin window.
        val spinDuration = if (hasLongSpin) 700 else 350

        // --- LAYOUT MEASUREMENT ---
        // 'remember' keeps variables alive across recompositions (UI updates).
        // 'mutableStateOf' tells Compose to watch this variable for changes.
        var parentWidth by remember { mutableStateOf(0f) }
        var parentHeight by remember { mutableStateOf(0f) }

        // Density is needed to convert between 'dp' (design units) and 'pixels' (screen units).
        val density = LocalDensity.current
        val itemSize = 130.dp
        val itemSizePx = with(density) { itemSize.toPx() }

        /**
         * --- SHARED STATE (mutableStateListOf) ---
         * We store the positions of all active onions in a list.
         * This allows individual onions to "see" where others are and avoid overlapping.
         */
        val onionPositions = remember { mutableStateListOf<Pair<Float, Float>>() }
        // Ensure the list has the correct size for the current number of onions.
        if (onionPositions.size != onionCount) {
            onionPositions.clear()
            repeat(onionCount) { onionPositions.add(0f to 0f) }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .onGloballyPositioned {
                        // This modifier runs after the element is placed on screen, giving us its size.
                        parentWidth = it.size.width.toFloat()
                        parentHeight = it.size.height.toFloat()
                    },
            contentAlignment = Alignment.Center,
        ) {
            if (parentWidth > 0 && parentHeight > 0) {
                // Instantiate multiple onions based on the 'onionCount' logic.
                repeat(onionCount) { index ->
                    // 'key' is crucial in Compose loops. It helps the system identify which
                    // state belongs to which object, preventing visual glitches.
                    key(index) {
                        SingleOnion(
                            modifier = modifier,
                            onVegetableClick = onVegetableClick,
                            coinReduction = coinReduction,
                            spinDuration = spinDuration,
                            baseRewards = baseRewards,
                            parentWidth = parentWidth,
                            parentHeight = parentHeight,
                            itemSizePx = itemSizePx,
                            onPositionChanged = { newPos ->
                                if (index < onionPositions.size) {
                                    onionPositions[index] = newPos
                                }
                            },
                            // Pass other onions' positions for collision avoidance logic.
                            otherOnions = onionPositions.filterIndexed { i, _ -> i != index },
                        )
                    }
                }
            }
        }
    }

    /**
     * --- PRIVATE COMPOSABLE (Encapsulation) ---
     * Represents a single independent onion instance.
     * Manages its own internal animations (fade, spin, scale) and teleportation cycle.
     */
    @Composable
    private fun SingleOnion(
        modifier: Modifier,
        onVegetableClick: (List<Reward>) -> List<Reward>,
        coinReduction: Int,
        spinDuration: Int,
        baseRewards: List<Reward>,
        parentWidth: Float,
        parentHeight: Float,
        itemSizePx: Float,
        onPositionChanged: (Pair<Float, Float>) -> Unit,
        otherOnions: List<Pair<Float, Float>>,
    ) {
        // 'rememberCoroutineScope' allows us to launch background tasks (animations) from the UI.
        val scope = rememberCoroutineScope()

        // --- ANIMATION CONTROLLERS (Animatable) ---
        // Animatable provides fine-grained control over values that change over time.
        val alpha = remember { Animatable(1f) }
        val rotation = remember { Animatable(0f) }
        val scale = remember { Animatable(1f) }

        // Local list of particles for this onion.
        val flyingParticles = remember { mutableStateListOf<FlyingParticle>() }

        // Boolean flag to prevent multiple clicks while the onion is busy teleporting.
        var canClick by remember { mutableStateOf(true) }

        var posX by remember { mutableStateOf(0f) }
        var posY by remember { mutableStateOf(0f) }

        /**
         * --- TELEPORTATION CYCLE (Coroutines & Suspend Functions) ---
         * 'suspend' means this function can "pause" its execution without freezing the app
         * while it waits for an animation to finish.
         */
        suspend fun teleport() {
            canClick = false
            // 1. Fade out the onion. 'animateTo' waits until the animation is done.
            alpha.animateTo(0f, tween(100))
            rotation.snapTo(0f) // Instantly reset rotation for the next spot

            // Calculate random bounds based on screen size.
            val limitX = (parentWidth - itemSizePx) / 2
            val limitY = (parentHeight - itemSizePx) / 2

            var newX: Float
            var newY: Float
            var attempts = 0
            val minDistance = itemSizePx * 1.2f // Threshold to prevent onions from touching

            // --- COLLISION AVOIDANCE ALGORITHM ---
            // A simple loop that picks random spots until one is "safe" (not overlapping).
            do {
                newX = Random.nextFloat() * (limitX * 2) - limitX
                newY = Random.nextFloat() * (limitY * 2) - limitY
                val overlaps =
                    otherOnions.any { other ->
                        // Distance formula (Euclidean): sqrt((x2-x1)^2 + (y2-y1)^2)
                        sqrt((newX - other.first).pow(2) + (newY - other.second).pow(2)) < minDistance
                    }
                attempts++
            } while (overlaps && attempts < 15)

            // Update the state with the new position.
            posX = newX
            posY = newY
            onPositionChanged(posX to posY) // Notify the parent 'Content' about our new spot

            // 2. Fade in at the new position.
            alpha.animateTo(1f, tween(100))
            canClick = true

            // 3. Start the "Spin Window": rotating the image 360 degrees.
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(spinDuration, easing = LinearEasing),
            )
        }

        /**
         * --- SIDE EFFECTS (LaunchedEffect) ---
         * Used to start a process when the Composable first appears.
         * In this case, it triggers the initial teleportation sequence.
         */
        LaunchedEffect(parentWidth, parentHeight) {
            teleport()
        }

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier =
                    Modifier
                        .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                        .size(130.dp),
                contentAlignment = Alignment.Center,
            ) {
                SpriteAnimation(
                    painter = painterResource(resource),
                    frameCount = 3,
                    modifier =
                        modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                // Apply the animated values to the actual drawing layer.
                                this.alpha = alpha.value
                                rotationZ = rotation.value
                                scaleX = scale.value
                                scaleY = scale.value
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null, // Disable default gray ripple effect
                                enabled = canClick && alpha.value > 0.8f,
                            ) {
                                if (!canClick) return@clickable
                                canClick = false

                                /**
                                 * --- GAME LOGIC: TIMING CHECK ---
                                 * If 'rotation' is between 0 and 360, it means the animation is active.
                                 * This is how we detect if the user was fast enough to hit the bonus.
                                 */
                                val isRotating = rotation.value > 0 && rotation.value < 360
                                val rewards =
                                    if (isRotating) {
                                        // Caught it spinning! Grant the high coin bonus.
                                        listOf(
                                            Reward(emoji = particleEmoji, countValue = 1, resource = resource),
                                            Reward(
                                                emoji = "🪙",
                                                moneyValue = (GamePrices.REWARD_ONION_SPIN_MONEY_BASE - coinReduction).coerceAtLeast(1),
                                                countValue = 0,
                                            ),
                                        )
                                    } else {
                                        // Too slow or hit during the static phase.
                                        baseRewards
                                    }

                                // Pass the reward data to the ViewModel.
                                val finalRewards = onVegetableClick(rewards)

                                val captureX = posX
                                val captureY = posY

                                /**
                                 * --- SEQUENTIAL ANIMATIONS (launch) ---
                                 * scope.launch creates a new coroutine so the code runs
                                 * without stopping the UI.
                                 */
                                scope.launch {
                                    // Visual "click" effect (shrinking and growing back)
                                    scale.animateTo(0.8f, spring())
                                    scale.animateTo(1f, spring())
                                    // Teleport the onion away after being hit.
                                    teleport()
                                }

                                // --- VISUAL FEEDBACK (Particles) ---
                                val newOnes =
                                    createRewardParticles(
                                        rewards = finalRewards,
                                        offsetX = captureX,
                                        offsetY = captureY,
                                    )

                                // Keep the screen clean by limiting the number of active particles.
                                val activeCount = flyingParticles.count { !it.isManuallyRemoved }
                                val overflow = (activeCount + newOnes.size) - 20
                                if (overflow > 0) {
                                    flyingParticles.filter { !it.isManuallyRemoved }.take(overflow).forEach { it.isManuallyRemoved = true }
                                }
                                flyingParticles.addAll(newOnes)
                            },
                )
            }

            // Render reward particles for this specific instance of the onion.
            ParticleEffect(flyingParticles)
        }
    }
}
