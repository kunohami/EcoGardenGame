package com.rafarg.ecogardengame.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafarg.ecogardengame.model.*
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.*
import org.jetbrains.compose.ui.tooling.preview.Preview

val items: List<GameItem> = listOf(
    Tomato(),
    Broccoli(),
    BellPepper(),
    Garlic(),
    PurpleOnion(),
    Squash(),
    Apple()
)

enum class Screen(val showInBottomBar: Boolean = true) {
    GAME(true),
    STORE(true),
    LIBRARY(true),
    PROFILE(true),
    MISC(true),
    STATS(false),
    SETTINGS(false),
    THEMES(false),
    ABOUT(false)
}

@Composable
fun Screen.getTitle(): String {
    return stringResource(when(this) {
        Screen.GAME -> Res.string.nav_garden
        Screen.STORE -> Res.string.nav_shop
        Screen.LIBRARY -> Res.string.nav_library
        Screen.PROFILE -> Res.string.nav_profile
        Screen.MISC -> Res.string.nav_misc
        Screen.STATS -> Res.string.stats_title
        Screen.SETTINGS -> Res.string.settings_title
        Screen.THEMES -> Res.string.themes_title
        Screen.ABOUT -> Res.string.about_title
    })
}

/**
 * The main application entry point with bottom navigation.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun App(prefs: DataStore<Preferences>? = null) {
    val viewModel: GameViewModel = viewModel { GameViewModel(prefs) }
    var currentScreen by remember { mutableStateOf(Screen.GAME) }
    
    EcoGardenTheme(
        useDarkTheme = viewModel.isDarkTheme,
        useWavyTheme = viewModel.shaderBackgroundEnabled
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!viewModel.isDataLoaded) {
                // Splash / Loading while DataStore initializes
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // --- MAIN GAME UI ---
                
                // --- WAVY BACKGROUND LAYER ---
                WavyBackground(enabled = viewModel.shaderBackgroundEnabled)

                Scaffold(
                    containerColor = if (viewModel.shaderBackgroundEnabled) Color.Transparent else MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar {
                            Screen.entries.filter { it.showInBottomBar }.forEach { screen ->
                                NavigationBarItem(
                                    icon = {
                                        Text(
                                            text = when(screen) {
                                                Screen.GAME -> "🌱"
                                                Screen.STORE -> "💰"
                                                Screen.LIBRARY -> "📖"
                                                Screen.PROFILE -> "👤"
                                                Screen.MISC -> "⚙️"
                                                else -> "❓"
                                            },
                                            fontSize = 24.sp
                                        )
                                    },
                                    label = { Text(screen.getTitle()) },
                                    selected = currentScreen == screen,
                                    onClick = { currentScreen = screen }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            Screen.GAME -> GameScreen(
                                viewModel = viewModel,
                                onNavigateToStore = { currentScreen = Screen.STORE }
                            )
                            Screen.STORE -> StoreScreen(viewModel)
                            Screen.LIBRARY -> LibraryScreen(viewModel)
                            Screen.PROFILE -> ProfileScreen(viewModel)
                            Screen.MISC -> MiscScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                                onNavigateToThemes = { currentScreen = Screen.THEMES },
                                onNavigateToStats = { currentScreen = Screen.STATS },
                                onNavigateToAbout = { currentScreen = Screen.ABOUT }
                            )
                            Screen.STATS -> StatsScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.MISC }
                            )
                            Screen.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.MISC }
                            )
                            Screen.THEMES -> ThemesScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.MISC }
                            )
                            Screen.ABOUT -> AboutScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.MISC }
                            )
                        }
                    }
                }

                // --- GLOBAL ACHIEVEMENT TOAST ---
                AnimatedVisibility(
                    visible = viewModel.achievementToast != null,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 100.dp) // Below the status bar / top UI
                        .zIndex(10f)
                ) {
                    viewModel.achievementToast?.let { achievement ->
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 32.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            tonalElevation = 8.dp,
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(achievement.emoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        stringResource(Res.string.congratulations),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        stringResource(achievement.nameRes),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
