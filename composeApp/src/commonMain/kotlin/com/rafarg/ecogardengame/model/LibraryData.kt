package com.rafarg.ecogardengame.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.StringResource
import ecogardengame.composeapp.generated.resources.*

/**
 * Represents a single piece of information or "fact" in the Knowledge Library.
 * @property id Unique identifier for the entry.
 * @property titleRes Resource ID for the fact's title.
 * @property contentRes Resource ID for the fact's detailed description.
 * @property cost The price in coins to unlock this fact.
 * @property isUnlocked Current unlock status.
 */
class LibraryEntry(
    val id: String,
    val titleRes: StringResource,
    val contentRes: StringResource,
    val cost: ItemCost,
    isUnlockedInitial: Boolean = false
) {
    var isUnlocked by mutableStateOf(isUnlockedInitial)
}

/**
 * Represents a collection of library entries grouped by topic.
 * @property id Unique identifier for the category.
 * @property nameRes Resource ID for the category name.
 * @property icon Emoji or icon representation for the category.
 * @property entries List of library facts belonging to this category.
 */
data class LibraryCategory(
    val id: String,
    val nameRes: StringResource,
    val icon: String,
    val entries: List<LibraryEntry>
)

/**
 * Data repository containing all the categories and facts for the Knowledge Library.
 */
object LibraryRepository {

    /**
     * Helper function to create a new LibraryEntry with a standardized ID.
     */
    private fun createEntry(
        cat: String,
        index: Int,
        titleRes: StringResource,
        contentRes: StringResource,
        price: Int
    ): LibraryEntry {
        return LibraryEntry(
            id = "${cat}_$index",
            titleRes = titleRes,
            contentRes = contentRes,
            cost = ItemCost(money = price)
        )
    }

    // --- FACT ENTRIES FOR EACH VEGETABLE AND TOPIC ---

    val tomatoEntries = listOf(
        createEntry("tomato", 1, Res.string.lib_tomato_t1, Res.string.lib_tomato_c1, 100),
        createEntry("tomato", 2, Res.string.lib_tomato_t2, Res.string.lib_tomato_c2, 200),
        createEntry("tomato", 3, Res.string.lib_tomato_t3, Res.string.lib_tomato_c3, 300),
        createEntry("tomato", 4, Res.string.lib_tomato_t4, Res.string.lib_tomato_c4, 400),
        createEntry("tomato", 5, Res.string.lib_tomato_t5, Res.string.lib_tomato_c5, 500),
        createEntry("tomato", 6, Res.string.lib_tomato_t6, Res.string.lib_tomato_c6, 600),
        createEntry("tomato", 7, Res.string.lib_tomato_t7, Res.string.lib_tomato_c7, 700),
        createEntry("tomato", 8, Res.string.lib_tomato_t8, Res.string.lib_tomato_c8, 800),
        createEntry("tomato", 9, Res.string.lib_tomato_t9, Res.string.lib_tomato_c9, 900),
        createEntry("tomato", 10, Res.string.lib_tomato_t10, Res.string.lib_tomato_c10, 1000)
    )

    val broccoliEntries = listOf(
        createEntry("broccoli", 1, Res.string.lib_broccoli_t1, Res.string.lib_broccoli_c1, 150),
        createEntry("broccoli", 2, Res.string.lib_broccoli_t2, Res.string.lib_broccoli_c2, 300),
        createEntry("broccoli", 3, Res.string.lib_broccoli_t3, Res.string.lib_broccoli_c3, 450),
        createEntry("broccoli", 4, Res.string.lib_broccoli_t4, Res.string.lib_broccoli_c4, 600),
        createEntry("broccoli", 5, Res.string.lib_broccoli_t5, Res.string.lib_broccoli_c5, 750),
        createEntry("broccoli", 6, Res.string.lib_broccoli_t6, Res.string.lib_broccoli_c6, 900),
        createEntry("broccoli", 7, Res.string.lib_broccoli_t7, Res.string.lib_broccoli_c7, 1050),
        createEntry("broccoli", 8, Res.string.lib_broccoli_t8, Res.string.lib_broccoli_c8, 1200),
        createEntry("broccoli", 9, Res.string.lib_broccoli_t9, Res.string.lib_broccoli_c9, 1350),
        createEntry("broccoli", 10, Res.string.lib_broccoli_t10, Res.string.lib_broccoli_c10, 1500)
    )

