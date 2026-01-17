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
import ecogardengame.composeapp.generated.resources.broccoli_strip
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt
import kotlin.random.Random

class Broccoli : BaseVegetable() {
    override val id: String = "broccoli"
    override val name: String = "Broccoli"
    override val resource = Res.drawable.broccoli_strip
    override val price: Int = 50
    override val unlockCost: ItemCost = ItemCost(
        money = 100,
        vegetableCosts = mapOf("tomato" to 20)
    )
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🥦"

    override val baseRewards: List<Reward> get() = listOf(
        Reward(emoji = particleEmoji, countValue = 1, resource = resource),
        Reward(emoji = "🪙", moneyValue = 2, countValue = 0)
    )

    override val modifiers: List<GameplayModifier> = listOf(
        GameplayModifier(
            id = "broccoli_giant",
            name = "Giant Broccoli",
            description = "Double size, but requires 2 clicks for reward.",
            unlockCost = ItemCost(money = 500, vegetableCosts = mapOf("broccoli" to 100)),
            targetItemId = "broccoli"
        )
    )

    @Composable
    override fun Content(
        modifier: Modifier,
        onVegetableClick: (List<Reward>) -> Unit,
        activeModifiers: List<GameplayModifier>
    ) {
        val scope = rememberCoroutineScope()
        val scale = remember { Animatable(1f) }
        val flyingParticles = remember { mutableStateListOf<FlyingParticle>() }
        
        var posX by remember { mutableStateOf(0f) }
        var posY by remember { mutableStateOf(0f) }
        var velX by remember { mutableStateOf(4f) }
        var velY by remember { mutableStateOf(4f) }

        var parentWidth by remember { mutableStateOf(0f) }
        var parentHeight by remember { mutableStateOf(0f) }
        
        val isGiant = activeModifiers.any { it.id == "broccoli_giant" && it.isEnabled }
        val itemSize = if (isGiant) 200.dp else 100.dp
        val itemSizePx = with(LocalDensity.current) { itemSize.toPx() }

        var clickCounter by remember { mutableStateOf(0) }

        LaunchedEffect(parentWidth, parentHeight) {
            if (parentWidth > 0 && parentHeight > 0) {
                while (true) {
                    withFrameMillis { _ ->
                        posX += velX
                        posY += velY

                        val limitX = (parentWidth - itemSizePx) / 2
                        val limitY = (parentHeight - itemSizePx) / 2

                        if (posX <= -limitX || posX >= limitX) {
                            velX *= -1
                            posX = posX.coerceIn(-limitX, limitX)
                        }
                        if (posY <= -limitY || posY >= limitY) {
                            velY *= -1
                            posY = posY.coerceIn(-limitY, limitY)
                        }
                    }
                }
            }
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
            // THE MOVING BROCCOLI
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
                            val currentX = posX
                            val currentY = posY
                            
                            val rewardsToGive = if (isGiant) {
                                clickCounter++
                                if (clickCounter >= 2) {
                                    clickCounter = 0
                                    baseRewards
                                } else emptyList()
                            } else {
                                baseRewards
                            }

                            if (rewardsToGive.isNotEmpty()) {
                                onVegetableClick(rewardsToGive)
                                val newOnes = createRewardParticles(
                                    rewards = rewardsToGive,
                                    offsetX = currentX,
                                    offsetY = currentY
                                )
                                
                                val activeCount = flyingParticles.count { !it.isManuallyRemoved }
                                val overflow = (activeCount + newOnes.size) - 20
                                if (overflow > 0) {
                                    flyingParticles.filter { !it.isManuallyRemoved }
                                        .take(overflow)
                                        .forEach { it.isManuallyRemoved = true }
                                }
                                flyingParticles.addAll(newOnes)
                            }
                            
                            scope.launch {
                                scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                            }
                        }
                )
            }

            // STATIC PARTICLE LAYER
            ParticleEffect(flyingParticles)
        }
    }
}
