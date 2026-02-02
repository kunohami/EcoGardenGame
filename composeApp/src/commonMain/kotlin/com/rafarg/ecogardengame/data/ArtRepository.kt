package com.rafarg.ecogardengame.data

import com.rafarg.ecogardengame.model.ArtEntry
import ecogardengame.composeapp.generated.resources.*

/**
 * Data repository containing all the collectible art pieces available in the Gallery.
 * These pieces can be unlocked by the player using coins and then used as profile avatars.
 */
object ArtRepository {
    /**
     * Complete list of art entries.
     * Each entry includes a unique ID, a drawable resource, a name, and a coin cost.
     */
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
        ArtEntry("clicky_sad", Res.drawable.clickysad_strip, Res.string.error_generic, 3500),
        ArtEntry("clicky_garden_nologo", Res.drawable.clickysgardennologo, Res.string.app_name, 5000, frameCount = 1),
        ArtEntry("sickle_farmer", Res.drawable.sicklefarmer_strip, Res.string.developed_with, 4000),
        ArtEntry("garden_menu", Res.drawable.gardenmainmenu_strip, Res.string.nav_garden, 3000),
        ArtEntry("shop_menu", Res.drawable.shopmainmenu_strip, Res.string.nav_shop, 3000),
        ArtEntry("library_menu", Res.drawable.librarymainmenu_strip, Res.string.nav_library, 3000),
        ArtEntry("profile_menu", Res.drawable.profilemainmenu_strip, Res.string.nav_profile, 3000),
        ArtEntry("misc_menu", Res.drawable.miscmainmenu_strip, Res.string.nav_misc, 3000),
        ArtEntry("fruit_menu", Res.drawable.fruitmenu_strip, Res.string.app_name, 3000)
    )
}
