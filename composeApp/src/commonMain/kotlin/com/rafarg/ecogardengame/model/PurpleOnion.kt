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
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.purpleonion_strip
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

class PurpleOnion : BaseVegetable() {
    override val id: String = "purple_onion"
    override val name: String = "Purple Onion"
    override val resource = Res.drawable.purpleonion_strip
    override val price: Int = 200
    override val unlockCost: ItemCost = ItemCost(
        money = 500,
        vegetableCosts = mapOf("tomato" to 100, "broccoli" to 25, "bell_pepper" to 10)
    )
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🧅"
    override val tutorialText: String = "The purple onion teleports! Every time it appears, it starts a quick rotation. If you catch it while it's spinning, you get a significant coin bonus! Buying more onions will increase your chances, but reduce the individual bonus slightly."

    // Base reward when NOT spinning: 1 money, 1 onion
    override val baseRewards: List<Reward> get() = listOf(
        Reward(emoji = particleEmoji, countValue = 1, resource = resource),
        Reward(emoji = "🪙", moneyValue = 1, countValue = 0)
    )

    override val modifiers: List<GameplayModifier> = listOf(
        GameplayModifier(
            id = "purple_onion_plus_1",
            name = "+1 Onion",
            description = "One extra onion appears, but bonus coins are reduced by 1.",
            unlockCost = ItemCost(money = 1000, vegetableCosts = mapOf("purple_onion" to 50)),
            targetItemId = "purple_onion"
        ),
        GameplayModifier(
            id = "purple_onion_plus_2",
            name = "+1 Onion II",
            description = "A third onion appears, bonus coins reduced further.",
            unlockCost = ItemCost(money = 2500, vegetableCosts = mapOf("purple_onion" to 150, "bell_pepper" to 30)),
            targetItemId = "purple_onion"
        ),
        GameplayModifier(
            id = "purple_onion_long_spin",
            name = "Sturdy Roots",
            description = "Onions rotate for longer, extending the bonus window.",
            unlockCost = ItemCost(money = 1500, vegetableCosts = mapOf("purple_onion" to 100, "broccoli" to 40)),
            targetItemId = "purple_onion"
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
        val hasPlus1 = activeModifiers.any { it.id == "purple_onion_plus_1" && it.isEnabled }
        val hasPlus2 = activeModifiers.any { it.id == "purple_onion_plus_2" && it.isEnabled }
        val hasLongSpin = activeModifiers.any { it.id == "purple_onion_long_spin" && it.isEnabled }
        
        val onionCount = when {
            hasPlus2 -> 3
            hasPlus1 -> 2
            else -> 1
        }
        
        val coinReduction = when {
            hasPlus2 -> 2
            hasPlus1 -> 1
            else -> 0
        }

        val spinDuration = if (hasLongSpin) 700 else 350
        
        var parentWidth by remember { mutableStateOf(0f) }
        var parentHeight by remember { mutableStateOf(0f) }
        val density = LocalDensity.current
        val itemSize = 130.dp
        val itemSizePx = with(density) { itemSize.toPx() }

        val onionPositions = remember { mutableStateListOf<Pair<Float, Float>>() }
        if (onionPositions.size != onionCount) {
            onionPositions.clear()
            repeat(onionCount) { onionPositions.add(0f to 0f) }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    parentWidth = it.size.width.toFloat()
                    parentHeight = it.size.height.toFloat()
                },
            contentAlignment = Alignment.Center
        ) {
            if (parentWidth > 0 && parentHeight > 0) {
                repeat(onionCount) { index ->
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
                            otherOnions = onionPositions.filterIndexed { i, _ -> i != index }
                        )
                    }
                }
            }
        }
    }

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
        otherOnions: List<Pair<Float, Float>>
    ) {
        val scope = rememberCoroutineScope()
        val alpha = remember { Animatable(1f) }
        val rotation = remember { Animatable(0f) }
        val scale = remember { Animatable(1f) }
        val flyingParticles = remember { mutableStateListOf<FlyingParticle>() }
        var canClick by remember { mutableStateOf(true) }

        var posX by remember { mutableStateOf(0f) }
        var posY by remember { mutableStateOf(0f) }

        suspend fun teleport() {
            canClick = false
            alpha.animateTo(0f, tween(100))
            rotation.snapTo(0f)
            
            val limitX = (parentWidth - itemSizePx) / 2
            val limitY = (parentHeight - itemSizePx) / 2
            
            var newX: Float
            var newY: Float
            var attempts = 0
            val minDistance = itemSizePx * 1.2f
            
            do {
                newX = Random.nextFloat() * (limitX * 2) - limitX
                newY = Random.nextFloat() * (limitY * 2) - limitY
                val overlaps = otherOnions.any { other ->
                    sqrt((newX - other.first).pow(2) + (newY - other.second).pow(2)) < minDistance
                }
                attempts++
            } while (overlaps && attempts < 15)

            posX = newX
            posY = newY
            onPositionChanged(posX to posY)
            
            alpha.animateTo(1f, tween(100))
            canClick = true
            
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(spinDuration, easing = LinearEasing)
            )
        }

        LaunchedEffect(parentWidth, parentHeight) {
            teleport()
        }

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                    .size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                SpriteAnimation(
                    painter = painterResource(resource),
                    frameCount = 3,
                    modifier = modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            this.alpha = alpha.value
                            rotationZ = rotation.value
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = canClick && alpha.value > 0.8f
                        ) {
                            if (!canClick) return@clickable
                            canClick = false

                            val isRotating = rotation.value > 0 && rotation.value < 360
                            var rewards = if (isRotating) {
                                listOf(
                                    Reward(emoji = particleEmoji, countValue = 1, resource = resource),
                                    Reward(emoji = "🪙", moneyValue = (5 - coinReduction).coerceAtLeast(1), countValue = 0)
                                )
                            } else {
                                baseRewards
                            }

                            val finalRewards = onVegetableClick(rewards)
                            
                            val captureX = posX
                            val captureY = posY

                            scope.launch {
                                scale.animateTo(0.8f, spring())
                                scale.animateTo(1f, spring())
                                teleport()
                            }

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
                        }
                )
            }

            ParticleEffect(flyingParticles)
        }
    }
}
