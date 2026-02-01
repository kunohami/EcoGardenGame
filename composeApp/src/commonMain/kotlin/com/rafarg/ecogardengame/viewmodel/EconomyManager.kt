package com.rafarg.ecogardengame.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rafarg.ecogardengame.model.ItemCost
import com.rafarg.ecogardengame.model.Reward

class EconomyManager {
    var totalClicks by mutableStateOf(0)
    var money by mutableStateOf(0)
    var totalMoneyEarned by mutableStateOf(0)
    var fruitCounts by mutableStateOf<Map<String, Int>>(emptyMap())
    var totalFruitHarvested by mutableStateOf<Map<String, Int>>(emptyMap())

    fun addRewards(rewards: List<Reward>, currentItemId: String) {
        val newFruitCounts = fruitCounts.toMutableMap()
        val newTotalHarvested = totalFruitHarvested.toMutableMap()
        var moneyGain = 0

        rewards.forEach { reward ->
            moneyGain += reward.moneyValue
            if (reward.countValue > 0) {
                newFruitCounts[currentItemId] = (newFruitCounts[currentItemId] ?: 0) + reward.countValue
                newTotalHarvested[currentItemId] = (newTotalHarvested[currentItemId] ?: 0) + reward.countValue
            }
        }

        money += moneyGain
        totalMoneyEarned += moneyGain
        fruitCounts = newFruitCounts
        totalFruitHarvested = newTotalHarvested
    }

    fun canAfford(cost: ItemCost): Boolean {
        if (money < cost.money) return false
        cost.vegetableCosts.forEach { (vegId, amount) ->
            if ((fruitCounts[vegId] ?: 0) < amount) return false
        }
        return true
    }

    fun spend(cost: ItemCost) {
        money -= cost.money
        val newCounts = fruitCounts.toMutableMap()
        cost.vegetableCosts.forEach { (vegId, amount) ->
            newCounts[vegId] = (newCounts[vegId] ?: 0) - amount
        }
        fruitCounts = newCounts
    }

    fun addClicks(count: Int = 1) {
        totalClicks += count
    }

    fun applySaveData(
        clicks: Int,
        currentMoney: Int,
        totalMoney: Int,
        counts: Map<String, Int>,
        totalHarvested: Map<String, Int>
    ) {
        totalClicks = clicks
        money = currentMoney
        totalMoneyEarned = totalMoney
        fruitCounts = counts
        totalFruitHarvested = totalHarvested
    }

    fun reset() {
        totalClicks = 0
        money = 0
        totalMoneyEarned = 0
        fruitCounts = emptyMap()
        totalFruitHarvested = emptyMap()
    }
    
    fun debugAddResources() {
        money += 100000
        totalMoneyEarned += 100000
        // Note: Individual item counts update would need item list, handled in ViewModel or passed here
    }
}
