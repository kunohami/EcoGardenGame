package com.rafarg.ecogardengame.model

import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.tomato_strip

/**
 * Tomato implementation of a [BaseVegetable].
 *
 * This class defines the specific data for the Tomato, including its image resource,
 * price, and the emoji used for its particle effect.
 *
 * How to use: Instantiated in the item list to be used as a playable vegetable.
 */
class Tomato : BaseVegetable() {
    override val id: String = "tomato"
    override val name: String = "Tomato"
    override val resource = Res.drawable.tomato_strip
    override val price: Int = 0
    override var unlocked: Boolean = true
    override val particleEmoji: String = "🍅"
}
