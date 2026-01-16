package com.rafarg.ecogardengame.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafarg.ecogardengame.model.GameItem
import com.rafarg.ecogardengame.model.Reward
import com.rafarg.ecogardengame.ui.items
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameViewModel(private val dataStore: DataStore<Preferences>?) : ViewModel() {

    // --- CURRENT CURRENCIES ---
    var totalClicks by mutableStateOf(0)
        private set

    var money by mutableStateOf(0)
        private set

    var fruitCounts by mutableStateOf<Map<String, Int>>(emptyMap())
        private set

    // --- TOTAL COLLECTED (Stats Only) ---
    var totalMoneyEarned by mutableStateOf(0)
        private set

    var totalFruitHarvested by mutableStateOf<Map<String, Int>>(emptyMap())
        private set

    // --- STATE ---
    var currentItem by mutableStateOf<GameItem>(items.first())
        private set

    var itemsList by mutableStateOf(items)
        private set

    // --- DATASTORE KEYS ---
    private val totalClicksKey = intPreferencesKey("total_clicks")
    private val moneyKey = intPreferencesKey("money")
    private val totalMoneyEarnedKey = intPreferencesKey("total_money_earned")

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            dataStore?.data?.first()?.let { prefs ->
                totalClicks = prefs[totalClicksKey] ?: 0
                money = prefs[moneyKey] ?: 0
                totalMoneyEarned = prefs[totalMoneyEarnedKey] ?: 0
                
                val newFruitCounts = mutableMapOf<String, Int>()
                val newTotalHarvested = mutableMapOf<String, Int>()
                
                val newList = itemsList.map { item ->
                    val countKey = intPreferencesKey("fruit_count_${item.id}")
                    val totalHarvestedKey = intPreferencesKey("total_harvested_${item.id}")
                    val unlockedKey = booleanPreferencesKey("unlocked_${item.id}")
                    
                    newFruitCounts[item.id] = prefs[countKey] ?: 0
                    newTotalHarvested[item.id] = prefs[totalHarvestedKey] ?: 0
                    
                    val isUnlocked = if (item.id == "tomato") true else prefs[unlockedKey] ?: false
                    item.unlocked = isUnlocked
                    item
                }
                
                fruitCounts = newFruitCounts
                totalFruitHarvested = newTotalHarvested
                itemsList = newList
                currentItem = itemsList.find { it.id == currentItem.id } ?: itemsList.first()
            }
        }
    }

    fun onVegetableClick(rewards: List<Reward>) {
        totalClicks++
        
        val newFruitCounts = fruitCounts.toMutableMap()
        val newTotalHarvested = totalFruitHarvested.toMutableMap()
        var moneyGain = 0

        rewards.forEach { reward ->
            moneyGain += reward.moneyValue
            
            if (reward.countValue > 0) {
                val currentId = currentItem.id
                
                // Update Current
                newFruitCounts[currentId] = (newFruitCounts[currentId] ?: 0) + reward.countValue
                
                // Update Total
                newTotalHarvested[currentId] = (newTotalHarvested[currentId] ?: 0) + reward.countValue
            }
        }

        money += moneyGain
        totalMoneyEarned += moneyGain
        fruitCounts = newFruitCounts
        totalFruitHarvested = newTotalHarvested

        saveData()
    }

    fun selectItem(item: GameItem) {
        if (item.unlocked) {
            currentItem = item
        }
    }

    fun canAfford(item: GameItem): Boolean {
        if (money < item.unlockCost.money) return false
        item.unlockCost.vegetableCosts.forEach { (vegId, amount) ->
            if ((fruitCounts[vegId] ?: 0) < amount) return false
        }
        return true
    }

    fun tryUnlockItem(item: GameItem) {
        if (!item.unlocked && canAfford(item)) {
            money -= item.unlockCost.money
            
            val newCounts = fruitCounts.toMutableMap()
            item.unlockCost.vegetableCosts.forEach { (vegId, amount) ->
                newCounts[vegId] = (newCounts[vegId] ?: 0) - amount
            }
            fruitCounts = newCounts
            
            item.unlocked = true
            saveData()
        }
    }

    fun resetGame() {
        totalClicks = 0
        money = 0
        totalMoneyEarned = 0
        fruitCounts = emptyMap()
        totalFruitHarvested = emptyMap()
        itemsList = items.mapIndexed { index, item ->
            item.apply { unlocked = (index == 0) }
        }
        currentItem = itemsList.first()
        saveData()
    }

    private fun saveData() {
        viewModelScope.launch {
            dataStore?.edit { prefs ->
                prefs[totalClicksKey] = totalClicks
                prefs[moneyKey] = money
                prefs[totalMoneyEarnedKey] = totalMoneyEarned
                
                itemsList.forEach { item ->
                    val countKey = intPreferencesKey("fruit_count_${item.id}")
                    val totalHarvestedKey = intPreferencesKey("total_harvested_${item.id}")
                    val unlockedKey = booleanPreferencesKey("unlocked_${item.id}")
                    
                    prefs[countKey] = fruitCounts[item.id] ?: 0
                    prefs[totalHarvestedKey] = totalFruitHarvested[item.id] ?: 0
                    prefs[unlockedKey] = item.unlocked
                }
            }
        }
    }
}
