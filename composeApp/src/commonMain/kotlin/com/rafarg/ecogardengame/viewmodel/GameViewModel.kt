package com.rafarg.ecogardengame.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafarg.ecogardengame.auth.AuthRepository
import com.rafarg.ecogardengame.auth.UserProfile
import com.rafarg.ecogardengame.data.ArtRepository
import com.rafarg.ecogardengame.data.GameRepository
import com.rafarg.ecogardengame.data.GameSaveData
import com.rafarg.ecogardengame.data.WeatherResponse
import com.rafarg.ecogardengame.model.*
import com.rafarg.ecogardengame.ui.items as staticItemsList
import com.rafarg.ecogardengame.util.vibrate
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Data class representing a player's public profile for the leaderboard/search.
 */
data class PublicProfile(
    val id: String,
    val username: String,
    val profileImageId: String,
    val achievements: List<String>
)

/**
 * The main ViewModel for the game, orchestrating state between various managers 
 * (Economy, Profile, Weather) and handling data persistence.
 */
@OptIn(ExperimentalTime::class)
class GameViewModel(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository? = null
) : ViewModel(), GameItemProvider {

    // --- MANAGERS ---
    private val weatherManager = WeatherManager(viewModelScope) {
        onVegetableClick(currentItem.baseRewards)
    }
    
    private val profileManager = ProfileManager(viewModelScope, authRepository)
    
    private val economyManager = EconomyManager()

    // --- LOADING STATE ---
    /** Indicates if the initial game data has been loaded from the repository. */
    var isDataLoaded by mutableStateOf(false)
        private set

    // --- AUTH & PROFILE DELEGATION ---
    val currentUser: UserProfile? get() = profileManager.currentUser
    val username: String get() = profileManager.username
    val profileImageId: String get() = profileManager.profileImageId
    val searchResults get() = profileManager.searchResults
    val isSearching: Boolean get() = profileManager.isSearching
    
    fun updateUsername(newName: String) { profileManager.updateUsername(newName); saveData() }
    fun updateProfileImage(id: String) { profileManager.updateProfileImage(id); saveData() }
    fun searchPlayers(query: String) = profileManager.searchPlayers(query)
    fun updatePublicProfile(onSuccess: () -> Unit, onError: (String) -> Unit) = 
        profileManager.updatePublicProfile(unlockedAchievements.toList(), onSuccess, onError)

    fun signInWithGoogle() = viewModelScope.launch { authRepository?.signInWithGoogle() }
    fun onUserLoggedOut() = viewModelScope.launch { authRepository?.signOut() }

    // --- ECONOMY DELEGATION ---
    override val totalClicks: Int get() = economyManager.totalClicks
    val money: Int get() = economyManager.money
    val fruitCounts: Map<String, Int> get() = economyManager.fruitCounts
    val totalMoneyEarned: Int get() = economyManager.totalMoneyEarned
    val totalFruitHarvested: Map<String, Int> get() = economyManager.totalFruitHarvested
    
    fun canAfford(cost: ItemCost) = economyManager.canAfford(cost)

    // --- CPS (Clicks Per Second) LOGIC ---
    private val clickTimestamps = mutableStateListOf<Long>()
    /** Current clicks per second, updated in real-time. */
    var currentCps by mutableStateOf(0.0f)
        private set

    // --- SETTINGS ---
    var vibrationEnabled by mutableStateOf(false)
        private set
    var vibrationIntensity by mutableStateOf(10f)
    var isDarkTheme by mutableStateOf(false)
        private set
    var isAutumnTheme by mutableStateOf(false)
        private set
    var shaderBackgroundEnabled by mutableStateOf(false)
        private set
    var isEmeraldWavyTheme by mutableStateOf(false)
        private set
    var language by mutableStateOf("auto")
        private set
    var languageSet by mutableStateOf(false)
        private set

    // --- GLOBAL UPGRADES ---
    override var globalUpgrades = listOf(
        GlobalUpgrade("double_click_10", Res.string.upg_precise_harvest_name, Res.string.upg_precise_harvest_desc, GamePrices.UPGRADE_PRECISE_HARVEST, 5),
        GlobalUpgrade("lucky_harvest", Res.string.upg_lucky_harvest_name, Res.string.upg_lucky_harvest_desc, GamePrices.UPGRADE_LUCKY_HARVEST, 5),
        GlobalUpgrade("weather_bonus", Res.string.upg_weather_bonus_name, Res.string.upg_weather_bonus_desc, GamePrices.UPGRADE_WEATHER_BONUS, 1)
    )
    private var globalClickCounter = 0

    // --- WEATHER DELEGATION ---
    val currentWeatherData: WeatherResponse? get() = weatherManager.currentWeatherData
    val lastWeatherUpdateTime: Long get() = weatherManager.lastWeatherUpdateTime
    val isWeatherBonusActive: Boolean get() = weatherManager.isWeatherBonusActive(hasWeatherUpgrade())
    val weatherBonusDuration: Long get() = weatherManager.weatherBonusDuration
    private fun hasWeatherUpgrade() = globalUpgrades.find { it.id == "weather_bonus" }?.unlockedLevel ?: 0 > 0
    fun updateWeather(lat: Double, lon: Double) = weatherManager.updateWeather(lat, lon) { saveData() }
    fun isThundering() = weatherManager.isThundering()

    // --- LIBRARY & ART ---
    override val libraryCategories = LibraryRepository.categories
    private var unlockedArtIds = mutableStateListOf<String>()
    override fun isArtUnlocked(artId: String): Boolean = unlockedArtIds.contains(artId)
    override fun getArtCount(): Int = ArtRepository.artEntries.size
    
    /** Unlocks a piece of art from the gallery if the user can afford it. */
    fun unlockArt(artId: String, cost: Int) {
        val finalCost = calculateDiscountedPrice(cost)
        if (money >= finalCost && !isArtUnlocked(artId)) {
            economyManager.spend(ItemCost(money = finalCost))
            unlockedArtIds.add(artId)
            checkAchievements(); saveData()
        }
    }

    // --- ACHIEVEMENTS ---
    val achievements = AchievementRepository.achievements
    var unlockedAchievements = mutableStateListOf<String>()
        private set
    var achievementToast by mutableStateOf<Achievement?>(null)
        private set

    // --- CLOUD SYNC ---
    var lastCloudSyncTime by mutableStateOf(0L)
        private set
    var isCloudLoading by mutableStateOf(false)
        private set

    // --- TUTORIAL ---
    var tutorialSeen by mutableStateOf(false)
        private set
    var showTutorial by mutableStateOf(false)

    // --- GAME STATE ---
    /** The item (vegetable) currently being displayed and interacted with in the garden. */
    var currentItem by mutableStateOf<GameItem>(staticItemsList.first())
        private set
    override var items by mutableStateOf(staticItemsList)
        private set
    val itemsList: List<GameItem> get() = items

    init {
        loadData()
        startCpsTracker()
    }

    /**
     * Coroutine that updates the currentCps value by tracking click timestamps over a 1-second window.
     */
    private fun startCpsTracker() {
        viewModelScope.launch {
            while (isActive) {
                val now = currentTimeMillis()
                val oneSecondAgo = now - 1000
                while (clickTimestamps.isNotEmpty() && clickTimestamps.first() < oneSecondAgo) clickTimestamps.removeAt(0)
                currentCps = clickTimestamps.size.toFloat()
                delay(100)
            }
        }
    }

    private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

    /** Loads game progress from local storage. */
    private fun loadData() {
        viewModelScope.launch {
            val saveData = gameRepository.loadGameData()
            applySaveData(saveData)
            isDataLoaded = true
            if (!tutorialSeen) showTutorial = true
        }
    }

    /** Updates the ViewModel's state based on a GameSaveData object. */
    private fun applySaveData(saveData: GameSaveData) {
        vibrationEnabled = saveData.vibrationEnabled; vibrationIntensity = saveData.vibrationIntensity
        isDarkTheme = saveData.isDarkTheme; isAutumnTheme = saveData.isAutumnTheme
        shaderBackgroundEnabled = saveData.shaderBackgroundEnabled; isEmeraldWavyTheme = saveData.isEmeraldWavyTheme
        language = saveData.language; languageSet = saveData.languageSet; tutorialSeen = saveData.tutorialSeen
        
        economyManager.applySaveData(saveData.totalClicks, saveData.money, saveData.totalMoneyEarned, saveData.fruitCounts, saveData.totalFruitHarvested)
        profileManager.username = saveData.username
        profileManager.profileImageId = saveData.profileImageId ?: "tomato"
        profileManager.lastProfileUpdateTime = saveData.lastProfileUpdateTime
        
        unlockedAchievements.clear(); unlockedAchievements.addAll(saveData.unlockedAchievements)
        unlockedArtIds.clear(); unlockedArtIds.addAll(saveData.unlockedArtIds)
        globalUpgrades.forEach { it.unlockedLevel = saveData.globalUpgradeLevels[it.id] ?: 0 }
        libraryCategories.forEach { cat -> cat.entries.forEach { it.isUnlocked = saveData.libraryUnlockedEntries[it.id] ?: false } }
        
        items = staticItemsList.map { item ->
            item.unlocked = if (item.id == "tomato") true else saveData.unlockedItems[item.id] ?: false
            item.modifiers.forEach { mod ->
                mod.isUnlocked = saveData.modifierUnlocked[mod.id] ?: false
                mod.isEnabled = saveData.modifierEnabled[mod.id] ?: false
            }
            item
        }
        currentItem = items.find { it.id == currentItem.id } ?: items.first()
        weatherManager.applySaveData(saveData.weatherDataJson, saveData.lastWeatherUpdateTime, hasWeatherUpgrade())
        checkAchievements(isInitialLoad = true)
    }

    /**
     * Handles a click on a vegetable, calculating rewards based on active upgrades and weather.
     * @param rewards The base rewards for the clicked item.
     * @return The final rewards granted after all modifiers are applied.
     */
    fun onVegetableClick(rewards: List<Reward>): List<Reward> {
        economyManager.addClicks()
        globalClickCounter++
        clickTimestamps.add(currentTimeMillis())
        if (vibrationEnabled) vibrate(vibrationIntensity.toLong())

        val finalRewards = RewardCalculator.calculateRewards(
            rewards, globalUpgrades, globalClickCounter, isWeatherBonusActive,
            currentWeatherData?.current_weather?.temperature ?: 20.0,
            weatherManager.isSunny(), weatherManager.isSnowing(), currentItem.id, weatherManager.isCloudy()
        )

        economyManager.addRewards(finalRewards, currentItem.id)
        saveData(); return finalRewards
    }

    /** Checks if any new achievements have been earned. */
    private fun checkAchievements(isInitialLoad: Boolean = false) {
        achievements.forEach { achievement ->
            if (!unlockedAchievements.contains(achievement.id)) {
                val isEarned = if (achievement.id == "art_collector") {
                    ArtRepository.artEntries.isNotEmpty() && ArtRepository.artEntries.all { isArtUnlocked(it.id) }
                } else achievement.checkEarned(this)
                if (isEarned) {
                    unlockedAchievements.add(achievement.id)
                    if (!isInitialLoad) showAchievementToast(achievement)
                }
            }
        }
    }

    /** Displays a temporary UI toast for a newly unlocked achievement. */
    private fun showAchievementToast(achievement: Achievement) {
        viewModelScope.launch {
            achievementToast = achievement
            delay(4000) 
            if (achievementToast?.id == achievement.id) achievementToast = null
        }
    }

    /** Applies active discounts (e.g., from weather) to a price. */
    fun calculateDiscountedPrice(basePrice: Int): Int = 
        if (isWeatherBonusActive && weatherManager.isCloudy()) (basePrice * 0.9).toInt() else basePrice

    /** Gets the drawable resource associated with a specific avatar ID. */
    fun getAvatarResource(id: String): DrawableResource =
        itemsList.find { it.id == id }?.resource ?: ArtRepository.artEntries.find { it.id == id }?.resource ?: Res.drawable.tomato_strip

    /** Attempts to level up a global upgrade. */
    fun tryUnlockGlobalUpgrade(upgrade: GlobalUpgrade) {
        val nextCost = upgrade.getNextLevelCost()
        if (!upgrade.isMaxLevel && canAfford(nextCost)) {
            economyManager.spend(nextCost)
            upgrade.unlockedLevel++
            checkAchievements(); saveData()
        }
    }

    /** Attempts to unlock a specific entry in the library. */
    fun tryUnlockLibraryEntry(entry: LibraryEntry) {
        val finalMoneyCost = calculateDiscountedPrice(entry.cost.money)
        val adjustedCost = entry.cost.copy(money = finalMoneyCost)
        if (!entry.isUnlocked && canAfford(adjustedCost)) {
            economyManager.spend(adjustedCost)
            entry.isUnlocked = true
            checkAchievements(); saveData()
        }
    }

    // --- SETTINGS UPDATERS ---
    fun setVibration(enabled: Boolean) { vibrationEnabled = enabled; saveData() }
    fun updateVibrationIntensity(intensity: Float) { vibrationIntensity = intensity; saveData() }
    fun setTheme(dark: Boolean) { isDarkTheme = dark; saveData() }
    fun updateAutumnTheme(autumn: Boolean) { isAutumnTheme = autumn; saveData() }
    fun setShaderBackground(enabled: Boolean) { shaderBackgroundEnabled = enabled; saveData() }
    fun updateEmeraldWavyTheme(enabled: Boolean) { isEmeraldWavyTheme = enabled; saveData() }
    fun updateLanguage(lang: String) { language = lang; languageSet = true; saveData() }
    fun resetLanguage() { languageSet = false; saveData() }
    fun completeTutorial() { tutorialSeen = true; showTutorial = false; saveData() }
    fun replayTutorial() { showTutorial = true }

    /** Uploads current progress to Firebase Firestore. */
    fun uploadSaveToCloud(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = currentUser ?: return
        val now = currentTimeMillis()
        if (now - lastCloudSyncTime < 60000L) {
            onError(((60000L - (now - lastCloudSyncTime)) / 1000).toInt().toString()); return
        }
        isCloudLoading = true
        viewModelScope.launch {
            try {
                val jsonString = Json.encodeToString(getSaveDataSnapshot())
                Firebase.firestore.collection("users").document(user.id).set(mapOf("save_json" to jsonString), merge = true)
                lastCloudSyncTime = now; onSuccess()
            } catch (e: Exception) { onError("error") } finally { isCloudLoading = false }
        }
    }

    /** Downloads and applies game progress from Firebase Firestore. */
    fun downloadSaveFromCloud(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = currentUser ?: return
        isCloudLoading = true
        viewModelScope.launch {
            try {
                val doc = Firebase.firestore.collection("users").document(user.id).get()
                val jsonString = doc.get<String?>("save_json")
                if (jsonString != null) {
                    applySaveData(Json.decodeFromString<GameSaveData>(jsonString)); saveData(); onSuccess()
                } else onError("no_save")
            } catch (e: Exception) { onError("error") } finally { isCloudLoading = false }
        }
    }

    /** Creates a snapshot of the current game state for saving. */
    private fun getSaveDataSnapshot(): GameSaveData {
        val modUnlocked = mutableMapOf<String, Boolean>()
        val modEnabled = mutableMapOf<String, Boolean>()
        val unlockedItemsMap = mutableMapOf<String, Boolean>()
        items.forEach { item ->
            unlockedItemsMap[item.id] = item.unlocked
            item.modifiers.forEach { mod -> modUnlocked[mod.id] = mod.isUnlocked; modEnabled[mod.id] = mod.isEnabled }
        }
        return GameSaveData(
            totalClicks = totalClicks, money = money, totalMoneyEarned = totalMoneyEarned,
            vibrationEnabled = vibrationEnabled, vibrationIntensity = vibrationIntensity,
            isDarkTheme = isDarkTheme, isAutumnTheme = isAutumnTheme, shaderBackgroundEnabled = shaderBackgroundEnabled,
            isEmeraldWavyTheme = isEmeraldWavyTheme, language = language, languageSet = languageSet,
            username = username, profileImageId = profileImageId, unlockedAchievements = unlockedAchievements.toSet(),
            lastProfileUpdateTime = profileManager.lastProfileUpdateTime, fruitCounts = fruitCounts, totalFruitHarvested = totalFruitHarvested,
            unlockedItems = unlockedItemsMap, globalUpgradeLevels = globalUpgrades.associate { it.id to it.unlockedLevel },
            libraryUnlockedEntries = libraryCategories.flatMap { it.entries }.associate { it.id to it.isUnlocked },
            modifierUnlocked = modUnlocked, modifierEnabled = modEnabled, unlockedArtIds = unlockedArtIds.toSet(),
            tutorialSeen = tutorialSeen, lastWeatherUpdateTime = lastWeatherUpdateTime,
            weatherDataJson = currentWeatherData?.let { Json.encodeToString(it) }
        )
    }

    /** Selects a new item to be displayed in the garden. */
    fun selectItem(item: GameItem) { if (item.unlocked) currentItem = item }

    /** Attempts to unlock a new vegetable for the garden. */
    fun tryUnlockItem(item: GameItem) {
        if (!item.unlocked && canAfford(item.unlockCost)) {
            economyManager.spend(item.unlockCost)
            item.unlocked = true; currentItem = item 
            checkAchievements(); saveData()
        }
    }

    /** Attempts to unlock a specific modifier for a vegetable. */
    fun tryUnlockModifier(modifier: GameplayModifier) {
        if (!modifier.isUnlocked && canAfford(modifier.unlockCost)) {
            economyManager.spend(modifier.unlockCost)
            modifier.isUnlocked = true; modifier.isEnabled = true
            checkAchievements(); saveData()
        }
    }

    /** Toggles an already unlocked modifier on or off. */
    fun toggleModifier(modifier: GameplayModifier) { if (modifier.isUnlocked) { modifier.isEnabled = !modifier.isEnabled; saveData() } }

    /** Debug function to add a large amount of all resources. */
    fun debugAddResources() {
        economyManager.debugAddResources()
        val newCounts = fruitCounts.toMutableMap(); val newTotals = totalFruitHarvested.toMutableMap()
        items.forEach { newCounts[it.id] = (newCounts[it.id] ?: 0) + 100000; newTotals[it.id] = (newTotals[it.id] ?: 0) + 100000 }
        economyManager.applySaveData(totalClicks, money, totalMoneyEarned, newCounts, newTotals)
        checkAchievements(); saveData()
    }

    /** Resets all progress and clears local save data. */
    fun resetGame() {
        economyManager.reset()
        profileManager.username = "Farmer"; profileManager.profileImageId = "tomato"
        unlockedAchievements.clear()
        items = staticItemsList.mapIndexed { i, item -> item.apply { unlocked = (i == 0); modifiers.forEach { it.isUnlocked = false; it.isEnabled = false } } }
        globalUpgrades.forEach { it.unlockedLevel = 0 }; libraryCategories.forEach { cat -> cat.entries.forEach { it.isUnlocked = false } }
        unlockedArtIds.clear(); tutorialSeen = false; currentItem = items.first(); saveData()
    }

    /** Saves current game state to local storage. */
    private fun saveData() { viewModelScope.launch { gameRepository.saveGameData(getSaveDataSnapshot()) } }
}
