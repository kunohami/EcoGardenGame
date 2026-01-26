package com.rafarg.ecogardengame.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafarg.ecogardengame.auth.AuthRepository
import com.rafarg.ecogardengame.auth.UserProfile
import com.rafarg.ecogardengame.data.GameRepository
import com.rafarg.ecogardengame.data.GameSaveData
import com.rafarg.ecogardengame.model.*
import com.rafarg.ecogardengame.ui.items as staticItemsList
import com.rafarg.ecogardengame.util.vibrate
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.Clock

data class PublicProfile(
    val id: String,
    val username: String,
    val profileImageIndex: Int,
    val achievements: List<String>
)

@OptIn(ExperimentalTime::class)
class GameViewModel(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository? = null
) : ViewModel(), GameItemProvider {

    // --- LOADING STATE ---
    var isDataLoaded by mutableStateOf(false)
        private set

    // --- AUTH STATE ---
    var currentUser by mutableStateOf<UserProfile?>(null)
        private set
    
    var authError by mutableStateOf<String?>(null)
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
    
    var isAutumnTheme by mutableStateOf(false)
        private set
    
    // --- BACKGROUND SETTINGS ---
    var shaderBackgroundEnabled by mutableStateOf(false)
        private set
    
    var isEmeraldWavyTheme by mutableStateOf(false)
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
            baseCost = GamePrices.UPGRADE_PRECISE_HARVEST,
            maxLevel = 5
        ),
        GlobalUpgrade(
            id = "lucky_harvest",
            nameRes = Res.string.upg_lucky_harvest_name,
            descriptionRes = Res.string.upg_lucky_harvest_desc,
            baseCost = GamePrices.UPGRADE_LUCKY_HARVEST,
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
    
    var lastProfileUpdateTime by mutableStateOf(0L)
        private set
    
    var searchResults = mutableStateListOf<PublicProfile>()
        private set
    
    var isSearching by mutableStateOf(false)
        private set

    // --- CLOUD SYNC ---
    var lastCloudSyncTime by mutableStateOf(0L)
        private set
    var isCloudLoading by mutableStateOf(false)
        private set

    // --- STATE ---
    var currentItem by mutableStateOf<GameItem>(staticItemsList.first())
        private set

    override var items by mutableStateOf(staticItemsList)
        private set

    val itemsList: List<GameItem> get() = items

    init {
        loadData()
        startCpsTracker()
        observeAuth()
    }

    private fun observeAuth() {
        viewModelScope.launch {
            authRepository?.currentUser?.collect { user ->
                currentUser = user
                if (user != null && username == "Farmer") {
                    username = user.name ?: "Farmer"
                }
            }
        }
    }

    fun updateAuthError(error: String?) {
        authError = error
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            authRepository?.signInWithGoogle()
        }
    }

    fun onUserLoggedOut() {
        viewModelScope.launch {
            authRepository?.signOut()
        }
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
        return Clock.System.now().toEpochMilliseconds()
    }

    private fun loadData() {
        viewModelScope.launch {
            val saveData = gameRepository.loadGameData()
            applySaveData(saveData)
            isDataLoaded = true
        }
    }

    private fun applySaveData(saveData: GameSaveData) {
        totalClicks = saveData.totalClicks
        money = saveData.money
        totalMoneyEarned = saveData.totalMoneyEarned
        vibrationEnabled = saveData.vibrationEnabled
        vibrationIntensity = saveData.vibrationIntensity
        isDarkTheme = saveData.isDarkTheme
        isAutumnTheme = saveData.isAutumnTheme
        shaderBackgroundEnabled = saveData.shaderBackgroundEnabled
        isEmeraldWavyTheme = saveData.isEmeraldWavyTheme
        language = saveData.language
        languageSet = saveData.languageSet
        username = saveData.username
        profileImageIndex = saveData.profileImageIndex
        lastProfileUpdateTime = saveData.lastProfileUpdateTime
        
        unlockedAchievements.clear()
        unlockedAchievements.addAll(saveData.unlockedAchievements)

        globalUpgrades.forEach { upgrade ->
            upgrade.unlockedLevel = saveData.globalUpgradeLevels[upgrade.id] ?: 0
        }

        libraryCategories.forEach { category ->
            category.entries.forEach { entry ->
                entry.isUnlocked = saveData.libraryUnlockedEntries[entry.id] ?: false
            }
        }

        fruitCounts = saveData.fruitCounts
        totalFruitHarvested = saveData.totalFruitHarvested
        
        items = staticItemsList.map { item ->
            item.unlocked = if (item.id == "tomato") true else saveData.unlockedItems[item.id] ?: false
            item.modifiers.forEach { mod ->
                mod.isUnlocked = saveData.modifierUnlocked[mod.id] ?: false
                mod.isEnabled = saveData.modifierEnabled[mod.id] ?: false
            }
            item
        }
        
        currentItem = items.find { it.id == currentItem.id } ?: items.first()
        checkAchievements(isInitialLoad = true)
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
            val isDouble = globalClickCounter % (11 - preciseLevel * 2) == 0
            
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
                
                if (!isInitialLoad) {
                    showAchievementToast(achievement)
                }
            }
        }
    }

    private fun showAchievementToast(achievement: Achievement) {
        viewModelScope.launch {
            achievementToast = achievement
            delay(4000) 
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
    
    fun updateAutumnTheme(autumn: Boolean) {
        isAutumnTheme = autumn
        saveData()
    }
    
    fun setShaderBackground(enabled: Boolean) {
        shaderBackgroundEnabled = enabled
        saveData()
    }
    
    fun updateEmeraldWavyTheme(enabled: Boolean) {
        isEmeraldWavyTheme = enabled
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

    fun updatePublicProfile(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = currentUser ?: return
        val now = currentTimeMillis()
        val cooldownMs = 60 * 1000L // 1 minute cooldown
        
        if (now - lastProfileUpdateTime < cooldownMs) {
            val secondsLeft = ((cooldownMs - (now - lastProfileUpdateTime)) / 1000).toInt()
            onError(secondsLeft.toString())
            return
        }

        viewModelScope.launch {
            try {
                val firestore = Firebase.firestore
                firestore.collection("users").document(user.id).set(
                    mapOf(
                        "username" to username,
                        "profileImageIndex" to profileImageIndex,
                        "achievements" to unlockedAchievements.toList()
                    ),
                    merge = true
                )
                lastProfileUpdateTime = now
                saveData()
                onSuccess()
            } catch (e: Exception) {
                onError("error")
            }
        }
    }

    fun uploadSaveToCloud(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = currentUser ?: return
        val now = currentTimeMillis()
        val cooldownMs = 60 * 1000L // 1 minute cooldown
        
        if (now - lastCloudSyncTime < cooldownMs) {
            val secondsLeft = ((cooldownMs - (now - lastCloudSyncTime)) / 1000).toInt()
            onError(secondsLeft.toString())
            return
        }

        isCloudLoading = true
        viewModelScope.launch {
            try {
                val currentData = getSaveDataSnapshot()
                val jsonString = Json.encodeToString(currentData)
                
                val firestore = Firebase.firestore
                firestore.collection("users").document(user.id).set(
                    mapOf("save_json" to jsonString),
                    merge = true
                )
                lastCloudSyncTime = now
                onSuccess()
            } catch (e: Exception) {
                onError("error")
            } finally {
                isCloudLoading = false
            }
        }
    }

    fun downloadSaveFromCloud(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = currentUser ?: return
        isCloudLoading = true
        
        viewModelScope.launch {
            try {
                val firestore = Firebase.firestore
                val doc = firestore.collection("users").document(user.id).get()
                val jsonString = doc.get<String?>("save_json")
                
                if (jsonString != null) {
                    val cloudData = Json.decodeFromString<GameSaveData>(jsonString)
                    applySaveData(cloudData)
                    saveData() // Save to local DataStore immediately
                    onSuccess()
                } else {
                    onError("no_save")
                }
            } catch (e: Exception) {
                onError("error")
            } finally {
                isCloudLoading = false
            }
        }
    }

    private fun getSaveDataSnapshot(): GameSaveData {
        val modUnlocked = mutableMapOf<String, Boolean>()
        val modEnabled = mutableMapOf<String, Boolean>()
        val unlockedItemsMap = mutableMapOf<String, Boolean>()
        
        items.forEach { item ->
            unlockedItemsMap[item.id] = item.unlocked
            item.modifiers.forEach { mod ->
                modUnlocked[mod.id] = mod.isUnlocked
                modEnabled[mod.id] = mod.isEnabled
            }
        }

        return GameSaveData(
            totalClicks = totalClicks,
            money = money,
            totalMoneyEarned = totalMoneyEarned,
            vibrationEnabled = vibrationEnabled,
            vibrationIntensity = vibrationIntensity,
            isDarkTheme = isDarkTheme,
            isAutumnTheme = isAutumnTheme,
            shaderBackgroundEnabled = shaderBackgroundEnabled,
            isEmeraldWavyTheme = isEmeraldWavyTheme,
            language = language,
            languageSet = languageSet,
            username = username,
            profileImageIndex = profileImageIndex,
            unlockedAchievements = unlockedAchievements.toSet(),
            lastProfileUpdateTime = lastProfileUpdateTime,
            fruitCounts = fruitCounts,
            totalFruitHarvested = totalFruitHarvested,
            unlockedItems = unlockedItemsMap,
            globalUpgradeLevels = globalUpgrades.associate { it.id to it.unlockedLevel },
            libraryUnlockedEntries = libraryCategories.flatMap { it.entries }.associate { it.id to it.isUnlocked },
            modifierUnlocked = modUnlocked,
            modifierEnabled = modEnabled
        )
    }

    fun searchPlayers(query: String) {
        val cleanedQuery = query.trim()
        if (cleanedQuery.isBlank()) return
        
        isSearching = true
        searchResults.clear()
        
        viewModelScope.launch {
            try {
                val firestore = Firebase.firestore
                val queries = listOf(cleanedQuery, cleanedQuery.lowercase()).distinct()
                
                queries.forEach { term ->
                    val result = firestore.collection("users")
                        .where { "username" greaterThanOrEqualTo term }
                        .where { "username" lessThanOrEqualTo term + "\uf8ff" }
                        .get()
                    
                    result.documents.forEach { doc ->
                        val data = doc.data<Map<String, Any?>>()
                        val profileId = doc.id
                        
                        if (searchResults.none { it.id == profileId }) {
                            @Suppress("UNCHECKED_CAST")
                            searchResults.add(PublicProfile(
                                id = profileId,
                                username = data["username"] as? String ?: "Unknown",
                                profileImageIndex = (data["profileImageIndex"] as? Number)?.toInt() ?: 0,
                                achievements = (data["achievements"] as? List<String>) ?: emptyList()
                            ))
                        }
                    }
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                isSearching = false
            }
        }
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
            val gameSaveData = getSaveDataSnapshot()
            gameRepository.saveGameData(gameSaveData)
        }
    }
}
