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
import com.rafarg.ecogardengame.ui.items
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameViewModel(private val dataStore: DataStore<Preferences>?) : ViewModel() {

    // --- CURRENCIES ---
    var totalClicks by mutableStateOf(0)
        private set

    var money by mutableStateOf(0)
        private set

    // Map to store count for each specific fruit/vegetable
    var fruitCounts by mutableStateOf<Map<String, Int>>(emptyMap())
        private set

    // --- STATE ---
    var currentItem by mutableStateOf<GameItem>(items.first())
        private set

    var itemsList by mutableStateOf(items)
        private set

    // --- DATASTORE KEYS ---
    private val totalClicksKey = intPreferencesKey("total_clicks")
    private val moneyKey = intPreferencesKey("money")

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            dataStore?.data?.first()?.let { prefs ->
                totalClicks = prefs[totalClicksKey] ?: 0
                money = prefs[moneyKey] ?: 0
                
                // Load fruit counts and unlocked status
                val newFruitCounts = mutableMapOf<String, Int>()
                val newList = itemsList.map { item ->
                    val countKey = intPreferencesKey("fruit_count_${item.id}")
                    val unlockedKey = booleanPreferencesKey("unlocked_${item.id}")
                    
                    newFruitCounts[item.id] = prefs[countKey] ?: 0
                    
                    // Tomato is always unlocked by default
                    val isUnlocked = if (item.id == "tomato") true else prefs[unlockedKey] ?: false
                    item.unlocked = isUnlocked
                    item
                }
                
                fruitCounts = newFruitCounts
                itemsList = newList
                currentItem = itemsList.find { it.id == currentItem.id } ?: itemsList.first()
            }
        }
    }

    fun onVegetableClick() {
        totalClicks++
        money++
        
        val currentId = currentItem.id
        val currentCount = fruitCounts[currentId] ?: 0
        fruitCounts = fruitCounts + (currentId to (currentCount + 1))

        saveData()
    }

    fun selectItem(item: GameItem) {
        if (item.unlocked) {
            currentItem = item
        }
    }

    fun tryUnlockItem(item: GameItem) {
        if (!item.unlocked && money >= item.price) {
            money -= item.price
            item.unlocked = true
            currentItem = item
            saveData()
        }
    }

    fun resetGame() {
        totalClicks = 0
        money = 0
        fruitCounts = emptyMap()
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
                
                itemsList.forEach { item ->
                    val countKey = intPreferencesKey("fruit_count_${item.id}")
                    val unlockedKey = booleanPreferencesKey("unlocked_${item.id}")
                    prefs[countKey] = fruitCounts[item.id] ?: 0
                    prefs[unlockedKey] = item.unlocked
                }
            }
        }
    }
}
