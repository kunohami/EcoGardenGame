package com.rafarg.ecogardengame.model

import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.broccoli_strip

class Broccoli : BaseVegetable() {
    override val id: String = "broccoli"
    override val name: String = "Broccoli"
    override val resource = Res.drawable.broccoli_strip
    override val price: Int = 50
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🥦"
}
