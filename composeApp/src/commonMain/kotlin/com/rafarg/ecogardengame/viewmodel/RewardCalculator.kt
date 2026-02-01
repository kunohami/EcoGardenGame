package com.rafarg.ecogardengame.viewmodel

import com.rafarg.ecogardengame.model.GlobalUpgrade
import com.rafarg.ecogardengame.model.Reward
import kotlin.random.Random

object RewardCalculator {

    fun calculateRewards(
        baseRewards: List<Reward>,
        globalUpgrades: List<GlobalUpgrade>,
        globalClickCounter: Int,
        isWeatherBonusActive: Boolean,
        temperature: Double,
        isSunny: Boolean,
        isSnowing: Boolean,
        currentId: String,
        isCloudy: Boolean
    ): List<Reward> {
        var finalRewards = baseRewards.map { it.copy() }

        // --- GLOBAL UPGRADES ---
        val luckyLevel = globalUpgrades.find { it.id == "lucky_harvest" }?.unlockedLevel ?: 0
        if (luckyLevel > 0) {
            val chance = luckyLevel / 100f
            if (Random.nextFloat() < chance) {
                finalRewards = finalRewards.map { it.copy(moneyValue = it.moneyValue * 10, countValue = it.countValue * 10, isLucky = true) }
            }
        }

        val preciseLevel = globalUpgrades.find { it.id == "double_click_10" }?.unlockedLevel ?: 0
        if (preciseLevel > 0) {
            val isDouble = globalClickCounter % (11 - preciseLevel * 2) == 0
            if (isDouble) {
                finalRewards = finalRewards.map { it.copy(moneyValue = it.moneyValue * 2, countValue = it.countValue * 2) }
            }
        }

        // --- WEATHER BONUSES ---
        if (isWeatherBonusActive) {
            // Temperature Bonus (+1 money and +1 fruit)
            val isResistant = temperature < 12 && (currentId == "garlic" || currentId == "purple_onion")
            val isBalanced = temperature in 12.0..22.0 && (currentId == "broccoli" || currentId == "apple")
            val isFastRipening = temperature > 22 && (currentId == "tomato" || currentId == "bell_pepper" || currentId == "squash")

            if (isResistant || isBalanced || isFastRipening) {
                finalRewards = finalRewards.map {
                    it.copy(
                        moneyValue = if (it.moneyValue > 0 || it.countValue == 0) it.moneyValue + 1 else it.moneyValue,
                        countValue = if (it.countValue > 0) it.countValue + 1 else it.countValue
                    )
                }
            }

            // Sunny Bonus: Photosynthesis (every 5 clicks, +2 money)
            if (isSunny && globalClickCounter % 5 == 0) {
                var applied = false
                finalRewards = finalRewards.map {
                    if (!applied && (it.moneyValue > 0 || it.countValue == 0)) {
                        applied = true
                        it.copy(moneyValue = it.moneyValue + 2)
                    } else it
                }
            }

            // Snow Bonus: Hibernation (Garlic x2 multiplier)
            if (isSnowing && currentId == "garlic") {
                finalRewards = finalRewards.map { it.copy(moneyValue = it.moneyValue * 2, countValue = it.countValue * 2) }
            }
        }

        return finalRewards
    }
}
