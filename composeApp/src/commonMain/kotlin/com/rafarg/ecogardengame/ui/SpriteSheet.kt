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

/**
 * --- CUSTOM UI COMPONENT: SPRITE ANIMATION ---
 * This composable handles the rendering of animated "Sprite Sheets" (long images containing
 * multiple frames side by side). This is a core technique in 2D game development.
 *
 * @param painter The image resource (strip) to be animated.
 * @param frameCount Total number of frames in the strip.
 * @param frameDurationMillis How long each frame stays on screen.
 */
@Composable
fun SpriteAnimation(
    painter: Painter,
    frameCount: Int,
    modifier: Modifier = Modifier,
    frameDurationMillis: Long = 150,
    colorFilter: ColorFilter? = null,
) {
    /**
     * --- REACTIVE STATE ---
     * 'frame' keeps track of the current image index being shown.
     */
    var frame by remember { mutableStateOf(0) }

    /**
     * --- ANIMATION LOOP (Coroutines) ---
     * 'LaunchedEffect' starts a background loop that increments the frame index.
     * It automatically handles cancellations if the Composable is removed from the UI.
     */
    LaunchedEffect(painter, frameCount) {
        frame = 0
        // Only start the loop if there's more than one frame (otherwise it's a static image).
        if (frameCount > 1) {
            while (true) {
                delay(frameDurationMillis)
                // Modular arithmetic: wraps around back to 0 when it reaches the end.
                frame = (frame + 1) % frameCount
            }
        }
    }

    /**
     * --- CUSTOM DRAWING (Canvas) ---
     * We don't use a simple Image() because we only want to show a SLICE of the strip.
     * This demonstrates low-level graphics manipulation in Compose.
     */
    Canvas(modifier = modifier) {
        val intrinsicSize = painter.intrinsicSize
        // Basic safety check: ensure the image is actually loaded.
        if (intrinsicSize.width <= 0 || intrinsicSize.height <= 0) return@Canvas

        // Logic to calculate the size of a single frame within the large strip.
        val srcFrameWidth = intrinsicSize.width / frameCount
        val srcFrameHeight = intrinsicSize.height
        val frameAspectRatio = srcFrameWidth / srcFrameHeight

        // Fit the frame into the Canvas while maintaining its aspect ratio.
        val canvasAspectRatio = size.width / size.height
        val (drawFrameWidth, drawFrameHeight) =
            if (canvasAspectRatio > frameAspectRatio) {
                size.height * frameAspectRatio to size.height
            } else {
                size.width to size.width / frameAspectRatio
            }

        val totalStripWidth = drawFrameWidth * frameCount

        // Center the frame inside the available canvas space.
        val offsetX = (size.width - drawFrameWidth) / 2
        val offsetY = (size.height - drawFrameHeight) / 2

        /**
         * --- THE SLICING TRICK ---
         * 1. 'clipRect': We create a "viewing window" that only shows one frame's width.
         * 2. 'translate': We slide the entire long strip horizontally behind that window.
         * By shifting it based on the 'frame' index, a different slice appears in the window.
         */
        clipRect(
            left = offsetX,
            top = offsetY,
            right = offsetX + drawFrameWidth,
            bottom = offsetY + drawFrameHeight,
        ) {
            // Sliding logic: shift left by (frame index * width of one frame).
            translate(left = offsetX - (frame * drawFrameWidth), top = offsetY) {
                with(painter) {
                    draw(
                        size = Size(totalStripWidth, drawFrameHeight),
                        colorFilter = colorFilter,
                    )
                }
            }
        }
    }
}
