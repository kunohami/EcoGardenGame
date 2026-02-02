package com.rafarg.ecogardengame.model

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.ui.SpriteAnimation
import com.rafarg.ecogardengame.util.vibrate
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.squash_strip
import ecogardengame.composeapp.generated.resources.sicklearm_strip
import ecogardengame.composeapp.generated.resources.sicklefarmer_strip
import ecogardengame.composeapp.generated.resources.item_squash
import ecogardengame.composeapp.generated.resources.tutorial_squash
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.pow

/**
 * Squash (Zucchini) vegetable implementation.
 * Mechanics: "Zelda Tennis" / Ping-pong gameplay. The squash bounces diagonally between
 * two corners. The player must click anywhere on the screen when the squash is near
 * the farmer (bottom-left) to "hit" it back.
 * Modifier "Speed Momentum" increases speed and rewards with each consecutive hit.
 */
class Squash : BaseVegetable() {
    override val id: String = "squash"
    override val nameRes = Res.string.item_squash
    override val resource = Res.drawable.squash_strip
    override val price: Int = 250
    override val unlockCost: ItemCost = GamePrices.UNLOCK_SQUASH
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🥒"
    override val tutorialRes = Res.string.tutorial_squash

    /** Tracks the maximum hit streak achieved in the current session. */
    var maxStreak by mutableStateOf(0)

    override val baseRewards: List<Reward> get() = listOf(
        Reward(emoji = particleEmoji, countValue = 1, resource = resource),
        Reward(emoji = "🪙", moneyValue = GamePrices.REWARD_MONEY_SQUASH, countValue = 0)
    )

    private val bonusRewards: List<Reward> get() = listOf(
        Reward(emoji = particleEmoji, countValue = 1, resource = resource),
        Reward(emoji = "🪙", moneyValue = GamePrices.REWARD_MONEY_SQUASH, countValue = 0)
    )

