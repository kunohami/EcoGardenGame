package com.rafarg.ecogardengame

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.DrawableResource

interface GameItem {
    val name: String
    val resource: DrawableResource
    val price: Int
    var unlocked: Boolean

    @Composable
    fun Animate(modifier: Modifier)

    fun animateClick(
        scope: CoroutineScope,
        scale: Animatable<Float, * >,
        translationY: Animatable<Float, * >,
        rotation: Animatable<Float, * >,
        clicks: Int,
        onEmitParticles: (List<FlyingParticle>) -> Unit
    )

    @Composable
    fun ParticleEffect(particles: List<FlyingParticle>, updateParticles: (List<FlyingParticle>) -> Unit)
}
