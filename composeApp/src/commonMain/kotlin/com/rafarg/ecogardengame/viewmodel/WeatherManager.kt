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

@OptIn(ExperimentalTime::class)
class WeatherManager(
    private val scope: CoroutineScope,
    private val onAutoClick: () -> Unit
) {
    private val weatherService = WeatherService()
    
    var currentWeatherData by mutableStateOf<WeatherResponse?>(null)
        private set
    
    var lastWeatherUpdateTime by mutableStateOf(0L)
        private set
    
    val weatherBonusDuration = 5 * 60 * 60 * 1000L // 5 hours

    private var autoClickJob: Job? = null

    fun isWeatherBonusActive(hasWeatherUpgrade: Boolean): Boolean {
        val now = Clock.System.now().toEpochMilliseconds()
        val isExpired = now - lastWeatherUpdateTime > weatherBonusDuration
        return hasWeatherUpgrade && currentWeatherData != null && !isExpired
    }

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

    fun startAutoClickerIfNeeded(hasWeatherUpgrade: Boolean) {
        autoClickJob?.cancel()
        if (isWeatherBonusActive(hasWeatherUpgrade) && isRaining()) {
            autoClickJob = scope.launch {
                while (isActive) {
                    delay(10000)
                    onAutoClick()
                }
            }
        }
    }

    fun applySaveData(weatherJson: String?, lastUpdate: Long, hasUpgrade: Boolean) {
        lastWeatherUpdateTime = lastUpdate
        weatherJson?.let {
            try {
                currentWeatherData = Json.decodeFromString<WeatherResponse>(it)
                startAutoClickerIfNeeded(hasUpgrade)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun isSunny(): Boolean = currentWeatherData?.current_weather?.weathercode == 0
    fun isRaining(): Boolean = currentWeatherData?.current_weather?.weathercode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82)
    fun isCloudy(): Boolean = currentWeatherData?.current_weather?.weathercode in listOf(1, 2, 3)
    fun isThundering(): Boolean = currentWeatherData?.current_weather?.weathercode in listOf(95, 96, 99)
    fun isSnowing(): Boolean = currentWeatherData?.current_weather?.weathercode in listOf(71, 73, 75, 77, 85, 86)
}

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
