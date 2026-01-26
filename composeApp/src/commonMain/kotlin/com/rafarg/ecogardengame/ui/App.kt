package com.rafarg.ecogardengame.ui

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafarg.ecogardengame.auth.AuthRepository
import com.rafarg.ecogardengame.data.DataStoreGameRepository
import com.rafarg.ecogardengame.model.*
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs

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
    ABOUT(false),
    LOGIN(false)
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
        Screen.LOGIN -> Res.string.login_title
    })
}

/**
 * The main application entry point with bottom navigation and 3D Cube Transitions.
 */
@OptIn(ExperimentalResourceApi::class, ExperimentalFoundationApi::class)
@Composable
@Preview
fun App(
    prefs: DataStore<Preferences>? = null,
    authRepository: AuthRepository? = null,
    onGoogleSignIn: () -> Unit = {}
) {
    val viewModel: GameViewModel = viewModel { 
        val repository = if (prefs != null) DataStoreGameRepository(prefs) else object : com.rafarg.ecogardengame.data.GameRepository {
            override suspend fun loadGameData() = com.rafarg.ecogardengame.data.GameSaveData()
            override suspend fun saveGameData(data: com.rafarg.ecogardengame.data.GameSaveData) {}
        }
        GameViewModel(repository, authRepository) 
    }
    
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(Screen.GAME) }
    
    // Screens shown in the bottom bar
    val mainScreens = remember { Screen.entries.filter { it.showInBottomBar } }
    val pagerState = rememberPagerState(pageCount = { mainScreens.size })

    // Sync Pager with currentScreen when navigation happens
    LaunchedEffect(currentScreen) {
        val index = mainScreens.indexOf(currentScreen)
        if (index != -1 && pagerState.currentPage != index) {
            pagerState.animateScrollToPage(index)
        }
    }
    
    EcoGardenTheme(
        useDarkTheme = viewModel.isDarkTheme,
        useWavyTheme = viewModel.shaderBackgroundEnabled,
        useAutumnTheme = viewModel.isAutumnTheme
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!viewModel.isDataLoaded) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                WavyBackground(enabled = viewModel.shaderBackgroundEnabled)

                Scaffold(
                    containerColor = if (viewModel.shaderBackgroundEnabled) Color.Transparent else MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.height(120.dp),
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            tonalElevation = 8.dp
                        ) {
                            mainScreens.forEach { screen ->
                                NavigationBarItem(
                                    selected = currentScreen == screen,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        val iconRes = when(screen) {
                                            Screen.GAME -> Res.drawable.gardenmainmenu_strip
                                            Screen.STORE -> Res.drawable.shopmainmenu_strip
                                            Screen.LIBRARY -> Res.drawable.librarymainmenu_strip
                                            Screen.PROFILE -> Res.drawable.profilemainmenu_strip
                                            Screen.MISC -> Res.drawable.miscmainmenu_strip
                                            else -> null
                                        }
                                        if (iconRes != null) {
                                            SpriteAnimation(
                                                painter = painterResource(iconRes),
                                                frameCount = 3,
                                                modifier = Modifier.size(56.dp)
                                            )
                                        } else {
                                            Text(text = "❓", fontSize = 24.sp)
                                        }
                                    },
                                    label = null,
                                    alwaysShowLabel = false,
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    )
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
                        if (currentScreen.showInBottomBar) {
                            // --- MAIN SCREENS WITH CUBE TRANSITION (NO GESTURES) ---
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = false // DESACTIVADO EL DESLIZAMIENTO MANUAL
                            ) { page ->
                                val screen = mainScreens[page]
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            // CUBE TRANSFORMATION LOGIC
                                            val pageOffset = ( (pagerState.currentPage - page ) + pagerState.currentPageOffsetFraction )
                                            
                                            rotationY = pageOffset * 90f
                                            
                                            transformOrigin = if (pageOffset > 0f) {
                                                TransformOrigin(0f, 0.5f) 
                                            } else {
                                                TransformOrigin(1f, 0.5f)
                                            }
                                            
                                            cameraDistance = 12f * density
                                            alpha = 1f - abs(pageOffset).coerceIn(0f, 1f) * 0.3f
                                        }
                                ) {
                                    when (screen) {
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
                                            onNavigateToAbout = { currentScreen = Screen.ABOUT },
                                            onNavigateToLogin = { currentScreen = Screen.LOGIN }
                                        )
                                        else -> Unit
                                    }
                                }
                            }
                        } else {
                            // --- SUB-SCREENS ---
                            when (currentScreen) {
                                Screen.STATS -> StatsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.MISC })
                                Screen.SETTINGS -> SettingsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.MISC })
                                Screen.THEMES -> ThemesScreen(viewModel = viewModel, onBack = { currentScreen = Screen.MISC })
                                Screen.ABOUT -> AboutScreen(viewModel = viewModel, onBack = { currentScreen = Screen.MISC })
                                Screen.LOGIN -> LoginScreen(viewModel = viewModel, onBack = { currentScreen = Screen.MISC }, onGoogleSignIn = onGoogleSignIn)
                                else -> Unit
                            }
                        }
                    }
                }

                // Achievement Toast...
                AnimatedVisibility(
                    visible = viewModel.achievementToast != null,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 100.dp)
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
                                val congrats = stringResource(Res.string.congratulations)
                                val unlockedText = stringResource(Res.string.unlocked_label, stringResource(achievement.nameRes))
                                Column {
                                    Text(congrats, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(unlockedText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
