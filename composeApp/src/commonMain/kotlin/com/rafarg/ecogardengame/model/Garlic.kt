package com.rafarg.ecogardengame.model

import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.garlic_strip

class Garlic : BaseVegetable() {
    override val id: String = "garlic"
    override val name: String = "Garlic"
    override val resource = Res.drawable.garlic_strip
    override val price: Int = 150
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🧄"
}
