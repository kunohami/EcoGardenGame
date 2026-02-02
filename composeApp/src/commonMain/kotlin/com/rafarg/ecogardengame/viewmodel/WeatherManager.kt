package com.rafarg.ecogardengame.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rafarg.ecogardengame.data.WeatherResponse
import com.rafarg.ecogardengame.data.WeatherService
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Manages weather-related logic, including fetching current weather data,
 * handling weather-based bonuses, and the automatic irrigation (auto-clicker) feature.
 */
@OptIn(ExperimentalTime::class)
class WeatherManager(
    private val scope: CoroutineScope,
    private val onAutoClick: () -> Unit
) {
    private val weatherService = WeatherService()
    
    /** Current weather data fetched from the service. */
    var currentWeatherData by mutableStateOf<WeatherResponse?>(null)
        private set
    
    /** Timestamp of the last successful weather update. */
    var lastWeatherUpdateTime by mutableStateOf(0L)
        private set
    
    /** Duration for which a weather bonus remains active (5 hours). */
    val weatherBonusDuration = 5 * 60 * 60 * 1000L // 5 hours

    private var autoClickJob: Job? = null

    /**
     * Determines if the weather bonus is currently active.
     * Requires the global weather upgrade to be unlocked and the last update to be within the duration.
     */
    fun isWeatherBonusActive(hasWeatherUpgrade: Boolean): Boolean {
        val now = Clock.System.now().toEpochMilliseconds()
        val isExpired = now - lastWeatherUpdateTime > weatherBonusDuration
        return hasWeatherUpgrade && currentWeatherData != null && !isExpired
    }

    /**
     * Fetches new weather data for the given coordinates and updates the state.
     */
    fun updateWeather(lat: Double, lon: Double, onSave: () -> Unit) {
        scope.launch {
            val result = weatherService.fetchWeather(lat, lon)
            if (result != null) {
                currentWeatherData = result
                lastWeatherUpdateTime = Clock.System.now().toEpochMilliseconds()
                onSave()
                startAutoClickerIfNeeded(hasWeatherUpgrade = true)
            }
        }
    }

    /**
     * Starts a background coroutine to perform automatic harvests if it's raining
     * and the weather bonus is active.
     */
    fun startAutoClickerIfNeeded(hasWeatherUpgrade: Boolean) {
        autoClickJob?.cancel()
        if (isWeatherBonusActive(hasWeatherUpgrade) && isRaining()) {
            autoClickJob = scope.launch {
                while (isActive) {
                    delay(10000) // Auto-harvest every 10 seconds
                    onAutoClick()
                }
            }
        }
    }

    /** Applies weather-related data from a saved game state. */
    fun applySaveData(weatherJson: String?, lastUpdate: Long, hasUpgrade: Boolean) {
        lastWeatherUpdateTime = lastUpdate
        weatherJson?.let {
            try {
                currentWeatherData = Json.decodeFromString<WeatherResponse>(it)
                startAutoClickerIfNeeded(hasUpgrade)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // Helper methods to check for specific weather conditions based on WMO codes
    fun isSunny(): Boolean = currentWeatherData?.current_weather?.weathercode == 0
    fun isRaining(): Boolean = currentWeatherData?.current_weather?.weathercode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82)
    fun isCloudy(): Boolean = currentWeatherData?.current_weather?.weathercode in listOf(1, 2, 3)
    fun isThundering(): Boolean = currentWeatherData?.current_weather?.weathercode in listOf(95, 96, 99)
    fun isSnowing(): Boolean = currentWeatherData?.current_weather?.weathercode in listOf(71, 73, 75, 77, 85, 86)
}

/**
 * Returns a human-readable description for a given WMO weather code.
 */
fun getWeatherDescription(code: Int): String {
    return when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Mainly clear, partly cloudy, and overcast"
        45, 48 -> "Fog and depositing rime fog"
        51, 53, 55 -> "Drizzle: Light, moderate, and dense intensity"
        61, 63, 65 -> "Rain: Slight, moderate and heavy intensity"
        71, 73, 75 -> "Snow fall: Slight, moderate, and heavy intensity"
        77 -> "Snow grains"
        80, 81, 82 -> "Rain showers: Slight, moderate, and violent"
        85, 86 -> "Snow showers slight and heavy"
        95 -> "Thunderstorm: Slight or moderate"
        96, 99 -> "Thunderstorm with slight and heavy hail"
        else -> "Unknown"
    }
}
