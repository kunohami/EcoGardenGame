package com.rafarg.ecogardengame.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafarg.ecogardengame.model.*
import com.rafarg.ecogardengame.ui.items as staticItemsList
import com.rafarg.ecogardengame.util.vibrate
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class GameViewModel(private val dataStore: DataStore<Preferences>?) : ViewModel(), GameItemProvider {

    // --- LOADING STATE ---
    var isDataLoaded by mutableStateOf(false)
        private set

    // --- CURRENT CURRENCIES ---
    override var totalClicks by mutableStateOf(0)
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
    
    // --- BACKGROUND SETTINGS ---
    var shaderBackgroundEnabled by mutableStateOf(false)
        private set

    // --- LANGUAGE SETTINGS ---
    var language by mutableStateOf("auto") // "auto", "en", "es"
        private set
    var languageSet by mutableStateOf(false)
        private set

    // --- GLOBAL UPGRADES ---
    override var globalUpgrades = listOf(
        GlobalUpgrade(
            id = "double_click_10",
            nameRes = Res.string.upg_precise_harvest_name,
            descriptionRes = Res.string.upg_precise_harvest_desc,
            baseCost = ItemCost(money = 1000, vegetableCosts = mapOf("tomato" to 100)),
            maxLevel = 5
        ),
        GlobalUpgrade(
            id = "lucky_harvest",
            nameRes = Res.string.upg_lucky_harvest_name,
            descriptionRes = Res.string.upg_lucky_harvest_desc,
            baseCost = ItemCost(money = 5000, vegetableCosts = mapOf("apple" to 50, "garlic" to 50)),
            maxLevel = 5
        )
    )
    private var globalClickCounter = 0

    // --- LIBRARY ---
    override val libraryCategories = LibraryRepository.categories

    // --- ACHIEVEMENTS ---
    val achievements = AchievementRepository.achievements
    var unlockedAchievements = mutableStateListOf<String>()
        private set
    
    // Toast state for achievement unlock
    var achievementToast by mutableStateOf<Achievement?>(null)
        private set

    // --- PROFILE ---
    var username by mutableStateOf("Farmer")
        private set
    var profileImageIndex by mutableStateOf(0)
        private set
    val availableAvatars = listOf("👨‍🌾", "👩‍🌾", "🌻", "🌿", "🍎", "🥕", "🏡", "🌦️")

    // --- STATE ---
    var currentItem by mutableStateOf<GameItem>(staticItemsList.first())
        private set

    override var items by mutableStateOf(staticItemsList)
        private set

    val itemsList: List<GameItem> get() = items

    // --- DATASTORE KEYS ---
    private val totalClicksKey = intPreferencesKey("total_clicks")
    private val moneyKey = intPreferencesKey("money")
    private val totalMoneyEarnedKey = intPreferencesKey("total_money_earned")
    private val vibrationEnabledKey = booleanPreferencesKey("vibration_enabled")
    private val vibrationIntensityKey = floatPreferencesKey("vibration_intensity")
    private val darkThemeKey = booleanPreferencesKey("dark_theme")
    private val shaderBackgroundEnabledKey = booleanPreferencesKey("shader_background_enabled")
    private val languageKey = stringPreferencesKey("language")
    private val languageSetKey = booleanPreferencesKey("language_set")
    private val usernameKey = stringPreferencesKey("username")
    private val profileImageKey = intPreferencesKey("profile_image_index")
    private val achievementsKey = stringSetPreferencesKey("unlocked_achievements")

    init {
        loadData()
        startCpsTracker()
    }

    private fun startCpsTracker() {
        viewModelScope.launch {
            while (isActive) {
                val now = currentTimeMillis()
                val oneSecondAgo = now - 1000
                while (clickTimestamps.isNotEmpty() && clickTimestamps.first() < oneSecondAgo) {
                    clickTimestamps.removeAt(0)
                }
                currentCps = clickTimestamps.size.toFloat()
                delay(100)
            }
        }
    }

    private fun currentTimeMillis(): Long {
        // Usamos kotlinx.datetime.Clock que es lo habitual en KMP para milisegundos de época
        return kotlin.time.Clock.System.now().toEpochMilliseconds()
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
                shaderBackgroundEnabled = prefs[shaderBackgroundEnabledKey] ?: false
                language = prefs[languageKey] ?: "auto"
                languageSet = prefs[languageSetKey] ?: false
                username = prefs[usernameKey] ?: "Farmer"
                profileImageIndex = prefs[profileImageKey] ?: 0
                
                prefs[achievementsKey]?.let {
                    unlockedAchievements.clear()
                    unlockedAchievements.addAll(it)
                }

                globalUpgrades.forEach { upgrade ->
                    upgrade.unlockedLevel = prefs[intPreferencesKey("global_upgrade_level_${upgrade.id}")] ?: 0
                }

                libraryCategories.forEach { category ->
                    category.entries.forEach { entry ->
                        entry.isUnlocked = prefs[booleanPreferencesKey("library_unlocked_${entry.id}")] ?: false
                    }
                }

                val newFruitCounts = mutableMapOf<String, Int>()
                val newTotalHarvested = mutableMapOf<String, Int>()
                
                val newList = staticItemsList.map { item ->
                    val countKey = intPreferencesKey("fruit_count_${item.id}")
                    val totalHarvestedKey = intPreferencesKey("total_harvested_${item.id}")
                    val unlockedKey = booleanPreferencesKey("unlocked_${item.id}")
                    
                    newFruitCounts[item.id] = prefs[countKey] ?: 0
                    newTotalHarvested[item.id] = prefs[totalHarvestedKey] ?: 0
                    
                    item.unlocked = if (item.id == "tomato") true else prefs[unlockedKey] ?: false
                    
                    item.modifiers.forEach { mod ->
                        mod.isUnlocked = prefs[booleanPreferencesKey("mod_unlocked_${mod.id}")] ?: false
                        mod.isEnabled = prefs[booleanPreferencesKey("mod_enabled_${mod.id}")] ?: false
                    }
                    
                    item
                }
                
                fruitCounts = newFruitCounts
                totalFruitHarvested = newTotalHarvested
                items = newList
                currentItem = items.find { it.id == currentItem.id } ?: items.first()

                checkAchievements(isInitialLoad = true)
                isDataLoaded = true
            }
        }
    }

    fun onVegetableClick(rewards: List<Reward>): List<Reward> {
        totalClicks++
        globalClickCounter++
        clickTimestamps.add(currentTimeMillis())
        
        if (vibrationEnabled) {
            vibrate(vibrationIntensity.toLong())
        }

        var finalRewards = rewards
        
        val luckyLevel = globalUpgrades.find { it.id == "lucky_harvest" }?.unlockedLevel ?: 0
        if (luckyLevel > 0) {
            val chance = luckyLevel / 100f 
            if (Random.nextFloat() < chance) {
                finalRewards = finalRewards.map { it.copy(moneyValue = it.moneyValue * 10, countValue = it.countValue * 10, isLucky = true) }
            }
        }

        val preciseLevel = globalUpgrades.find { it.id == "double_click_10" }?.unlockedLevel ?: 0
        if (preciseLevel > 0) {
            val isDouble = when (preciseLevel) {
                1 -> globalClickCounter % 10 == 0
                2 -> globalClickCounter % 5 == 0
                3 -> (globalClickCounter % 10) % 3 == 0 
                4 -> (globalClickCounter % 10) % 2 == 0 && (globalClickCounter % 10 != 0)
                5 -> globalClickCounter % 2 == 0 
                else -> false
            }
            
            if (isDouble) {
                finalRewards = finalRewards.map { it.copy(moneyValue = it.moneyValue * 2, countValue = it.countValue * 2) }
            }
        }

        val newFruitCounts = fruitCounts.toMutableMap()
        val newTotalHarvested = totalFruitHarvested.toMutableMap()
        var moneyGain = 0

        finalRewards.forEach { reward ->
            moneyGain += reward.moneyValue
            
            if (reward.countValue > 0) {
                val currentId = currentId()
                newFruitCounts[currentId] = (newFruitCounts[currentId] ?: 0) + reward.countValue
                newTotalHarvested[currentId] = (newTotalHarvested[currentId] ?: 0) + reward.countValue
            }
        }

        money += moneyGain
        totalMoneyEarned += moneyGain
        fruitCounts = newFruitCounts
        totalFruitHarvested = newTotalHarvested

        saveData()
        return finalRewards
    }

    private fun currentId(): String {
        return currentItem.id
    }

    private fun checkAchievements(isInitialLoad: Boolean = false) {
        achievements.forEach { achievement ->
            if (!unlockedAchievements.contains(achievement.id) && achievement.checkEarned(this)) {
                unlockedAchievements.add(achievement.id)
                
                // Show notification toast if not initial load
                if (!isInitialLoad) {
                    showAchievementToast(achievement)
                }
            }
        }
    }

    private fun showAchievementToast(achievement: Achievement) {
        viewModelScope.launch {
            achievementToast = achievement
            delay(4000) // Show for 4 seconds
            if (achievementToast?.id == achievement.id) {
                achievementToast = null
            }
        }
    }

    fun tryUnlockGlobalUpgrade(upgrade: GlobalUpgrade) {
        val nextCost = upgrade.getNextLevelCost()
        if (!upgrade.isMaxLevel && canAfford(nextCost)) {
            money -= nextCost.money
            val newCounts = fruitCounts.toMutableMap()
            nextCost.vegetableCosts.forEach { (vegId, amount) ->
                newCounts[vegId] = (newCounts[vegId] ?: 0) - amount
            }
            fruitCounts = newCounts
            upgrade.unlockedLevel++
            checkAchievements()
            saveData()
        }
    }

    fun tryUnlockLibraryEntry(entry: LibraryEntry) {
        if (!entry.isUnlocked && canAfford(entry.cost)) {
            money -= entry.cost.money
            val newCounts = fruitCounts.toMutableMap()
            entry.cost.vegetableCosts.forEach { (vegId, amount) ->
                newCounts[vegId] = (newCounts[vegId] ?: 0) - amount
            }
            fruitCounts = newCounts
            entry.isUnlocked = true
            checkAchievements()
            saveData()
        }
    }

    fun updateUsername(newUsername: String) {
        username = newUsername
        saveData()
    }

    fun updateProfileImage(index: Int) {
        profileImageIndex = index
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
    
    fun setShaderBackground(enabled: Boolean) {
        shaderBackgroundEnabled = enabled
        saveData()
    }

    fun updateLanguage(lang: String) {
        language = lang
        languageSet = true
        saveData()
    }

    fun resetLanguage() {
        languageSet = false
        saveData()
    }

    fun selectItem(item: GameItem) {
        if (item.unlocked) {
            currentItem = item
        }
    }

    fun canAfford(cost: ItemCost): Boolean {
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
            checkAchievements()
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
            checkAchievements()
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
        
        items.forEach { item ->
            newCounts[item.id] = (newCounts[item.id] ?: 0) + 100000
            newTotals[item.id] = (newTotals[item.id] ?: 0) + 100000
        }
        
        fruitCounts = newCounts
        totalFruitHarvested = newTotals
        checkAchievements()
        saveData()
    }

    fun resetGame() {
        totalClicks = 0
        globalClickCounter = 0
        money = 0
        totalMoneyEarned = 0
        fruitCounts = emptyMap()
        totalFruitHarvested = emptyMap()
        unlockedAchievements.clear()
        username = "Farmer"
        profileImageIndex = 0
        items = staticItemsList.mapIndexed { index, item ->
            item.apply { 
                unlocked = (index == 0)
                modifiers.forEach { 
                    it.isUnlocked = false
                    it.isEnabled = false
                }
            }
        }
        globalUpgrades.forEach { it.unlockedLevel = 0 }
        libraryCategories.forEach { cat -> cat.entries.forEach { it.isUnlocked = false } }
        currentItem = items.first()
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
                prefs[shaderBackgroundEnabledKey] = shaderBackgroundEnabled
                prefs[languageKey] = language
                prefs[languageSetKey] = languageSet
                prefs[usernameKey] = username
                prefs[profileImageKey] = profileImageIndex
                prefs[achievementsKey] = unlockedAchievements.toSet()
                
                globalUpgrades.forEach { upgrade ->
                    prefs[intPreferencesKey("global_upgrade_level_${upgrade.id}")] = upgrade.unlockedLevel
                }

                libraryCategories.forEach { category ->
                    category.entries.forEach { entry ->
                        prefs[booleanPreferencesKey("library_unlocked_${entry.id}")] = entry.isUnlocked
                    }
                }

                items.forEach { item ->
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
