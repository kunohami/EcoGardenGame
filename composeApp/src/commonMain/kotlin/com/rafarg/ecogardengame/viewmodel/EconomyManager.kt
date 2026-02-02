package com.rafarg.ecogardengame.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rafarg.ecogardengame.model.ItemCost
import com.rafarg.ecogardengame.model.Reward

/**
 * --- ENCAPSULATION (OOP Principle) ---
 * The EconomyManager class encapsulates all logic related to currency and inventory.
 * By isolating this logic, the ViewModel doesn't need to know 'how' money is added,
 * only that it needs to call a method to do so.
 */
class EconomyManager {
    
    /**
     * --- REACTIVE STATE ---
     * Using 'mutableStateOf' allows Compose to observe these variables.
     * When any of these change, the UI components that read them will 
     * automatically re-render (Recomposition).
     */
    var totalClicks by mutableStateOf(0)
    var money by mutableStateOf(0)
    var totalMoneyEarned by mutableStateOf(0)
    var fruitCounts by mutableStateOf<Map<String, Int>>(emptyMap())
    var totalFruitHarvested by mutableStateOf<Map<String, Int>>(emptyMap())

    /**
     * Processes a list of rewards and updates the player's balance.
     * 
     * @param rewards The list of [Reward] objects obtained from a click.
     * @param currentItemId The ID of the vegetable that produced these rewards.
     */
    fun addRewards(rewards: List<Reward>, currentItemId: String) {
        val newFruitCounts = fruitCounts.toMutableMap()
        val newTotalHarvested = totalFruitHarvested.toMutableMap()
        var moneyGain = 0

        // Iterate through each reward object to sum up the totals
        rewards.forEach { reward ->
            moneyGain += reward.moneyValue
            if (reward.countValue > 0) {
                // Update specific fruit inventory
                newFruitCounts[currentItemId] = (newFruitCounts[currentItemId] ?: 0) + reward.countValue
                newTotalHarvested[currentItemId] = (newTotalHarvested[currentItemId] ?: 0) + reward.countValue
            }
        }

        // Apply changes to the state variables
        money += moneyGain
        totalMoneyEarned += moneyGain
        fruitCounts = newFruitCounts
        totalFruitHarvested = newTotalHarvested
    }

    /**
     * Checks if the user has enough resources to afford a certain cost.
     * This is a "Validation" method used before allowing a purchase.
     * 
     * @param cost An [ItemCost] object defining required money and vegetables.
     * @return True if the user can afford the item, false otherwise.
     */
    fun canAfford(cost: ItemCost): Boolean {
        // 1. Check money balance
        if (money < cost.money) return false
        
        // 2. Check each required vegetable amount in the inventory map
        cost.vegetableCosts.forEach { (vegId, amount) ->
            if ((fruitCounts[vegId] ?: 0) < amount) return false
        }
        return true
    }

    /**
     * Deducts the specified cost from the user's resources.
     * Assumes 'canAfford' has been called and returned true.
     * 
     * @param cost The resources to be subtracted.
     */
    fun spend(cost: ItemCost) {
        money -= cost.money
        val newCounts = fruitCounts.toMutableMap()
        cost.vegetableCosts.forEach { (vegId, amount) ->
            newCounts[vegId] = (newCounts[vegId] ?: 0) - amount
        }
        fruitCounts = newCounts
    }

    /** Increments the total click counter for achievements. */
    fun addClicks(count: Int = 1) {
        totalClicks += count
    }

    /**
     * Data Hydration: Applies values loaded from persistent storage.
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

    /** Resets the economy state to the beginning of the game. */
    fun reset() {
        totalClicks = 0
        money = 0
        totalMoneyEarned = 0
        fruitCounts = emptyMap()
        totalFruitHarvested = emptyMap()
    }
    
    /** 
     * DEBUG METHOD
     * Grants resources for testing purposes.
     */
    fun debugAddResources() {
        money += 100000
        totalMoneyEarned += 100000
    }
}