    override val modifiers: List<GameplayModifier> = listOf(
        GameplayModifier(
            id = "squash_speed_momentum",
            nameRes = Res.string.mod_squash_momentum_name,
            descriptionRes = Res.string.mod_squash_momentum_desc,
            unlockCost = GamePrices.MOD_SQUASH_MOMENTUM,
            targetItemId = id
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
        val hitFlash = remember { Animatable(0f) }
        val sickleRotation = remember { Animatable(0f) }
        val flyingParticles = remember { mutableStateListOf<FlyingParticle>() }
        
        var parentWidth by remember { mutableStateOf(0f) }
        var parentHeight by remember { mutableStateOf(0f) }
        val density = LocalDensity.current
        
        val itemSize = 150.dp
        val itemSizePx = with(density) { itemSize.toPx() }

        // --- MOMENTUM STATE ---
        var consecutiveHits by remember { mutableStateOf(0) }
        val isMomentumActive = activeModifiers.any { it.id == "squash_speed_momentum" && it.isEnabled }

        // --- MOVEMENT STATE ---
        var posX by remember { mutableStateOf(0f) }
        var posY by remember { mutableStateOf(0f) }
        // 1 means moving towards Bottom-Left (Farmer), -1 means moving towards Top-Right
        var moveDirection by remember { mutableStateOf(1) }
        
        val baseSpeedDpPerSecond = 800.dp
        var travelPoints by remember { mutableStateOf<Pair<Offset, Offset>?>(null) }

        // Movement loop
        LaunchedEffect(parentWidth, parentHeight, consecutiveHits, isMomentumActive) {
            if (parentWidth > 0 && parentHeight > 0) {
                // Calculate path bounds
                val limitX = (parentWidth - itemSizePx) / 2
                val limitY = (parentHeight - itemSizePx) / 2
                
                val startX = limitX * 0.9f
                val startY = -limitY * 0.9f
                
                val sicklePaddingPx = with(density) { 16.dp.toPx() }
                val endX = -limitX + sicklePaddingPx
                val endY = limitY - sicklePaddingPx
                
                val startPoint = Offset(startX, startY)
                val endPoint = Offset(endX, endY)
                travelPoints = Pair(startPoint, endPoint)

                val dx = endX - startX
                val dy = endY - startY
                val distance = sqrt(dx * dx + dy * dy)
                
                val dirX = if (distance > 0) dx / distance else 0f
                val dirY = if (distance > 0) dy / distance else 0f

                // Speed increases by 10% per consecutive hit if modifier is active
                val speedMultiplier = if (isMomentumActive) {
                    1.1.pow(consecutiveHits.toDouble()).toFloat()
                } else 1f
                
                val speedPxPerSecond = with(density) { baseSpeedDpPerSecond.toPx() } * speedMultiplier

                if (posX == 0f && posY == 0f) {
                    posX = startX
                    posY = startY
                    moveDirection = 1
                }
                
                var lastFrameTime = 0L
                while (true) {
                    withFrameMillis { frameTime ->
                        if (lastFrameTime != 0L) {
                            val deltaSeconds = (frameTime - lastFrameTime) / 1000f
                            
                            posX += moveDirection * dirX * speedPxPerSecond * deltaSeconds
                            posY += moveDirection * dirY * speedPxPerSecond * deltaSeconds
                            
                            // Check for arrival at corners
                            if (moveDirection == 1) { // Moving towards Bottom-Left
                                if (posX <= endX && posY >= endY) {
                                    posX = endX
                                    posY = endY
                                    moveDirection = -1
                                }
                            } else { // Moving towards Top-Right
                                if (posX >= startX && posY <= startY) {
                                    posX = startX
                                    posY = startY
                                    moveDirection = 1
                                    
                                    if (vibrationEnabled) {
                                        vibrate(vibrationIntensity.toLong())
                                    }
                                }
                            }
                        }
                        lastFrameTime = frameTime
                    }
                }
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    parentWidth = it.size.width.toFloat()
                    parentHeight = it.size.height.toFloat()
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // Interaction: Slash with the sickle
                    val targetX = travelPoints?.second?.x ?: 0f
                    val targetY = travelPoints?.second?.y ?: 0f
                    val tolerancePx = itemSizePx * 0.7f
                    
                    // Hit logic: check if the squash is within the hit zone near the farmer
                    val isNearBottomLeft = 
                        parentWidth > 0 && parentHeight > 0 && 
                        posX <= targetX + tolerancePx && 
                        posX >= targetX - tolerancePx && 
                        posY >= targetY - tolerancePx &&
                        posY <= targetY + tolerancePx

                    // Execute slash animation regardless of hit success
                    scope.launch {
                        sickleRotation.stop()
                        sickleRotation.snapTo(0f)
                        sickleRotation.animateTo(90f, tween(durationMillis = 80, easing = LinearEasing))
                        sickleRotation.snapTo(0f)
                    }

                    if (isNearBottomLeft && moveDirection == 1) {
                        // Successful hit!
                        moveDirection = -1
                        consecutiveHits++
                        if (consecutiveHits > maxStreak) {
                            maxStreak = consecutiveHits
                        }
                        
                        // Apply momentum bonuses to rewards
                        val finalRewardsBase = bonusRewards.map { reward ->
                            if (isMomentumActive && consecutiveHits > 0) {
                                when {
                                    reward.emoji == "🪙" -> reward.copy(moneyValue = reward.moneyValue + consecutiveHits)
                                    reward.emoji == particleEmoji -> reward.copy(countValue = reward.countValue + consecutiveHits)
                                    else -> reward
                                }
                            } else {
                                reward
                            }
                        }
                        
                        val finalRewards = onVegetableClick(finalRewardsBase)
                        
                        scope.launch {
                            launch {
                                scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                            }
                            launch {
                                hitFlash.animateTo(1f, tween(50))
                                hitFlash.animateTo(0f, tween(200))
                            }
                        }
                        
                        val newOnes = createRewardParticles(
                            rewards = finalRewards,
                            offsetX = posX,
                            offsetY = posY
                        )
                        val activeCount = flyingParticles.count { !it.isManuallyRemoved }
                        val overflow = (activeCount + newOnes.size) - 20
                        if (overflow > 0) {
                            flyingParticles.filter { !it.isManuallyRemoved }.take(overflow).forEach { it.isManuallyRemoved = true }
                        }
                        flyingParticles.addAll(newOnes)
                    } else {
                        // Miss: Reset momentum
                        if (isMomentumActive) {
                            consecutiveHits = 0
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Render the Squash (the "ball")
            Box(
                modifier = Modifier
                    .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                    .size(itemSize),
                contentAlignment = Alignment.Center
            ) {
                SpriteAnimation(
                    painter = painterResource(resource),
                    frameCount = 3,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                        .drawWithContent {
                            if (hitFlash.value > 0f) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = hitFlash.value * 0.8f),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width / 2, size.height / 2),
                                        radius = size.minDimension * 0.8f
                                    ),
                                    radius = size.minDimension * 0.8f,
                                    center = Offset(size.width / 2, size.height / 2)
                                )
                            }
                            drawContent()
                        }
                )
            }

            // Render the Sickle Farmer (the "player")
            val farmerSize = 125.dp
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(farmerSize)
                    .offset(x = (-10).dp, y = 10.dp)
            ) {
                // SICKLE ARM (animated)
                val armSize = 60.dp
                Box(
                    modifier = Modifier
                        .offset(x = (farmerSize / 2) + 8.dp, y = (farmerSize / 2) - armSize + 4.dp)
                        .size(armSize)
                ) {
                    SpriteAnimation(
                        painter = painterResource(Res.drawable.sicklearm_strip),
                        frameCount = 3,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = sickleRotation.value - 30f
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 1f)
                            }
                    )
                }

                // Farmer body
                SpriteAnimation(
                    painter = painterResource(Res.drawable.sicklefarmer_strip),
                    frameCount = 3,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Particle effect layer
            ParticleEffect(flyingParticles)
        }
    }
}
