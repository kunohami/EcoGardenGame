package com.rafarg.ecogardengame.model

import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.purpleonion_strip

class PurpleOnion : BaseVegetable() {
    override val id: String = "purple_onion"
    override val name: String = "Purple Onion"
    override val resource = Res.drawable.purpleonion_strip
    override val price: Int = 200
    override var unlocked: Boolean = false
    override val particleEmoji: String = "🧅"
}
