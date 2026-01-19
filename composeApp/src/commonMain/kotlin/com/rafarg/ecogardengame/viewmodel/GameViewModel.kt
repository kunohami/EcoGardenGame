package com.rafarg.ecogardengame.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafarg.ecogardengame.model.GameItem
import com.rafarg.ecogardengame.model.GameplayModifier
import com.rafarg.ecogardengame.model.Reward
import com.rafarg.ecogardengame.ui.items
import com.rafarg.ecogardengame.util.vibrate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

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

    // --- CPS LOGIC (Clicks Per Second) ---
    private val clickTimestamps = mutableStateListOf<Long>()
    var currentCps by mutableStateOf(0.0f)
        private set

    // --- VIBRATION SETTINGS ---
    var vibrationEnabled by mutableStateOf(false)
        private set
    var vibrationIntensity by mutableStateOf(10f) // Milliseconds
        private set

    // --- THEME SETTINGS ---
    var isDarkTheme by mutableStateOf(false)
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
    private val vibrationEnabledKey = booleanPreferencesKey("vibration_enabled")
    private val vibrationIntensityKey = floatPreferencesKey("vibration_intensity")
    private val darkThemeKey = booleanPreferencesKey("dark_theme")

    init {
        loadData()
        startCpsTracker()
    }

    private fun startCpsTracker() {
        viewModelScope.launch {
            while (isActive) {
                val now = currentTimeMillis()
                val oneSecondAgo = now - 1000
                // Remove timestamps older than 1 second
                while (clickTimestamps.isNotEmpty() && clickTimestamps.first() < oneSecondAgo) {
                    clickTimestamps.removeAt(0)
                }
                currentCps = clickTimestamps.size.toFloat()
                delay(100) // Update more frequently for CPS
            }
        }
    }

    private fun currentTimeMillis(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }

    private fun loadData() {
        viewModelScope.launch {
            dataStore?.data?.first()?.let { prefs ->
                totalClicks = prefs[totalClicksKey] ?: 0
                money = prefs[moneyKey] ?: 0
                totalMoneyEarned = prefs[totalMoneyEarnedKey] ?: 0
                vibrationEnabled = prefs[vibrationEnabledKey] ?: false
                vibrationIntensity = prefs[vibrationIntensityKey] ?: 10f
                isDarkTheme = prefs[darkThemeKey] ?: false
                
                val newFruitCounts = mutableMapOf<String, Int>()
                val newTotalHarvested = mutableMapOf<String, Int>()
                
                val newList = itemsList.map { item ->
                    val countKey = intPreferencesKey("fruit_count_${item.id}")
                    val totalHarvestedKey = intPreferencesKey("total_harvested_${item.id}")
                    val unlockedKey = booleanPreferencesKey("unlocked_${item.id}")
                    
                    newFruitCounts[item.id] = prefs[countKey] ?: 0
                    newTotalHarvested[item.id] = prefs[totalHarvestedKey] ?: 0
                    
                    item.unlocked = if (item.id == "tomato") true else prefs[unlockedKey] ?: false
                    
                    // Load Modifiers
                    item.modifiers.forEach { mod ->
                        mod.isUnlocked = prefs[booleanPreferencesKey("mod_unlocked_${mod.id}")] ?: false
                        mod.isEnabled = prefs[booleanPreferencesKey("mod_enabled_${mod.id}")] ?: false
                    }
                    
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
        clickTimestamps.add(currentTimeMillis())
        
        if (vibrationEnabled) {
            vibrate(vibrationIntensity.toLong())
        }

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

    fun setVibration(enabled: Boolean) {
        vibrationEnabled = enabled
        saveData()
    }

    fun updateVibrationIntensity(intensity: Float) {
        vibrationIntensity = intensity
        saveData()
    }

    fun setTheme(dark: Boolean) {
        isDarkTheme = dark
        saveData()
    }

    fun selectItem(item: GameItem) {
        if (item.unlocked) {
            currentItem = item
        }
    }

    fun canAfford(cost: com.rafarg.ecogardengame.model.ItemCost): Boolean {
        if (money < cost.money) return false
        cost.vegetableCosts.forEach { (vegId, amount) ->
            if ((fruitCounts[vegId] ?: 0) < amount) return false
        }
        return true
    }

    fun tryUnlockItem(item: GameItem) {
        if (!item.unlocked && canAfford(item.unlockCost)) {
            money -= item.unlockCost.money
            
            val newCounts = fruitCounts.toMutableMap()
            item.unlockCost.vegetableCosts.forEach { (vegId, amount) ->
                newCounts[vegId] = (newCounts[vegId] ?: 0) - amount
            }
            fruitCounts = newCounts
            
            item.unlocked = true
            currentItem = item 
            saveData()
        }
    }

    fun tryUnlockModifier(modifier: GameplayModifier) {
        if (!modifier.isUnlocked && canAfford(modifier.unlockCost)) {
            money -= modifier.unlockCost.money
            val newCounts = fruitCounts.toMutableMap()
            modifier.unlockCost.vegetableCosts.forEach { (vegId, amount) ->
                newCounts[vegId] = (newCounts[vegId] ?: 0) - amount
            }
            fruitCounts = newCounts
            modifier.isUnlocked = true
            modifier.isEnabled = true
            saveData()
        }
    }

    fun toggleModifier(modifier: GameplayModifier) {
        if (modifier.isUnlocked) {
            modifier.isEnabled = !modifier.isEnabled
            saveData()
        }
    }

    fun debugAddResources() {
        money += 100000
        totalMoneyEarned += 100000
        
        val newCounts = fruitCounts.toMutableMap()
        val newTotals = totalFruitHarvested.toMutableMap()
        
        itemsList.forEach { item ->
            newCounts[item.id] = (newCounts[item.id] ?: 0) + 100000
            newTotals[item.id] = (newTotals[item.id] ?: 0) + 100000
        }
        
        fruitCounts = newCounts
        totalFruitHarvested = newTotals
        saveData()
    }

    fun resetGame() {
        totalClicks = 0
        money = 0
        totalMoneyEarned = 0
        fruitCounts = emptyMap()
        totalFruitHarvested = emptyMap()
        itemsList = items.mapIndexed { index, item ->
            item.apply { 
                unlocked = (index == 0)
                modifiers.forEach { 
                    it.isUnlocked = false
                    it.isEnabled = false
                }
            }
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
                prefs[vibrationEnabledKey] = vibrationEnabled
                prefs[vibrationIntensityKey] = vibrationIntensity
                prefs[darkThemeKey] = isDarkTheme
                
                itemsList.forEach { item ->
                    val countKey = intPreferencesKey("fruit_count_${item.id}")
                    val totalHarvestedKey = intPreferencesKey("total_harvested_${item.id}")
                    val unlockedKey = booleanPreferencesKey("unlocked_${item.id}")
                    
                    prefs[countKey] = fruitCounts[item.id] ?: 0
                    prefs[totalHarvestedKey] = totalFruitHarvested[item.id] ?: 0
                    prefs[unlockedKey] = item.unlocked
                    
                    item.modifiers.forEach { mod ->
                        prefs[booleanPreferencesKey("mod_unlocked_${mod.id}")] = mod.isUnlocked
                        prefs[booleanPreferencesKey("mod_enabled_${mod.id}")] = mod.isEnabled
                    }
                }
            }
        }
    }
}
