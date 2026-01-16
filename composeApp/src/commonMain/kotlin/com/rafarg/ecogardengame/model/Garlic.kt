package com.rafarg.ecogardengame.model

import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.garlic_strip

class Garlic : BaseVegetable() {
    override val id: String = "garlic"
    override val name: String = "Garlic"
    override val resource = Res.drawable.garlic_strip
    override val price: Int = 150
    override val unlockCost: ItemCost = ItemCost(
        money = 750,
        vegetableCosts = mapOf("tomato" to 150, "broccoli" to 50, "bell_pepper" to 20, "purple_onion" to 5)
    )
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🧄"
}
