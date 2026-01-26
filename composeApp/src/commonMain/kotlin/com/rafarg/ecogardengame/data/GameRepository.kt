package com.rafarg.ecogardengame.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first

data class GameSaveData(
    val totalClicks: Int = 0,
    val money: Int = 0,
    val totalMoneyEarned: Int = 0,
    val vibrationEnabled: Boolean = false,
    val vibrationIntensity: Float = 10f,
    val isDarkTheme: Boolean = false,
    val isAutumnTheme: Boolean = false,
    val shaderBackgroundEnabled: Boolean = false,
    val isEmeraldWavyTheme: Boolean = false,
    val language: String = "auto",
    val languageSet: Boolean = false,
    val username: String = "Farmer",
    val profileImageIndex: Int = 0,
    val unlockedAchievements: Set<String> = emptySet(),
    val lastProfileUpdateTime: Long = 0L,
    val fruitCounts: Map<String, Int> = emptyMap(),
    val totalFruitHarvested: Map<String, Int> = emptyMap(),
    val unlockedItems: Map<String, Boolean> = emptyMap(),
    val globalUpgradeLevels: Map<String, Int> = emptyMap(),
    val libraryUnlockedEntries: Map<String, Boolean> = emptyMap(),
    val modifierUnlocked: Map<String, Boolean> = emptyMap(),
    val modifierEnabled: Map<String, Boolean> = emptyMap()
)

interface GameRepository {
    suspend fun loadGameData(): GameSaveData
    suspend fun saveGameData(data: GameSaveData)
}

class DataStoreGameRepository(private val dataStore: DataStore<Preferences>) : GameRepository {

    private val totalClicksKey = intPreferencesKey("total_clicks")
    private val moneyKey = intPreferencesKey("money")
    private val totalMoneyEarnedKey = intPreferencesKey("total_money_earned")
    private val vibrationEnabledKey = booleanPreferencesKey("vibration_enabled")
    private val vibrationIntensityKey = floatPreferencesKey("vibration_intensity")
    private val darkThemeKey = booleanPreferencesKey("dark_theme")
    private val autumnThemeKey = booleanPreferencesKey("autumn_theme")
    private val shaderBackgroundEnabledKey = booleanPreferencesKey("shader_background_enabled")
    private val emeraldWavyThemeKey = booleanPreferencesKey("emerald_wavy_theme")
    private val languageKey = stringPreferencesKey("language")
    private val languageSetKey = booleanPreferencesKey("language_set")
    private val usernameKey = stringPreferencesKey("username")
    private val profileImageKey = intPreferencesKey("profile_image_index")
    private val achievementsKey = stringSetPreferencesKey("unlocked_achievements")
    private val lastProfileUpdateKey = longPreferencesKey("last_profile_update")

    private val fixedKeys = setOf(
        "total_clicks", "money", "total_money_earned", "vibration_enabled",
        "vibration_intensity", "dark_theme", "autumn_theme", "shader_background_enabled",
        "emerald_wavy_theme", "language", "language_set", "username",
        "profile_image_index", "unlocked_achievements", "last_profile_update"
    )

