package com.rafarg.ecogardengame.viewmodel

import com.rafarg.ecogardengame.model.GlobalUpgrade
import com.rafarg.ecogardengame.model.Reward
import kotlin.random.Random

/**
 * Utility object responsible for calculating final rewards based on base values,
 * global upgrades, and active weather conditions.
 */
object RewardCalculator {
    /**
     * Calculates the final rewards for a harvest event.
     *
     * @param baseRewards The initial reward values for the item.
     * @param globalUpgrades List of all global upgrades to check for active levels.
     * @param globalClickCounter Total clicks in the session, used for interval-based bonuses.
     * @param isWeatherBonusActive Whether weather bonuses should be applied.
     * @param temperature Current temperature in Celsius.
     * @param isSunny Whether the current weather is clear/sunny.
     * @param isSnowing Whether it is currently snowing.
     * @param currentId The ID of the vegetable being harvested.
     * @param isCloudy Whether it is currently cloudy.
     * @return A list of modified Rewards with final money and count values.
     */
    fun calculateRewards(
        baseRewards: List<Reward>,
        globalUpgrades: List<GlobalUpgrade>,
        globalClickCounter: Int,
        isWeatherBonusActive: Boolean,
        temperature: Double,
        isSunny: Boolean,
        isSnowing: Boolean,
        currentId: String,
        isCloudy: Boolean,
    ): List<Reward> {
        var finalRewards = baseRewards.map { it.copy() }

        // --- GLOBAL UPGRADES ---

        // Lucky Harvest: Small chance to get 10x rewards.
        val luckyLevel = globalUpgrades.find { it.id == "lucky_harvest" }?.unlockedLevel ?: 0
        if (luckyLevel > 0) {
            val chance = luckyLevel / 100f
            if (Random.nextFloat() < chance) {
                finalRewards =
                    finalRewards.map {
                        it.copy(moneyValue = it.moneyValue * 10, countValue = it.countValue * 10, isLucky = true)
                    }
            }
        }

        // Precise Harvest: Guarantees double rewards at specific click intervals.
        val preciseLevel = globalUpgrades.find { it.id == "double_click_10" }?.unlockedLevel ?: 0
        if (preciseLevel > 0) {
            val isDouble = globalClickCounter % (11 - preciseLevel * 2) == 0
            if (isDouble) {
                finalRewards = finalRewards.map { it.copy(moneyValue = it.moneyValue * 2, countValue = it.countValue * 2) }
            }
        }

        // --- WEATHER BONUSES ---
        if (isWeatherBonusActive) {
            // Temperature Bonus: Adds +1 money and +1 fruit to specific crops based on heat/cold.
            val isResistant = temperature < 12 && (currentId == "garlic" || currentId == "purple_onion")
            val isBalanced = temperature in 12.0..22.0 && (currentId == "broccoli" || currentId == "apple")
            val isFastRipening = temperature > 22 && (currentId == "tomato" || currentId == "bell_pepper" || currentId == "squash")

            if (isResistant || isBalanced || isFastRipening) {
                finalRewards =
                    finalRewards.map {
                        it.copy(
                            moneyValue = if (it.moneyValue > 0 || it.countValue == 0) it.moneyValue + 1 else it.moneyValue,
                            countValue = if (it.countValue > 0) it.countValue + 1 else it.countValue,
                        )
                    }
            }

            // Sunny Bonus (Photosynthesis): Every 5 clicks, grants a flat +2 money bonus.
            if (isSunny && globalClickCounter % 5 == 0) {
                var applied = false
                finalRewards =
                    finalRewards.map {
                        if (!applied && (it.moneyValue > 0 || it.countValue == 0)) {
                            applied = true
                            it.copy(moneyValue = it.moneyValue + 2)
                        } else {
                            it
                        }
                    }
            }

            // Snow Bonus (Hibernation): Doubles rewards for Garlic specifically.
            if (isSnowing && currentId == "garlic") {
                finalRewards = finalRewards.map { it.copy(moneyValue = it.moneyValue * 2, countValue = it.countValue * 2) }
            }
        }

        return finalRewards
    }
}
