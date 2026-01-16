package com.rafarg.ecogardengame.model

import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.tomato_strip

class Tomato : BaseVegetable() {
    override val id: String = "tomato"
    override val name: String = "Tomato"
    override val resource = Res.drawable.tomato_strip
    override val price: Int = 0
    override val unlockCost: ItemCost = ItemCost(money = 0)
    override var unlocked: Boolean = true
    override val particleEmoji: String = "🍅"
}