    val bellPepperEntries = listOf(
        createEntry("bell_pepper", 1, Res.string.lib_bell_pepper_t1, Res.string.lib_bell_pepper_c1, 200),
        createEntry("bell_pepper", 2, Res.string.lib_bell_pepper_t2, Res.string.lib_bell_pepper_c2, 400),
        createEntry("bell_pepper", 3, Res.string.lib_bell_pepper_t3, Res.string.lib_bell_pepper_c3, 600),
        createEntry("bell_pepper", 4, Res.string.lib_bell_pepper_t4, Res.string.lib_bell_pepper_c4, 800),
        createEntry("bell_pepper", 5, Res.string.lib_bell_pepper_t5, Res.string.lib_bell_pepper_c5, 1000),
        createEntry("bell_pepper", 6, Res.string.lib_bell_pepper_t6, Res.string.lib_bell_pepper_c6, 1200),
        createEntry("bell_pepper", 7, Res.string.lib_bell_pepper_t7, Res.string.lib_bell_pepper_c7, 1400),
        createEntry("bell_pepper", 8, Res.string.lib_bell_pepper_t8, Res.string.lib_bell_pepper_c8, 1600),
        createEntry("bell_pepper", 9, Res.string.lib_bell_pepper_t9, Res.string.lib_bell_pepper_c9, 1800),
        createEntry("bell_pepper", 10, Res.string.lib_bell_pepper_t10, Res.string.lib_bell_pepper_c10, 2000)
    )

    val garlicEntries = listOf(
        createEntry("garlic", 1, Res.string.lib_garlic_t1, Res.string.lib_garlic_c1, 250),
        createEntry("garlic", 2, Res.string.lib_garlic_t2, Res.string.lib_garlic_c2, 500),
        createEntry("garlic", 3, Res.string.lib_garlic_t3, Res.string.lib_garlic_c3, 750),
        createEntry("garlic", 4, Res.string.lib_garlic_t4, Res.string.lib_garlic_c4, 1000),
        createEntry("garlic", 5, Res.string.lib_garlic_t5, Res.string.lib_garlic_c5, 1250),
        createEntry("garlic", 6, Res.string.lib_garlic_t6, Res.string.lib_garlic_c6, 1500),
        createEntry("garlic", 7, Res.string.lib_garlic_t7, Res.string.lib_garlic_c7, 1750),
        createEntry("garlic", 8, Res.string.lib_garlic_t8, Res.string.lib_garlic_c8, 2000),
        createEntry("garlic", 9, Res.string.lib_garlic_t9, Res.string.lib_garlic_c9, 2250),
        createEntry("garlic", 10, Res.string.lib_garlic_t10, Res.string.lib_garlic_c10, 2500)
    )

    val purpleOnionEntries = listOf(
        createEntry("purple_onion", 1, Res.string.lib_purple_onion_t1, Res.string.lib_purple_onion_c1, 300),
        createEntry("purple_onion", 2, Res.string.lib_purple_onion_t2, Res.string.lib_purple_onion_c2, 600),
        createEntry("purple_onion", 3, Res.string.lib_purple_onion_t3, Res.string.lib_purple_onion_c3, 900),
        createEntry("purple_onion", 4, Res.string.lib_purple_onion_t4, Res.string.lib_purple_onion_c4, 1200),
        createEntry("purple_onion", 5, Res.string.lib_purple_onion_t5, Res.string.lib_purple_onion_c5, 1500),
        createEntry("purple_onion", 6, Res.string.lib_purple_onion_t6, Res.string.lib_purple_onion_c6, 1800),
        createEntry("purple_onion", 7, Res.string.lib_purple_onion_t7, Res.string.lib_purple_onion_c7, 2100),
        createEntry("purple_onion", 8, Res.string.lib_purple_onion_t8, Res.string.lib_purple_onion_c8, 2400),
        createEntry("purple_onion", 9, Res.string.lib_purple_onion_t9, Res.string.lib_purple_onion_c9, 2700),
        createEntry("purple_onion", 10, Res.string.lib_purple_onion_t10, Res.string.lib_purple_onion_c10, 3000)
    )

