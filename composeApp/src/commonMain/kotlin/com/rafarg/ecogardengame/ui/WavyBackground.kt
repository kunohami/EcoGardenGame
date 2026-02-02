package com.rafarg.ecogardengame.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush

/**
 * WavyBackground provides a dynamic, animated background effect.
 * It uses an AGSL shader (Android Graphics Shading Language) on Android 
 * to create a fluid, wavy aesthetic.
 */
@Composable
fun WavyBackground(enabled: Boolean) {
    // If the theme is not "Wavy", we don't render anything here.
    if (!enabled) return

    // rememberInfiniteTransition creates an animation that runs forever.
    val infiniteTransition = rememberInfiniteTransition()
    
    // This 'time' value increments continuously.
    // It's passed to the shader to drive the movement of the waves.
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing), // 30 seconds for a full loop
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            // .wavyShader is an 'expect' function, meaning its implementation 
            // is platform-specific (defined in androidMain and desktopMain/iosMain).
            .wavyShader(time)
    )
}

/**
 * Platform-specific modifier that applies the actual shader logic.
 * @param time The current animation time to make the shader move.
 */
expect fun Modifier.wavyShader(time: Float): Modifier
