package com.rafarg.ecogardengame.model

/**
 * Centralized game prices, costs and reward values.
 * This object serves as a single source of truth for the game's economic balance.
 */
object GamePrices {
    
    // --- VEGETABLE UNLOCK COSTS ---
    val UNLOCK_TOMATO = ItemCost(money = 0)
    val UNLOCK_BROCCOLI = ItemCost(money = 100, vegetableCosts = mapOf("tomato" to 20))
    val UNLOCK_BELL_PEPPER = ItemCost(money = 250, vegetableCosts = mapOf("tomato" to 50, "broccoli" to 10))
    val UNLOCK_GARLIC = ItemCost(money = 750, vegetableCosts = mapOf("tomato" to 150, "broccoli" to 50, "bell_pepper" to 20, "purple_onion" to 15))
    val UNLOCK_PURPLE_ONION = ItemCost(money = 500, vegetableCosts = mapOf("tomato" to 100, "broccoli" to 25, "bell_pepper" to 10))
    val UNLOCK_SQUASH = ItemCost(money = 1000, vegetableCosts = mapOf("tomato" to 200, "broccoli" to 75, "bell_pepper" to 30, "purple_onion" to 15))
    val UNLOCK_APPLE = ItemCost(money = 1500, vegetableCosts = mapOf("tomato" to 400, "squash" to 75, "bell_pepper" to 30, "purple_onion" to 15))

    // --- GLOBAL UPGRADES ---
    val UPGRADE_PRECISE_HARVEST = ItemCost(money = 1000, vegetableCosts = mapOf("tomato" to 100))
    val UPGRADE_LUCKY_HARVEST = ItemCost(money = 1500, vegetableCosts = mapOf("apple" to 50, "garlic" to 50))
    val UPGRADE_WEATHER_BONUS = ItemCost(money = 1000)

    // --- MODIFIER COSTS ---
    val MOD_TOMATO_HAPTIC = ItemCost(money = 300, vegetableCosts = mapOf("tomato" to 150))
    val MOD_BROCCOLI_GIANT = ItemCost(money = 500, vegetableCosts = mapOf("broccoli" to 100))
    val MOD_BROCCOLI_OVERCLOCKED = ItemCost(money = 1500, vegetableCosts = mapOf("broccoli" to 250, "bell_pepper" to 50))
    val MOD_BROCCOLI_AIR = ItemCost(money = 69, vegetableCosts = mapOf("broccoli" to 69, "garlic" to 69))
    val MOD_BELL_PEPPER_TURBO = ItemCost(money = 1000, vegetableCosts = mapOf("bell_pepper" to 100, "broccoli" to 50))
    val MOD_GARLIC_CLUSTER = ItemCost(money = 1500, vegetableCosts = mapOf("garlic" to 150, "purple_onion" to 30))
    val MOD_GARLIC_SHAKE = ItemCost(money = 700, vegetableCosts = mapOf("garlic" to 300, "squash" to 20))
    val MOD_ONION_PLUS1 = ItemCost(money = 650, vegetableCosts = mapOf("purple_onion" to 50))
    val MOD_ONION_PLUS2 = ItemCost(money = 1200, vegetableCosts = mapOf("purple_onion" to 150, "bell_pepper" to 30))
    val MOD_ONION_STURDY = ItemCost(money = 1500, vegetableCosts = mapOf("purple_onion" to 100, "broccoli" to 40))
    val MOD_SQUASH_MOMENTUM = ItemCost(money = 750, vegetableCosts = mapOf("squash" to 50))
    val MOD_APPLE_HIGH_FREQ = ItemCost(money = 1000, vegetableCosts = mapOf("apple" to 200, "bell_pepper" to 40))

    // --- BASE CLICK REWARDS (Money) ---
    val REWARD_MONEY_TOMATO = 1
    val REWARD_MONEY_BROCCOLI = 2
    val REWARD_MONEY_BELL_PEPPER = 3
    val REWARD_MONEY_GARLIC = 1
    val REWARD_MONEY_PURPLE_ONION = 1
    val REWARD_MONEY_SQUASH = 1
    val REWARD_MONEY_APPLE = 1

    // --- SPECIAL REWARDS ---
    // Tomato Precision (Critical Hits)
    val REWARD_TOMATO_CRIT_MONEY = 30
    val REWARD_TOMATO_CRIT_COUNT = 20

    // Broccoli Overclocked Multiplier
    val MULTIPLIER_BROCCOLI_FAST = 2

    // Bell Pepper Turbo Multiplier
    val MULTIPLIER_BELL_PEPPER_TURBO = 2

    // Garlic Explosion Bonuses
    val REWARD_GARLIC_EXPLOSION_MONEY = 20
    val REWARD_GARLIC_EXPLOSION_COUNT = 10
    val MULTIPLIER_GARLIC_CLUSTER = 2.5f

    // Purple Onion Spinning Bonus
    val REWARD_ONION_SPIN_MONEY_BASE = 5

    // Apple High Frequency Bonus
    val REWARD_APPLE_HIGH_FREQ_MONEY = 5

    /**
     * Helper function to calculate the library price based on category and fact index.
     */
    fun libraryPrice(cat: String, index: Int): Int {
        return when(cat) {
            "tomato" -> index * 100
            "broccoli" -> index * 100
            "bell_pepper" -> index * 100
            "garlic" -> index * 100
            "purple_onion" -> index * 100
            "squash" -> index * 100
            "apple" -> index * 100
            "plagues" -> index * 150
            "farmers" -> index * 100
            "pesticides" -> index * 150
            "genetic" -> index * 150
            else -> index * 100
        }
    }
}
