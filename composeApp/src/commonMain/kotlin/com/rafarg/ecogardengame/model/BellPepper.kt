package com.rafarg.ecogardengame.model

import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.bellpepper_strip

class BellPepper : BaseVegetable() {
    override val id: String = "bell_pepper"
    override val name: String = "Bell Pepper"
    override val resource = Res.drawable.bellpepper_strip
    override val price: Int = 100
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🫑"
}