    override suspend fun loadGameData(): GameSaveData {
        val prefs = dataStore.data.first()
        
        val fruitCounts = mutableMapOf<String, Int>()
        val totalFruitHarvested = mutableMapOf<String, Int>()
        val unlockedItems = mutableMapOf<String, Boolean>()
        val globalUpgradeLevels = mutableMapOf<String, Int>()
        val libraryUnlockedEntries = mutableMapOf<String, Boolean>()
        val modifierUnlocked = mutableMapOf<String, Boolean>()
        val modifierEnabled = mutableMapOf<String, Boolean>()

        prefs.asMap().forEach { (key, value) ->
            val name = key.name
            if (name in fixedKeys) return@forEach

            // Uso de safe cast (as?) para evitar ClassCastException
            when {
                name.startsWith("fruit_count_") -> (value as? Int)?.let { fruitCounts[name.removePrefix("fruit_count_")] = it }
                name.startsWith("total_harvested_") -> (value as? Int)?.let { totalFruitHarvested[name.removePrefix("total_harvested_")] = it }
                name.startsWith("library_unlocked_") -> (value as? Boolean)?.let { libraryUnlockedEntries[name.removePrefix("library_unlocked_")] = it }
                name.startsWith("mod_unlocked_") -> (value as? Boolean)?.let { modifierUnlocked[name.removePrefix("mod_unlocked_")] = it }
                name.startsWith("mod_enabled_") -> (value as? Boolean)?.let { modifierEnabled[name.removePrefix("mod_enabled_")] = it }
                name.startsWith("global_upgrade_level_") -> (value as? Int)?.let { globalUpgradeLevels[name.removePrefix("global_upgrade_level_")] = it }
                // unlocked_achievements debería saltarse por fixedKeys, pero por seguridad excluimos el nombre exacto aquí también
                name.startsWith("unlocked_") && name != "unlocked_achievements" -> (value as? Boolean)?.let { unlockedItems[name.removePrefix("unlocked_")] = it }
            }
        }

        return GameSaveData(
            totalClicks = prefs[totalClicksKey] ?: 0,
            money = prefs[moneyKey] ?: 0,
            totalMoneyEarned = prefs[totalMoneyEarnedKey] ?: 0,
            vibrationEnabled = prefs[vibrationEnabledKey] ?: false,
            vibrationIntensity = prefs[vibrationIntensityKey] ?: 10f,
            isDarkTheme = prefs[darkThemeKey] ?: false,
            isAutumnTheme = prefs[autumnThemeKey] ?: false,
            shaderBackgroundEnabled = prefs[shaderBackgroundEnabledKey] ?: false,
            isEmeraldWavyTheme = prefs[emeraldWavyThemeKey] ?: false,
            language = prefs[languageKey] ?: "auto",
            languageSet = prefs[languageSetKey] ?: false,
            username = prefs[usernameKey] ?: "Farmer",
            profileImageIndex = prefs[profileImageKey] ?: 0,
            unlockedAchievements = prefs[achievementsKey] ?: emptySet(),
            lastProfileUpdateTime = prefs[lastProfileUpdateKey] ?: 0L,
            fruitCounts = fruitCounts,
            totalFruitHarvested = totalFruitHarvested,
            unlockedItems = unlockedItems,
            globalUpgradeLevels = globalUpgradeLevels,
            libraryUnlockedEntries = libraryUnlockedEntries,
            modifierUnlocked = modifierUnlocked,
            modifierEnabled = modifierEnabled
        )
    }

    override suspend fun saveGameData(data: GameSaveData) {
        dataStore.edit { prefs ->
            prefs[totalClicksKey] = data.totalClicks
            prefs[moneyKey] = data.money
            prefs[totalMoneyEarnedKey] = data.totalMoneyEarned
            prefs[vibrationEnabledKey] = data.vibrationEnabled
            prefs[vibrationIntensityKey] = data.vibrationIntensity
            prefs[darkThemeKey] = data.isDarkTheme
            prefs[autumnThemeKey] = data.isAutumnTheme
            prefs[shaderBackgroundEnabledKey] = data.shaderBackgroundEnabled
            prefs[emeraldWavyThemeKey] = data.isEmeraldWavyTheme
            prefs[languageKey] = data.language
            prefs[languageSetKey] = data.languageSet
            prefs[usernameKey] = data.username
            prefs[profileImageKey] = data.profileImageIndex
            prefs[achievementsKey] = data.unlockedAchievements
            prefs[lastProfileUpdateKey] = data.lastProfileUpdateTime

            data.fruitCounts.forEach { (id, count) -> prefs[intPreferencesKey("fruit_count_$id")] = count }
            data.totalFruitHarvested.forEach { (id, count) -> prefs[intPreferencesKey("total_harvested_$id")] = count }
            data.unlockedItems.forEach { (id, unlocked) -> prefs[booleanPreferencesKey("unlocked_$id")] = unlocked }
            data.globalUpgradeLevels.forEach { (id, level) -> prefs[intPreferencesKey("global_upgrade_level_$id")] = level }
            data.libraryUnlockedEntries.forEach { (id, unlocked) -> prefs[booleanPreferencesKey("library_unlocked_$id")] = unlocked }
            data.modifierUnlocked.forEach { (id, unlocked) -> prefs[booleanPreferencesKey("mod_unlocked_$id")] = unlocked }
            data.modifierEnabled.forEach { (id, enabled) -> prefs[booleanPreferencesKey("mod_enabled_$id")] = enabled }
        }
    }
}
