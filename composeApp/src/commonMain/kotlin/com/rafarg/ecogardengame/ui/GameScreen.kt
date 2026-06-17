package com.rafarg.ecogardengame.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.rafarg.ecogardengame.model.GameItem
import com.rafarg.ecogardengame.util.rememberLocationProvider
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import com.rafarg.ecogardengame.viewmodel.getWeatherDescription
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Codename: EcoGardenGame
 * Final name: Clicky's Garden
 * Developed by: Rafael Robles García
 *
 * GameScreen is the main interactive screen of the game.
 * It displays the current vegetable, the currency (money),
 * and various menus for navigation and progression.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel, // Manages the game logic and state
    onNavigateToStore: () -> Unit, // Callback to navigate to the shop
) {
    // --- UI STATE VARIABLES ---
    // These 'remember' variables keep track of UI states like dialog visibility.
    var itemToPurchase by remember { mutableStateOf<GameItem?>(null) }
    var showWeatherDialog by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }
    var vegetableMenuVisible by remember { mutableStateOf(false) }

    // Check if background shaders are enabled from settings
    val wavy = viewModel.shaderBackgroundEnabled
    val topBarHeight = 64.dp

    // Box is like a FrameLayout, it allows layering elements on top of each other.
    Box(modifier = Modifier.fillMaxSize()) {
        // --- MAIN GAME AREA ---
        // This is where the vegetable is displayed and clicked.
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = topBarHeight)
                    .zIndex(1f),
            // Lower zIndex means it's at the back
            contentAlignment = Alignment.Center,
        ) {
            // Displays the current vegetable and handles its clicking logic
            viewModel.currentItem.Content(
                modifier = Modifier,
                onVegetableClick = { viewModel.onVegetableClick(it) },
                activeModifiers = viewModel.currentItem.modifiers.filter { it.isEnabled },
                vibrationEnabled = viewModel.vibrationEnabled,
                vibrationIntensity = viewModel.vibrationIntensity,
            )
        }

        // --- DISMISS LAYER FOR MENU ---
        // If the side menu is open, this invisible layer detects clicks
        // outside the menu to close it automatically.
        if (vegetableMenuVisible) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .zIndex(5f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // No visual ripple effect
                        ) { vegetableMenuVisible = false },
            )
        }

        // --- TOP BAR ---
        // Contains the Weather button, Money counter, Tutorial, and Menu buttons.
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(topBarHeight)
                    .padding(horizontal = 16.dp)
                    .zIndex(10f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Weather Button (Animated Sprite)
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { showWeatherDialog = true },
                contentAlignment = Alignment.Center,
            ) {
                SpriteAnimation(
                    painter = painterResource(Res.drawable.clickycheekykneel_strip),
                    frameCount = 3,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Money Counter: Shows current currency with a coin animation
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SpriteAnimation(
                        painter = painterResource(Res.drawable.coin_strip),
                        frameCount = 3,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${viewModel.money}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (wavy) Color.White else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Right side buttons: Bunny (Tutorial) then Fruit Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Tutorial Button
                Box(
                    modifier =
                        Modifier
                            .size(56.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { showTutorial = true },
                    contentAlignment = Alignment.Center,
                ) {
                    SpriteAnimation(
                        painter = painterResource(Res.drawable.infobunny_strip),
                        frameCount = 3,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // Fruit Menu Toggle Button
                Box(
                    modifier =
                        Modifier
                            .height(56.dp)
                            .width(87.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { vegetableMenuVisible = !vegetableMenuVisible },
                    contentAlignment = Alignment.Center,
                ) {
                    SpriteAnimation(
                        painter = painterResource(Res.drawable.fruitmenu_strip),
                        frameCount = 3,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        // --- FRUIT DROPDOWN OVERLAY ---
        // A vertical list of vegetables to switch between.
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = topBarHeight, end = 16.dp)
                    .zIndex(15f),
            contentAlignment = Alignment.TopEnd,
        ) {
            AnimatedVisibility(
                visible = vegetableMenuVisible,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        viewModel.itemsList.forEach { item ->
                            val isSelected = viewModel.currentItem.id == item.id
                            val isUnlocked = item.unlocked

                            Box(
                                modifier =
                                    Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                Color.Transparent
                                            },
                                        )
                                        .clickable {
                                            if (isUnlocked) {
                                                viewModel.selectItem(item) // Switch to this item
                                            } else {
                                                itemToPurchase = item // Open purchase dialog
                                            }
                                            vegetableMenuVisible = false
                                        },
                                contentAlignment = Alignment.Center,
                            ) {
                                SpriteAnimation(
                                    painter = painterResource(item.resource),
                                    frameCount = 3,
                                    modifier = Modifier.size(36.dp).graphicsLayer { alpha = if (isUnlocked) 1f else 0.4f },
                                )
                                if (!isUnlocked) {
                                    Text("🔒", modifier = Modifier.align(Alignment.BottomEnd), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- BOTTOM RIGHT: STATS & RESOURCES ---
        // Displays how many items of the current vegetable you have collected
        // and your current "Coins Per Second" (CPS) generation.
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .zIndex(20f),
        ) {
            Column(horizontalAlignment = Alignment.End) {
                val currentCount = viewModel.fruitCounts[viewModel.currentItem.id] ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$currentCount",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (wavy) Color.White else MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    SpriteAnimation(
                        painter = painterResource(viewModel.currentItem.resource),
                        frameCount = 3,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Text(
                    text = "${viewModel.currentCps} ${stringResource(Res.string.cps_label)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (wavy) Color.White else MaterialTheme.colorScheme.primary,
                )
            }
        }

        // --- DIALOGS SECTION ---

        // 1. Weather Dialog: Manages weather-based bonuses
        if (showWeatherDialog) {
            WeatherDialog(viewModel = viewModel, onDismiss = { showWeatherDialog = false }, onNavigateToStore = onNavigateToStore)
        }

        // 2. Tutorial Dialog: Explains the current vegetable and its upgrades
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
                                    Text(
                                        stringResource(mod.nameRes),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
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
                },
            )
        }

        // 3. Unlock Item Dialog: Shown when clicking a locked vegetable in the menu
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
                        // Requirement: Money
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SpriteAnimation(
                                painter = painterResource(Res.drawable.coin_strip),
                                frameCount = 3,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${item.unlockCost.money} ($youHave: ${viewModel.money})",
                                color = if (hasMoney) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            )
                        }
                        // Requirement: Other vegetables
                        item.unlockCost.vegetableCosts.forEach { (vegId, amount) ->
                            val currentCount = viewModel.fruitCounts[vegId] ?: 0
                            val vegEmoji = viewModel.itemsList.find { it.id == vegId }?.particleEmoji ?: "?"
                            val hasVeg = currentCount >= amount
                            Text(
                                "$vegEmoji $amount ($youHave: $currentCount)",
                                color = if (hasVeg) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                confirmButton = {
                    if (viewModel.canAfford(item.unlockCost)) {
                        Button(onClick = {
                            viewModel.tryUnlockItem(item)
                            itemToPurchase = null
                        }) {
                            Text(stringResource(Res.string.unlock_confirm))
                        }
                    } else {
                        // If not enough resources, offer to go to the shop
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
                },
            )
        }
    }
}

/**
 * WeatherDialog displays real-world weather and applies bonuses to game production.
 * This encourages players to play in different real-world conditions!
 */
@OptIn(ExperimentalTime::class)
@Composable
fun WeatherDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit,
    onNavigateToStore: () -> Unit,
) {
    val locationProvider = rememberLocationProvider()
    val weatherBonusActive = viewModel.isWeatherBonusActive
    val hasWeatherUpgrade = viewModel.globalUpgrades.find { it.id == "weather_bonus" }?.unlockedLevel ?: 0 > 0
    val scrollState = rememberScrollState()

    // Countdown timer for active weather bonuses
    var timeRemaining by remember { mutableStateOf(0L) }

    // Side effect to update the timer every second
    LaunchedEffect(weatherBonusActive, viewModel.lastWeatherUpdateTime) {
        while (weatherBonusActive) {
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
                    modifier = Modifier.size(40.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(Res.string.weather_dialog_title))
            }
        },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // If weather data is available, show temperature and description
                if (viewModel.currentWeatherData != null) {
                    val weather = viewModel.currentWeatherData!!
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("${weather.current_weather.temperature}°C", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text(getWeatherDescription(weather.current_weather.weathercode))

                        // Show timer if a bonus is currently active
                        if (weatherBonusActive) {
                            val hours = (timeRemaining / (1000 * 60 * 60)) % 24
                            val minutes = (timeRemaining / (1000 * 60)) % 60
                            val seconds = (timeRemaining / 1000) % 60
                            val timeStr = "${hours.toString().padStart(
                                2,
                                '0',
                            )}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

                            Text(
                                text = "Ends in: $timeStr",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    HorizontalDivider()
                }

                // --- BONUSES LIST ---
                // Shows various weather conditions and highlights the one currently active.
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val temp = viewModel.currentWeatherData?.current_weather?.temperature ?: 0.0
                    val code = viewModel.currentWeatherData?.current_weather?.weathercode ?: -1

                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_cold_title),
                        desc = stringResource(Res.string.weather_bonus_cold_desc),
                        isActive = weatherBonusActive && temp < 12,
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_mild_title),
                        desc = stringResource(Res.string.weather_bonus_mild_desc),
                        isActive = weatherBonusActive && temp in 12.0..22.0,
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_hot_title),
                        desc = stringResource(Res.string.weather_bonus_hot_desc),
                        isActive = weatherBonusActive && temp > 22,
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_sunny_title),
                        desc = stringResource(Res.string.weather_bonus_sunny_desc),
                        isActive = weatherBonusActive && code == 0,
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_rain_title),
                        desc = stringResource(Res.string.weather_bonus_rain_desc),
                        isActive = weatherBonusActive && code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82),
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_clouds_title),
                        desc = stringResource(Res.string.weather_bonus_clouds_desc),
                        isActive = weatherBonusActive && code in listOf(1, 2, 3),
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_storm_title),
                        desc = stringResource(Res.string.weather_bonus_storm_desc),
                        isActive = weatherBonusActive && code in listOf(95, 96, 99),
                    )
                    WeatherBonusItem(
                        title = stringResource(Res.string.weather_bonus_snow_title),
                        desc = stringResource(Res.string.weather_bonus_snow_desc),
                        isActive = weatherBonusActive && code in listOf(71, 73, 75, 77, 85, 86),
                    )
                }

                // If the player hasn't bought the weather upgrade yet, show a button to the store
                if (!hasWeatherUpgrade) {
                    Button(
                        onClick = {
                            onNavigateToStore()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    ) {
                        Text(stringResource(Res.string.weather_buy_upgrade))
                    }
                }
            }
        },
        confirmButton = {
            // Button to refresh the current location and weather
            Button(onClick = {
                locationProvider.requestLocation { coords ->
                    coords?.let { viewModel.updateWeather(it.latitude, it.longitude) }
                }
            }) {
                Text(stringResource(Res.string.weather_update_location))
            }
        },
    )
}

/**
 * A small UI component representing a single weather bonus entry.
 */
@Composable
fun WeatherBonusItem(
    title: String,
    desc: String,
    isActive: Boolean,
) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(text = desc, style = MaterialTheme.typography.labelSmall)
        }
    }
}
