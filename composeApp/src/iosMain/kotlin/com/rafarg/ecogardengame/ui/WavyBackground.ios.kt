package com.rafarg.ecogardengame.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ShaderBrush
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

private const val SKSL_SHADER = """
    uniform float time;
    uniform vec2 resolution;

    vec4 main(vec2 fragCoord) {
        vec2 uv = fragCoord / resolution;
        
        // Wavy warping
        uv.x += sin(uv.y * 10.0 + time) * 0.05;
        uv.y += cos(uv.x * 10.0 + time) * 0.05;

        // Pattern
        float pattern = sin(uv.x * 20.0) * sin(uv.y * 20.0);
        
        // Dark blue / purple aesthetic
        return vec4(0.05, 0.02, 0.15 + pattern * 0.1, 1.0);
    }
"""

private val runtimeEffect = RuntimeEffect.makeForShader(SKSL_SHADER)

actual fun Modifier.wavyShader(time: Float): Modifier =
    this.drawBehind {
        val shaderBuilder = RuntimeShaderBuilder(runtimeEffect)
        shaderBuilder.uniform("time", time)
        shaderBuilder.uniform("resolution", size.width, size.height)

        val skiaShader = shaderBuilder.makeShader()
        drawRect(ShaderBrush(skiaShader))
    }
