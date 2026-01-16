package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.delay

@Composable
fun SpriteAnimation(
    painter: Painter,
    frameCount: Int,
    modifier: Modifier = Modifier,
    frameDurationMillis: Long = 150,
    colorFilter: ColorFilter? = null
) {
    var frame by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(frameDurationMillis)
            frame = (frame + 1) % frameCount
        }
    }

    Canvas(modifier = modifier) {
        val drawWidth = size.width * frameCount
        val drawHeight = size.height

        clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
            translate(left = -frame * size.width, top = 0f) {
                with(painter) {
                    draw(
                        size = Size(drawWidth, drawHeight),
                        colorFilter = colorFilter
                    )
                }
            }
        }
    }
}
