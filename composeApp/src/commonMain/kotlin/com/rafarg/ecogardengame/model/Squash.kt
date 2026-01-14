package com.rafarg.ecogardengame.model

import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.squash_strip

class Squash : BaseVegetable() {
    override val id: String = "squash"
    override val name: String = "Squash"
    override val resource = Res.drawable.squash_strip
    override val price: Int = 250
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🥒"
}