    val squashEntries = listOf(
        createEntry("squash", 1, Res.string.lib_squash_t1, Res.string.lib_squash_c1, 350),
        createEntry("squash", 2, Res.string.lib_squash_t2, Res.string.lib_squash_c2, 700),
        createEntry("squash", 3, Res.string.lib_squash_t3, Res.string.lib_squash_c3, 1050),
        createEntry("squash", 4, Res.string.lib_squash_t4, Res.string.lib_squash_c4, 1400),
        createEntry("squash", 5, Res.string.lib_squash_t5, Res.string.lib_squash_c5, 1750),
        createEntry("squash", 6, Res.string.lib_squash_t6, Res.string.lib_squash_c6, 2100),
        createEntry("squash", 7, Res.string.lib_squash_t7, Res.string.lib_squash_c7, 2450),
        createEntry("squash", 8, Res.string.lib_squash_t8, Res.string.lib_squash_c8, 2800),
        createEntry("squash", 9, Res.string.lib_squash_t9, Res.string.lib_squash_c9, 3150),
        createEntry("squash", 10, Res.string.lib_squash_t10, Res.string.lib_squash_c10, 3500)
    )

    val appleEntries = listOf(
        createEntry("apple", 1, Res.string.lib_apple_t1, Res.string.lib_apple_c1, 400),
        createEntry("apple", 2, Res.string.lib_apple_t2, Res.string.lib_apple_c2, 800),
        createEntry("apple", 3, Res.string.lib_apple_t3, Res.string.lib_apple_c3, 1200),
        createEntry("apple", 4, Res.string.lib_apple_t4, Res.string.lib_apple_c4, 1600),
        createEntry("apple", 5, Res.string.lib_apple_t5, Res.string.lib_apple_c5, 2000),
        createEntry("apple", 6, Res.string.lib_apple_t6, Res.string.lib_apple_c6, 2400),
        createEntry("apple", 7, Res.string.lib_apple_t7, Res.string.lib_apple_c7, 2800),
        createEntry("apple", 8, Res.string.lib_apple_t8, Res.string.lib_apple_c8, 3200),
        createEntry("apple", 9, Res.string.lib_apple_t9, Res.string.lib_apple_c9, 3600),
        createEntry("apple", 10, Res.string.lib_apple_t10, Res.string.lib_apple_c10, 4000)
    )

    val plaguesEntries = listOf(
        createEntry("plagues", 1, Res.string.lib_plagues_t1, Res.string.lib_plagues_c1, 500),
        createEntry("plagues", 2, Res.string.lib_plagues_t2, Res.string.lib_plagues_c2, 1000),
        createEntry("plagues", 3, Res.string.lib_plagues_t3, Res.string.lib_plagues_c3, 1500),
        createEntry("plagues", 4, Res.string.lib_plagues_t4, Res.string.lib_plagues_c4, 2000),
        createEntry("plagues", 5, Res.string.lib_plagues_t5, Res.string.lib_plagues_c5, 2500),
        createEntry("plagues", 6, Res.string.lib_plagues_t6, Res.string.lib_plagues_c6, 3000),
        createEntry("plagues", 7, Res.string.lib_plagues_t7, Res.string.lib_plagues_c7, 3500),
        createEntry("plagues", 8, Res.string.lib_plagues_t8, Res.string.lib_plagues_c8, 4000),
        createEntry("plagues", 9, Res.string.lib_plagues_t9, Res.string.lib_plagues_c9, 4500),
        createEntry("plagues", 10, Res.string.lib_plagues_t10, Res.string.lib_plagues_c10, 5000)
    )

    val farmersEntries = listOf(
        createEntry("farmers", 1, Res.string.lib_farmers_t1, Res.string.lib_farmers_c1, 450),
        createEntry("farmers", 2, Res.string.lib_farmers_t2, Res.string.lib_farmers_c2, 900),
        createEntry("farmers", 3, Res.string.lib_farmers_t3, Res.string.lib_farmers_c3, 1350),
        createEntry("farmers", 4, Res.string.lib_farmers_t4, Res.string.lib_farmers_c4, 1800),
        createEntry("farmers", 5, Res.string.lib_farmers_t5, Res.string.lib_farmers_c5, 2250),
        createEntry("farmers", 6, Res.string.lib_farmers_t6, Res.string.lib_farmers_c6, 2700),
        createEntry("farmers", 7, Res.string.lib_farmers_t7, Res.string.lib_farmers_c7, 3150),
        createEntry("farmers", 8, Res.string.lib_farmers_t8, Res.string.lib_farmers_c8, 3600),
        createEntry("farmers", 9, Res.string.lib_farmers_t9, Res.string.lib_farmers_c9, 4050),
        createEntry("farmers", 10, Res.string.lib_farmers_t10, Res.string.lib_farmers_c10, 4500)
    )

