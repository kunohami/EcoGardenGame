package com.rafarg.ecogardengame.ui

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush

private const val AGSL_SHADER = """
    uniform float time;
    uniform vec2 resolution;

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / resolution;
        
        // Wavy warping
        uv.x += sin(uv.y * 10.0 + time) * 0.05;
        uv.y += cos(uv.x * 10.0 + time) * 0.05;

        // Pattern
        float pattern = sin(uv.x * 20.0) * sin(uv.y * 20.0);
        
        // Dark blue / purple aesthetic
        return half4(0.05, 0.02, 0.15 + pattern * 0.1, 1.0);
    }
"""

actual fun Modifier.wavyShader(time: Float): Modifier =
    this.drawBehind {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shader = RuntimeShader(AGSL_SHADER)
            shader.setFloatUniform("time", time)
            shader.setFloatUniform("resolution", size.width, size.height)
            drawRect(ShaderBrush(shader))
        } else {
            // Fallback for older Android versions
            val color1 = Color(0xFF0D0221)
            val color2 = Color(0xFF1B065E)
            drawRect(
                brush =
                    Brush.verticalGradient(
                        colors = listOf(color1, color2),
                        startY = 0f,
                        endY = size.height,
                    ),
            )
        }
    }
