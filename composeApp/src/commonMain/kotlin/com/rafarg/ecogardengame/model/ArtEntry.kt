package com.rafarg.ecogardengame.model

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/**
 * Represents a piece of collectible art in the gallery.
 * @property id Unique identifier for the art piece.
 * @property resource The drawable resource to display.
 * @property nameRes Resource ID for the art's display name.
 * @property cost The price in coins to unlock this art.
 * @property isUnlocked Current unlock status.
 * @property frameCount Number of frames if the resource is a sprite sheet.
 */
data class ArtEntry(
    val id: String,
    val resource: DrawableResource,
    val nameRes: StringResource,
    val cost: Int = 1000,
    var isUnlocked: Boolean = false,
    val frameCount: Int = 3,
)