    val pesticidesEntries = listOf(
        createEntry("pesticides", 1, Res.string.lib_pesticides_t1, Res.string.lib_pesticides_c1, 600),
        createEntry("pesticides", 2, Res.string.lib_pesticides_t2, Res.string.lib_pesticides_c2, 1200),
        createEntry("pesticides", 3, Res.string.lib_pesticides_t3, Res.string.lib_pesticides_c3, 1800),
        createEntry("pesticides", 4, Res.string.lib_pesticides_t4, Res.string.lib_pesticides_c4, 2400),
        createEntry("pesticides", 5, Res.string.lib_pesticides_t5, Res.string.lib_pesticides_c5, 3000),
        createEntry("pesticides", 6, Res.string.lib_pesticides_t6, Res.string.lib_pesticides_c6, 3600),
        createEntry("pesticides", 7, Res.string.lib_pesticides_t7, Res.string.lib_pesticides_c7, 4200),
        createEntry("pesticides", 8, Res.string.lib_pesticides_t8, Res.string.lib_pesticides_c8, 4800),
        createEntry("pesticides", 9, Res.string.lib_pesticides_t9, Res.string.lib_pesticides_c9, 5400),
        createEntry("pesticides", 10, Res.string.lib_pesticides_t10, Res.string.lib_pesticides_c10, 6000)
    )

    val geneticEntries = listOf(
        createEntry("genetic", 1, Res.string.lib_genetic_t1, Res.string.lib_genetic_c1, 1000),
        createEntry("genetic", 2, Res.string.lib_genetic_t2, Res.string.lib_genetic_c2, 2000),
        createEntry("genetic", 3, Res.string.lib_genetic_t3, Res.string.lib_genetic_c3, 3000),
        createEntry("genetic", 4, Res.string.lib_genetic_t4, Res.string.lib_genetic_c4, 4000),
        createEntry("genetic", 5, Res.string.lib_genetic_t5, Res.string.lib_genetic_c5, 5000),
        createEntry("genetic", 6, Res.string.lib_genetic_t6, Res.string.lib_genetic_c6, 6000),
        createEntry("genetic", 7, Res.string.lib_genetic_t7, Res.string.lib_genetic_c7, 7000),
        createEntry("genetic", 8, Res.string.lib_genetic_t8, Res.string.lib_genetic_c8, 8000),
        createEntry("genetic", 9, Res.string.lib_genetic_t9, Res.string.lib_genetic_c9, 9000),
        createEntry("genetic", 10, Res.string.lib_genetic_t10, Res.string.lib_genetic_c10, 10000)
    )

    /** Full list of categories available in the library. */
    val categories = listOf(
        LibraryCategory("tomato", Res.string.item_tomato, "🍅", tomatoEntries),
        LibraryCategory("broccoli", Res.string.item_broccoli, "🥦", broccoliEntries),
        LibraryCategory("bell_pepper", Res.string.item_bell_pepper, "🫑", bellPepperEntries),
        LibraryCategory("garlic", Res.string.item_garlic, "🧄", garlicEntries),
        LibraryCategory("purple_onion", Res.string.item_purple_onion, "🧅", purpleOnionEntries),
        LibraryCategory("squash", Res.string.item_squash, "🥒", squashEntries),
        LibraryCategory("apple", Res.string.item_apple, "🍎", appleEntries),
        LibraryCategory("plagues", Res.string.lib_cat_plagues, "🐛", plaguesEntries),
        LibraryCategory("farmers", Res.string.lib_cat_farmers, "👨‍🌾", farmersEntries),
        LibraryCategory("pesticides", Res.string.lib_cat_pesticides, "🧪", pesticidesEntries),
        LibraryCategory("genetic", Res.string.lib_cat_genetic, "🧬", geneticEntries)
    )
}
