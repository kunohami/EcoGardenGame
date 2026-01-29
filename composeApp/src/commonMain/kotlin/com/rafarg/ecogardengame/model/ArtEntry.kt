package com.rafarg.ecogardengame.model

import ecogardengame.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.DrawableResource

data class ArtEntry(
    val id: String,
    val resource: DrawableResource,
    val nameRes: org.jetbrains.compose.resources.StringResource,
    val cost: Int = 1000,
    var isUnlocked: Boolean = false,
    val frameCount: Int = 3
)
