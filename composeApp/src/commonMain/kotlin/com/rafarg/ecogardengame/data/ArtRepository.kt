package com.rafarg.ecogardengame.data

import com.rafarg.ecogardengame.model.ArtEntry
import ecogardengame.composeapp.generated.resources.*

object ArtRepository {
    val artEntries = listOf(
        ArtEntry("tomato", Res.drawable.tomato_strip, Res.string.item_tomato, 500),
        ArtEntry("broccoli", Res.drawable.broccoli_strip, Res.string.item_broccoli, 750),
        ArtEntry("bell_pepper", Res.drawable.bellpepper_strip, Res.string.item_bell_pepper, 1000),
        ArtEntry("garlic", Res.drawable.garlic_strip, Res.string.item_garlic, 1250),
        ArtEntry("onion", Res.drawable.purpleonion_strip, Res.string.item_purple_onion, 1500),
        ArtEntry("squash", Res.drawable.squash_strip, Res.string.item_squash, 2000),
        ArtEntry("apple", Res.drawable.apple_strip, Res.string.item_apple, 2500),
        ArtEntry("bunny", Res.drawable.infobunny_strip, Res.string.bunny_thanks, 3000),
        ArtEntry("clicky_cheeky", Res.drawable.clickycheekykneel_strip, Res.string.nav_profile, 3500),
        ArtEntry("clicky_explain", Res.drawable.clickyexplain_strip, Res.string.app_name, 3500),
        ArtEntry("clicky_open", Res.drawable.clickyopenarms_strip, Res.string.congratulations, 3500),
        ArtEntry("sickle_farmer", Res.drawable.sicklefarmer_strip, Res.string.developed_with, 4000)
    )
}
