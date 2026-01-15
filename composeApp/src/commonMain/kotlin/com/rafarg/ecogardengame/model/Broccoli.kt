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
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Broccoli implementation with a unique "DVD Screensaver" movement.
 *
 * It overrides the [Content] method to implement its own movement logic,
 * ensuring the hitbox and particles follow the moving object.
 */
class Broccoli : BaseVegetable() {
    override val id: String = "broccoli"
    override val name: String = "Broccoli"
    override val resource = Res.drawable.broccoli_strip
    override val price: Int = 50
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🥦"

    @Composable
    override fun Content(
        modifier: Modifier,
        onVegetableClick: () -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val scale = remember { Animatable(1f) }
        var flyingParticles by remember { mutableStateOf<List<FlyingParticle>>(emptyList()) }
        
        // --- MOVEMENT STATE ---
        var posX by remember { mutableStateOf(0f) }
        var posY by remember { mutableStateOf(0f) }
        var velX by remember { mutableStateOf(4f) }
        var velY by remember { mutableStateOf(4f) }

        var parentWidth by remember { mutableStateOf(0f) }
        var parentHeight by remember { mutableStateOf(0f) }
        
        val itemSize = 100.dp
        val itemSizePx = with(LocalDensity.current) { itemSize.toPx() }

        // Movement Loop
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
            Box(
                modifier = Modifier
                    .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                    .size(itemSize),
                contentAlignment = Alignment.Center
            ) {
                // The Sprite
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
                            onVegetableClick()
                            // Local animation and particles
                            scope.launch {
                                scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                                scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
                            }
                            flyingParticles = List(5) {
                                FlyingParticle(id = Random.nextLong())
                            }
                        }
                )

                // Particles are relative to this moving Box!
                ParticleEffect(flyingParticles) {
                    flyingParticles = it
                }
            }
        }
    }
}
