package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
        val intrinsicSize = painter.intrinsicSize
        if (intrinsicSize.width <= 0 || intrinsicSize.height <= 0) return@Canvas

        // Calculate single frame dimensions from the source
        val srcFrameWidth = intrinsicSize.width / frameCount
        val srcFrameHeight = intrinsicSize.height
        val frameAspectRatio = srcFrameWidth / srcFrameHeight

        // Determine drawing size maintaining aspect ratio (Fit center)
        val canvasAspectRatio = size.width / size.height
        val (drawFrameWidth, drawFrameHeight) = if (canvasAspectRatio > frameAspectRatio) {
            // Height is the constraint
            size.height * frameAspectRatio to size.height
        } else {
            // Width is the constraint
            size.width to size.width / frameAspectRatio
        }

        val totalStripWidth = drawFrameWidth * frameCount
        
        // Center the frame in the canvas area
        val offsetX = (size.width - drawFrameWidth) / 2
        val offsetY = (size.height - drawFrameHeight) / 2

        clipRect(
            left = offsetX, 
            top = offsetY, 
            right = offsetX + drawFrameWidth, 
            bottom = offsetY + drawFrameHeight
        ) {
            translate(left = offsetX - (frame * drawFrameWidth), top = offsetY) {
                with(painter) {
                    draw(
                        size = Size(totalStripWidth, drawFrameHeight),
                        colorFilter = colorFilter
                    )
                }
            }
        }
    }
}
