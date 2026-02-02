package com.rafarg.ecogardengame.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rafarg.ecogardengame.model.ItemCost
import com.rafarg.ecogardengame.model.Reward

/**
 * Manages the game economy, including currency (money), click counts, 
 * and fruit/vegetable inventory.
 */
class EconomyManager {
    /** Total number of clicks performed by the user across all sessions. */
    var totalClicks by mutableStateOf(0)
    
    /** Current available money to spend. */
    var money by mutableStateOf(0)
    
    /** Total money earned throughout the entire game. */
    var totalMoneyEarned by mutableStateOf(0)
    
    /** Current inventory counts for each type of fruit/vegetable. */
    var fruitCounts by mutableStateOf<Map<String, Int>>(emptyMap())
    
    /** Total amount harvested for each type of fruit/vegetable throughout the game. */
    var totalFruitHarvested by mutableStateOf<Map<String, Int>>(emptyMap())

    /**
     * Processes and adds rewards (money and items) to the user's account.
     * @param rewards The list of rewards to be added.
     * @param currentItemId The ID of the item currently being harvested.
     */
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

    /**
     * Checks if the user has enough resources to afford a certain cost.
     * @param cost The required money and items.
     * @return True if the user can afford it, false otherwise.
     */
    fun canAfford(cost: ItemCost): Boolean {
        if (money < cost.money) return false
        cost.vegetableCosts.forEach { (vegId, amount) ->
            if ((fruitCounts[vegId] ?: 0) < amount) return false
        }
        return true
    }

    /**
     * Deducts the specified cost from the user's resources.
     * Assumes canAfford has been called before.
     * @param cost The resources to be deducted.
     */
    fun spend(cost: ItemCost) {
        money -= cost.money
        val newCounts = fruitCounts.toMutableMap()
        cost.vegetableCosts.forEach { (vegId, amount) ->
            newCounts[vegId] = (newCounts[vegId] ?: 0) - amount
        }
        fruitCounts = newCounts
    }

    /**
     * Increments the total click counter.
     */
    fun addClicks(count: Int = 1) {
        totalClicks += count
    }

    /**
     * Applies loaded save data to the current session.
     */
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

    /**
     * Resets all economic progress to zero.
     */
    fun reset() {
        totalClicks = 0
        money = 0
        totalMoneyEarned = 0
        fruitCounts = emptyMap()
        totalFruitHarvested = emptyMap()
    }
    
    /**
     * Debug function to grant a large amount of resources for testing.
     */
    fun debugAddResources() {
        money += 100000
        totalMoneyEarned += 100000
    }
}
