package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafarg.ecogardengame.model.*
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.ui.tooling.preview.Preview

val items: List<GameItem> = listOf(
    Tomato(),
    Broccoli(),
    BellPepper(),
    Garlic(),
    PurpleOnion(),
    Squash()
)

/**
 * The main application entry point with bottom navigation.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun App(prefs: DataStore<Preferences>? = null) {
    val viewModel: GameViewModel = viewModel { GameViewModel(prefs) }
    var currentScreen by remember { mutableStateOf(Screen.GAME) }
    
    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    // Only show screens that are marked for the bottom bar
                    Screen.entries.filter { it.showInBottomBar }.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Text(
                                    text = when(screen) {
                                        Screen.GAME -> "🌱"
                                        Screen.STORE -> "💰"
                                        Screen.STATS -> "📊"
                                        Screen.PROFILE -> "👤"
                                        Screen.MISC -> "⚙️"
                                        else -> "❓"
                                    },
                                    fontSize = 24.sp
                                )
                            },
                            label = { Text(screen.title) },
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
                    Screen.GAME -> GameScreen(viewModel)
                    Screen.STORE -> StoreScreen(viewModel)
                    Screen.STATS -> StatsScreen(viewModel)
                    Screen.PROFILE -> ProfileScreen(viewModel)
                    Screen.MISC -> MiscScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                        onNavigateToAbout = { currentScreen = Screen.ABOUT }
                    )
                    Screen.SETTINGS -> SettingsScreen(
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
    }
}
