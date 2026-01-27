package com.rafarg.ecogardengame.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * A custom shape that draws an "inflated" cloudy box without any triangle tips.
 * Designed to fit perfectly within its bounds to avoid clipping.
 */
class SpeechBubbleShape(
    // Parameters kept for compatibility with existing calls, but logic for tips is removed
    private val tipSize: Dp = 14.dp,
    private val tipAtTop: Boolean = true,
    private val tipPaddingEnd: Dp = 30.dp,
    private val cornerRadius: Dp = 24.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val cornerPx = with(density) { cornerRadius.toPx() }
        
        // This margin defines how much the body is inset to allow the curves to "bulge"
        // out to the edges [0, w] and [0, h] without being clipped.
        val bulgeMargin = with(density) { 12.dp.toPx() }

        val path = Path().apply {
            val leftBody = bulgeMargin
            val rightBody = w - bulgeMargin
            val topBody = bulgeMargin
            val bottomBody = h - bulgeMargin

            // Start at top-left corner body position
            moveTo(leftBody + cornerPx, topBody)

            // --- TOP SIDE ---
            // Bulge up to y = 0
            quadraticTo(w / 2, 0f, rightBody - cornerPx, topBody)
            
            // Top-Right Corner
            quadraticTo(rightBody, topBody, rightBody, topBody + cornerPx)

            // --- RIGHT SIDE ---
            // Bulge right to x = w
            quadraticTo(w, h / 2, rightBody, bottomBody - cornerPx)
            
            // Bottom-Right Corner
            quadraticTo(rightBody, bottomBody, rightBody - cornerPx, bottomBody)

            // --- BOTTOM SIDE ---
            // Bulge down to y = h
            quadraticTo(w / 2, h, leftBody + cornerPx, bottomBody)
            
            // Bottom-Left Corner
            quadraticTo(leftBody, bottomBody, leftBody, bottomBody - cornerPx)

            // --- LEFT SIDE ---
            // Bulge left to x = 0
            quadraticTo(0f, h / 2, leftBody, topBody + cornerPx)
            
            // Top-Left Corner
            quadraticTo(leftBody, topBody, leftBody + cornerPx, topBody)

            close()
        }
        return Outline.Generic(path)
    }
}
