package com.rafarg.ecogardengame.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * --- DATA MODEL: WEATHER RESPONSE ---
 * These classes represent the structure of the JSON data returned by the Open-Meteo API.
 * The '@Serializable' annotation allows the 'kotlinx.serialization' library to 
 * automatically convert JSON text into these Kotlin objects.
 */
@Serializable
data class WeatherResponse(
    val current_weather: CurrentWeather
)

@Serializable
data class CurrentWeather(
    val temperature: Double, // Current temp in Celsius
    val weathercode: Int     // WMO Weather interpretation code (e.g., 0 = Clear, 61 = Rain)
)

/**
 * --- NETWORK SERVICE ---
 * Handles communication with the external Weather API.
 * Uses Ktor, which is the standard networking library for Kotlin Multiplatform.
 */
class WeatherService {
    /**
     * --- HTTP CLIENT CONFIGURATION ---
     * We initialize a client that knows how to handle JSON data.
     * 'ignoreUnknownKeys = true' is a best practice: if the API adds new data 
     * in the future, our app won't crash just because it doesn't recognize it.
     */
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    /**
     * Fetches real-time weather data for a specific location.
     * 
     * @param lat Latitude of the user.
     * @param lon Longitude of the user.
     * @return A [WeatherResponse] object if successful, or null if the network fails.
     * 
     * --- ASYNCHRONOUS PROGRAMMING (suspend) ---
     * This function is marked as 'suspend' because network calls take time. 
     * It allows the app to "wait" for the internet without freezing the user interface.
     */
    suspend fun fetchWeather(lat: Double, lon: Double): WeatherResponse? {
        return try {
            // Making a GET request to the Open-Meteo API
            client.get("https://api.open-meteo.com/v1/forecast") {
                parameter("latitude", lat)
                parameter("longitude", lon)
                parameter("current_weather", true)
            }.body()
        } catch (e: Exception) {
            // If there's no internet or the server is down, we print the error and return null.
            e.printStackTrace()
            null
        }
    }
}
