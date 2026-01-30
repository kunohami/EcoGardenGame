package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.rafarg.ecogardengame.data.WeatherResponse
import com.rafarg.ecogardengame.data.WeatherService
import com.rafarg.ecogardengame.model.GameItem
import com.rafarg.ecogardengame.util.rememberLocationProvider
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun GameScreen(viewModel: GameViewModel, onNavigateToStore: () -> Unit) {
    var menuVisible by remember { mutableStateOf(false) }
    var itemToPurchase by remember { mutableStateOf<GameItem?>(null) }
    var showTutorial by remember { mutableStateOf(false) }
    var showWeatherDialog by remember { mutableStateOf(false) }

    val textColor = if (viewModel.shaderBackgroundEnabled) Color.White else Color.Unspecified
    val secondaryTextColor = if (viewModel.shaderBackgroundEnabled) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (menuVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2.5f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { menuVisible = false }
            )
        }

        // --- TOP BAR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp)
                .zIndex(2f)
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Money Counter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SpriteAnimation(
                        painter = painterResource(Res.drawable.coin_strip),
                        frameCount = 3,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${viewModel.money}",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        maxLines = 1
                    )
                }

                // Specific Fruit Counter
                val fruitCount = viewModel.fruitCounts[viewModel.currentItem.id] ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(viewModel.currentItem.particleEmoji, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$fruitCount",
                        style = MaterialTheme.typography.titleMedium,
                        color = secondaryTextColor,
                        maxLines = 1
                    )
                }
            }

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Weather / Clicky Cheeky
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .clickable { showWeatherDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    SpriteAnimation(
                        painter = painterResource(Res.drawable.clickycheekykneel_strip),
                        frameCount = 3,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Info Bunny
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .clickable { showTutorial = true },
                    contentAlignment = Alignment.Center
                ) {
                    SpriteAnimation(
                        painter = painterResource(Res.drawable.infobunny_strip),
                        frameCount = 3,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Menu Toggle
                Box(
                    modifier = Modifier
                        .size(width = 70.dp, height = 52.dp)
                        .clip(CircleShape)
                        .clickable { menuVisible = !menuVisible },
                    contentAlignment = Alignment.Center
                ) {
                    SpriteAnimation(
                        painter = painterResource(Res.drawable.fruitmenu_strip),
                        frameCount = 3,
                        modifier = Modifier.size(width = 56.dp, height = 40.dp)
                    )
                }
            }
        }

        // --- FRUIT SELECTION MENU ---
        if (menuVisible) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 16.dp)
                    .width(160.dp)
                    .zIndex(3f),
                shape = SpeechBubbleShape(tipAtTop = true, tipPaddingEnd = 32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.wrapContentHeight()
                ) {
                    items(viewModel.itemsList) { item ->
                        val isSelected = item.id == viewModel.currentItem.id
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                    else Color.Transparent
                                )
                                .clickable {
                                    if (item.unlocked) {
                                        viewModel.selectItem(item)
                                        menuVisible = false
                                    } else {
                                        itemToPurchase = item
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val colorFilter = if (item.unlocked) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                            Box(modifier = Modifier.graphicsLayer { alpha = if (item.unlocked) 1f else 0.4f }) {
                                SpriteAnimation(
                                    painter = painterResource(item.resource),
                                    frameCount = 3,
                                    modifier = Modifier.size(48.dp),
                                    colorFilter = colorFilter
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- MAIN GAME AREA ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp, bottom = 0.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            viewModel.currentItem.Content(
                modifier = Modifier,
                onVegetableClick = { rewards -> viewModel.onVegetableClick(rewards) },
                activeModifiers = viewModel.currentItem.modifiers,
                vibrationEnabled = viewModel.vibrationEnabled,
                vibrationIntensity = viewModel.vibrationIntensity
            )
        }

        // --- CPS Meter ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(1f)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${viewModel.currentCps.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (viewModel.shaderBackgroundEnabled) Color.White else MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(Res.string.cps_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (viewModel.shaderBackgroundEnabled) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                )
            }
        }

        // --- WEATHER DIALOG ---
        if (showWeatherDialog) {
            WeatherDialog(viewModel = viewModel, onDismiss = { showWeatherDialog = false }, onNavigateToStore = onNavigateToStore)
        }

        // --- TUTORIAL DIALOG ---
        if (showTutorial) {
            AlertDialog(
                onDismissRequest = { showTutorial = false },
                modifier = Modifier.clip(SpeechBubbleShape(tipAtTop = false)),
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(viewModel.currentItem.particleEmoji)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(Res.string.bunny_guide_title, stringResource(viewModel.currentItem.nameRes)))
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(viewModel.currentItem.tutorialRes))
                        if (viewModel.currentItem.modifiers.isNotEmpty()) {
                            HorizontalDivider()
                            Text(stringResource(Res.string.upgrades_info), style = MaterialTheme.typography.titleSmall)
                            viewModel.currentItem.modifiers.forEach { mod ->
                                Column {
                                    Text(stringResource(mod.nameRes), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text(stringResource(mod.descriptionRes), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTutorial = false }) {
                        Text(stringResource(Res.string.bunny_thanks))
                    }
                }
            )
        }

        // --- PURCHASE DIALOG ---
        itemToPurchase?.let { item ->
            AlertDialog(
                onDismissRequest = { itemToPurchase = null },
                title = { Text(stringResource(Res.string.unlock_item_title, stringResource(item.nameRes))) },
                text = {
                    Column {
                        Text(stringResource(Res.string.unlock_requirement))
                        Spacer(modifier = Modifier.height(8.dp))
                        val hasMoney = viewModel.money >= item.unlockCost.money
                        val youHave = stringResource(Res.string.you_have)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SpriteAnimation(
                                painter = painterResource(Res.drawable.coin_strip),
                                frameCount = 3,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${item.unlockCost.money} ($youHave: ${viewModel.money})",
                                color = if (hasMoney) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                        item.unlockCost.vegetableCosts.forEach { (vegId, amount) ->
                            val currentCount = viewModel.fruitCounts[vegId] ?: 0
                            val vegEmoji = viewModel.itemsList.find { it.id == vegId }?.particleEmoji ?: "?"
                            val hasVeg = currentCount >= amount
                            Text("$vegEmoji $amount ($youHave: $currentCount)",
                                color = if (hasVeg) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    if (viewModel.canAfford(item.unlockCost)) {
                        Button(onClick = {
                            viewModel.tryUnlockItem(item)
                            itemToPurchase = null
                            menuVisible = false
                        }) {
                            Text(stringResource(Res.string.unlock_confirm))
                        }
                    } else {
                        Button(onClick = { 
                            onNavigateToStore()
                            itemToPurchase = null
                        }) {
                            Text(stringResource(Res.string.go_to_store))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToPurchase = null }) {
                        Text(stringResource(Res.string.close))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun WeatherDialog(viewModel: GameViewModel, onDismiss: () -> Unit, onNavigateToStore: () -> Unit) {
    val locationProvider = rememberLocationProvider()
    val weatherBonusActive = viewModel.isWeatherBonusActive
    val hasWeatherUpgrade = viewModel.globalUpgrades.find { it.id == "weather_bonus" }?.unlockedLevel ?: 0 > 0
    val scrollState = rememberScrollState()
    
    var timeRemaining by remember { mutableStateOf(0L) }
    
    LaunchedEffect(weatherBonusActive, viewModel.lastWeatherUpdateTime) {
        while(weatherBonusActive) {
            val now = Clock.System.now().toEpochMilliseconds()
            val elapsed = now - viewModel.lastWeatherUpdateTime
            timeRemaining = (viewModel.weatherBonusDuration - elapsed).coerceAtLeast(0L)
            if (timeRemaining <= 0) break
            delay(1000)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.clip(SpeechBubbleShape(tipAtTop = false)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SpriteAnimation(
                    painter = painterResource(Res.drawable.clickycheekykneel_strip),
                    frameCount = 3,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(Res.string.weather_dialog_title))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (viewModel.currentWeatherData != null) {
                    val weather = viewModel.currentWeatherData!!
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("${weather.current_weather.temperature}°C", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text(getWeatherDescription(weather.current_weather.weathercode))
                        
                        if (weatherBonusActive) {
                            val hours = (timeRemaining / (1000 * 60 * 60)) % 24
                            val minutes = (timeRemaining / (1000 * 60)) % 60
                            val seconds = (timeRemaining / 1000) % 60
                            val timeStr = "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
                            
                            Text(
                                text = "Ends in: $timeStr",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    HorizontalDivider()
                }

                // --- BONUSES LIST ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val temp = viewModel.currentWeatherData?.current_weather?.temperature ?: 0.0
                    val code = viewModel.currentWeatherData?.current_weather?.weathercode ?: -1
                    
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_cold_title),
                        desc = stringResource(Res.string.weather_bonus_cold_desc),
                        isActive = weatherBonusActive && temp < 12
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_mild_title),
                        desc = stringResource(Res.string.weather_bonus_mild_desc),
                        isActive = weatherBonusActive && temp in 12.0..22.0
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_hot_title),
                        desc = stringResource(Res.string.weather_bonus_hot_desc),
                        isActive = weatherBonusActive && temp > 22
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_sunny_title),
                        desc = stringResource(Res.string.weather_bonus_sunny_desc),
                        isActive = weatherBonusActive && code == 0
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_rain_title),
                        desc = stringResource(Res.string.weather_bonus_rain_desc),
                        isActive = weatherBonusActive && code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82)
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_clouds_title),
                        desc = stringResource(Res.string.weather_bonus_clouds_desc),
                        isActive = weatherBonusActive && code in listOf(1, 2, 3)
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_storm_title),
                        desc = stringResource(Res.string.weather_bonus_storm_desc),
                        isActive = weatherBonusActive && code in listOf(95, 96, 99)
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_snow_title),
                        desc = stringResource(Res.string.weather_bonus_snow_desc),
                        isActive = weatherBonusActive && code in listOf(71, 73, 75, 77, 85, 86)
                    )
                }

                if (!hasWeatherUpgrade) {
                    Button(
                        onClick = { 
                            onNavigateToStore()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(stringResource(Res.string.weather_buy_upgrade))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                locationProvider.requestLocation { coords ->
                    coords?.let { viewModel.updateWeather(it.latitude, it.longitude) }
                }
            }) {
                Text(stringResource(Res.string.weather_update_location))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.close))
            }
        }
    )
}

@Composable
fun WeatherBonusItem(title: String, desc: String, isActive: Boolean) {
    Column {
        Text(
            text = title,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isActive) 16.sp else 14.sp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
