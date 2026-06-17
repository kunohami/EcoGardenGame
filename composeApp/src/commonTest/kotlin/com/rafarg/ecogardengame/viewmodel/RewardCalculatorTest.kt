package com.rafarg.ecogardengame.viewmodel

import com.rafarg.ecogardengame.model.GlobalUpgrade
import com.rafarg.ecogardengame.model.ItemCost
import com.rafarg.ecogardengame.model.Reward
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.about_title
import kotlin.test.Test
import kotlin.test.assertEquals

class RewardCalculatorTest {
    private fun globalUpgrade(
        id: String,
        unlockedLevel: Int,
    ) = GlobalUpgrade(
        id = id,
        nameRes = Res.string.about_title,
        descriptionRes = Res.string.about_title,
        baseCost = ItemCost(),
        unlockedLevelInitial = unlockedLevel,
    )

    private fun vegetableRewards() =
        listOf(
            Reward(emoji = "🧄", moneyValue = 0, countValue = 1),
            Reward(emoji = "🪙", moneyValue = 1, countValue = 0),
        )

    @Test
    fun noBonusesWhenWeatherInactiveAndUpgradesUnlevelled() {
        val result =
            RewardCalculator.calculateRewards(
                baseRewards = vegetableRewards(),
                globalUpgrades = listOf(globalUpgrade("lucky_harvest", 0), globalUpgrade("double_click_10", 0)),
                globalClickCounter = 1,
                isWeatherBonusActive = false,
                temperature = 20.0,
                isSunny = false,
                isSnowing = false,
                currentId = "garlic",
                isCloudy = false,
            )

        assertEquals(vegetableRewards(), result)
    }

    @Test
    fun preciseHarvestDoublesOnlyAtTheRightClickInterval() {
        val upgrades = listOf(globalUpgrade("double_click_10", 1))

        val onInterval =
            RewardCalculator.calculateRewards(
                baseRewards = vegetableRewards(),
                globalUpgrades = upgrades,
                globalClickCounter = 9,
                isWeatherBonusActive = false,
                temperature = 20.0,
                isSunny = false,
                isSnowing = false,
                currentId = "garlic",
                isCloudy = false,
            )
        assertEquals(listOf(0, 2), onInterval.map { it.moneyValue })
        assertEquals(listOf(2, 0), onInterval.map { it.countValue })

        val offInterval =
            RewardCalculator.calculateRewards(
                baseRewards = vegetableRewards(),
                globalUpgrades = upgrades,
                globalClickCounter = 1,
                isWeatherBonusActive = false,
                temperature = 20.0,
                isSunny = false,
                isSnowing = false,
                currentId = "garlic",
                isCloudy = false,
            )
        assertEquals(vegetableRewards(), offInterval)
    }

    @Test
    fun luckyHarvestIsNoOpAtLevelZero() {
        val result =
            RewardCalculator.calculateRewards(
                baseRewards = vegetableRewards(),
                globalUpgrades = listOf(globalUpgrade("lucky_harvest", 0)),
                globalClickCounter = 1,
                isWeatherBonusActive = false,
                temperature = 20.0,
                isSunny = false,
                isSnowing = false,
                currentId = "garlic",
                isCloudy = false,
            )

        assertEquals(vegetableRewards(), result)
    }

    @Test
    fun temperatureBonusAppliesToColdResistantCrops() {
        val result =
            RewardCalculator.calculateRewards(
                baseRewards = vegetableRewards(),
                globalUpgrades = emptyList(),
                globalClickCounter = 1,
                isWeatherBonusActive = true,
                temperature = 5.0,
                isSunny = false,
                isSnowing = false,
                currentId = "garlic",
                isCloudy = false,
            )

        assertEquals(listOf(0, 2), result.map { it.moneyValue })
        assertEquals(listOf(2, 0), result.map { it.countValue })
    }

    @Test
    fun temperatureBonusDoesNotApplyToUnrelatedCrop() {
        val result =
            RewardCalculator.calculateRewards(
                baseRewards = vegetableRewards(),
                globalUpgrades = emptyList(),
                globalClickCounter = 1,
                isWeatherBonusActive = true,
                temperature = 5.0,
                isSunny = false,
                isSnowing = false,
                currentId = "tomato",
                isCloudy = false,
            )

        assertEquals(vegetableRewards(), result)
    }

    @Test
    fun sunnyBonusAppliesFlatTwoMoneyEveryFiveClicks() {
        val result =
            RewardCalculator.calculateRewards(
                baseRewards = vegetableRewards(),
                globalUpgrades = emptyList(),
                globalClickCounter = 5,
                isWeatherBonusActive = true,
                temperature = 20.0,
                isSunny = true,
                isSnowing = false,
                currentId = "garlic",
                isCloudy = false,
            )

        assertEquals(listOf(0, 3), result.map { it.moneyValue })
        assertEquals(listOf(1, 0), result.map { it.countValue })
    }

    @Test
    fun sunnyBonusDoesNotApplyOffInterval() {
        val result =
            RewardCalculator.calculateRewards(
                baseRewards = vegetableRewards(),
                globalUpgrades = emptyList(),
                globalClickCounter = 4,
                isWeatherBonusActive = true,
                temperature = 20.0,
                isSunny = true,
                isSnowing = false,
                currentId = "garlic",
                isCloudy = false,
            )

        assertEquals(vegetableRewards(), result)
    }

    @Test
    fun snowBonusDoublesGarlicRewardsOnly() {
        val garlicResult =
            RewardCalculator.calculateRewards(
                baseRewards = vegetableRewards(),
                globalUpgrades = emptyList(),
                globalClickCounter = 1,
                isWeatherBonusActive = true,
                temperature = 20.0,
                isSunny = false,
                isSnowing = true,
                currentId = "garlic",
                isCloudy = false,
            )
        assertEquals(listOf(0, 2), garlicResult.map { it.moneyValue })
        assertEquals(listOf(2, 0), garlicResult.map { it.countValue })

        val otherCropResult =
            RewardCalculator.calculateRewards(
                baseRewards = vegetableRewards(),
                globalUpgrades = emptyList(),
                globalClickCounter = 1,
                isWeatherBonusActive = true,
                temperature = 20.0,
                isSunny = false,
                isSnowing = true,
                currentId = "tomato",
                isCloudy = false,
            )
        assertEquals(vegetableRewards(), otherCropResult)
    }
}
