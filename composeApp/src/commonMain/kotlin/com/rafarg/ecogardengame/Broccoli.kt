package com.rafarg.ecogardengame

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.broccoli_strip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class Broccoli : GameItem {
    override val name: String = "Broccoli"
    override val resource = Res.drawable.broccoli_strip
    override val price: Int = 50
    override var unlocked: Boolean = false

    @Composable
    override fun Animate(modifier: Modifier) {
        SpriteAnimation(
            painter = org.jetbrains.compose.resources.painterResource(resource),
            frameCount = 3,
            modifier = modifier
        )
    }

    override fun animateClick(
        scope: CoroutineScope,
        scale: Animatable<Float, * >,
        translationY: Animatable<Float, * >,
        rotation: Animatable<Float, * >,
        clicks: Int,
        onEmitParticles: (List<FlyingParticle>) -> Unit
    ) {
        scope.launch {
            scale.animateTo(0.8f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
            scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
        }
    }

    @Composable
    override fun ParticleEffect(
        particles: List<FlyingParticle>,
        updateParticles: (List<FlyingParticle>) -> Unit
    ) {
        particles.forEach { particle ->
            LaunchedEffect(particle.id) {
                val explosionDistance = 1900f
                val animationDuration = 1000

                launch {
                    particle.animatableY.animateTo(
                        targetValue = (Random.nextFloat() * explosionDistance) - (explosionDistance / 2),
                        animationSpec = tween(durationMillis = animationDuration, easing = androidx.compose.animation.core.EaseOut)
                    )
                }
                launch {
                    particle.animatableX.animateTo(
                        targetValue = (Random.nextFloat() * explosionDistance) - (explosionDistance / 2),
                        animationSpec = tween(durationMillis = animationDuration, easing = androidx.compose.animation.core.EaseOut)
                    )
                }
                launch {
                    delay((animationDuration / 2).toLong())
                    particle.animatableAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = (animationDuration / 2))
                    )
                    updateParticles(particles.filterNot { it.id == particle.id })
                }
            }

            Text(
                text = "🥦",
                fontSize = 32.sp,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = particle.animatableX.value.toInt(),
                            y = particle.animatableY.value.toInt()
                        )
                    }
                    .alpha(particle.animatableAlpha.value)
            )
        }
    }
}
