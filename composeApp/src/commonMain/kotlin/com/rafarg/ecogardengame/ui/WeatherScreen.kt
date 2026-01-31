package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.data.WeatherResponse
import com.rafarg.ecogardengame.data.WeatherService
import com.rafarg.ecogardengame.util.rememberLocationProvider
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun WeatherScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val locationProvider = rememberLocationProvider()
    val weatherService = remember { WeatherService() }
    val scope = rememberCoroutineScope()
    
    var weatherData by remember { mutableStateOf<WeatherResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = primaryText)
            }
            Text(
                text = "Garden Weather",
                style = MaterialTheme.typography.headlineMedium,
                color = primaryText
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            weatherData?.let { data ->
                Text(
                    text = "${data.current_weather.temperature}°C",
                    fontSize = 64.sp,
                    color = primaryText
                )
                Text(
                    text = getWeatherDescription(data.current_weather.weathercode),
                    style = MaterialTheme.typography.titleLarge,
                    color = primaryText
                )
            }

            if (weatherData == null && !isLoading) {
                Text("Tap the button to check your garden's climate", color = Color.Gray)
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                isLoading = true
                error = null
                locationProvider.requestLocation { coords ->
                    if (coords != null) {
                        scope.launch {
                            val result = weatherService.fetchWeather(coords.latitude, coords.longitude)
                            if (result != null) {
                                weatherData = result
                            } else {
                                error = "Failed to fetch weather data"
                            }
                            isLoading = false
                        }
                    } else {
                        error = "Location permission denied or unavailable"
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Update Weather")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
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
