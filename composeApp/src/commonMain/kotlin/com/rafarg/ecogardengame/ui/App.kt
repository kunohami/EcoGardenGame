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
import org.jetbrains.compose.resources.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs

/**
 * --- POLYMORPHISM (OOP Principle) ---
 * Here we define a list of objects that all implement the 'GameItem' interface.
 * Even though 'Tomato', 'Broccoli', etc., are different classes with unique mechanics,
 * the rest of the app can treat them as a generic 'GameItem'.
 * This makes the code scalable: adding a new vegetable only requires adding it to this list.
 */
val items: List<GameItem> =
    listOf(
        Tomato(),
        Broccoli(),
        BellPepper(),
        Garlic(),
        PurpleOnion(),
        Squash(),
        Apple(),
    )

/**
 * --- ENCAPSULATION (OOP Principle) ---
 * This Enum centralizes all screen-related metadata.
 * By defining 'showInBottomBar' here, we don't need 'if' statements spread across the UI
 * to decide which screens appear in the navigation bar.
 */
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
    LOGIN(false),
    GALLERY(false),
    TUTORIAL(false),
    WEATHER(false),
}

/**
 * Extension function for the Screen enum.
 * Kotlin allows adding functionality to existing classes without inheriting from them.
 * This helper returns the localized title string for each screen.
 */
@Composable
fun Screen.getTitle(): String {
    return stringResource(
        when (this) {
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
            Screen.GALLERY -> Res.string.achievements_title
            Screen.TUTORIAL -> Res.string.tutorial_title
            Screen.WEATHER -> Res.string.app_name
        },
    )
}

/**
 * The main entry point of the application UI.
 *
 * @param prefs DataStore for persistence (passed from platform-specific code).
 * @param authRepository Handles Google Auth (Dependency Injection principle).
 * @param onGoogleSignIn Callback to trigger the native login flow.
 */
@OptIn(ExperimentalResourceApi::class, ExperimentalFoundationApi::class)
@Composable
@Preview
fun App(
    prefs: DataStore<Preferences>? = null,
    authRepository: AuthRepository? = null,
    onGoogleSignIn: () -> Unit = {},
) {
    /**
     * --- STATE MANAGEMENT & MVVM ---
     * We initialize the ViewModel using a factory.
     * The ViewModel acts as the "Source of Truth" for all game data.
     */
    val viewModel: GameViewModel =
        viewModel {
            val repository =
                if (prefs != null) {
                    DataStoreGameRepository(prefs)
                } else {
                    // Mock repository for Previews/Tests
                    object : com.rafarg.ecogardengame.data.GameRepository {
                        override suspend fun loadGameData() = com.rafarg.ecogardengame.data.GameSaveData()

                        override suspend fun saveGameData(data: com.rafarg.ecogardengame.data.GameSaveData) {}
                    }
                }
            GameViewModel(repository, authRepository)
        }

    val scope = rememberCoroutineScope()

    // Local state for navigation
    var currentScreen by remember { mutableStateOf(Screen.GAME) }

    // Pager state for the 3D Cube transition
    val mainScreens = remember { Screen.entries.filter { it.showInBottomBar } }
    val pagerState = rememberPagerState(pageCount = { mainScreens.size })

    /**
     * --- SIDE EFFECTS ---
     * LaunchedEffect reacts to changes in 'currentScreen'.
     * When the user picks a screen from the bottom bar, we programmatically scroll the pager.
     */
    LaunchedEffect(currentScreen) {
        val index = mainScreens.indexOf(currentScreen)
        if (index != -1 && pagerState.currentPage != index) {
            pagerState.animateScrollToPage(index)
        }
    }

    /**
     * --- COMPOSITION & THEMING ---
     * We wrap the entire UI in our custom Theme.
     * It observes 'viewModel' states to toggle Dark/Wavy/Autumn modes instantly.
     */
    EcoGardenTheme(
        useDarkTheme = viewModel.isDarkTheme,
        useWavyTheme = viewModel.shaderBackgroundEnabled,
        useAutumnTheme = viewModel.isAutumnTheme,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!viewModel.isDataLoaded) {
                // Initial loading screen
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Background Layer (Wavy shader)
                WavyBackground(enabled = viewModel.shaderBackgroundEnabled)

                /**
                 * --- MATERIAL 3 SCAFFOLD ---
                 * A standard Android layout structure providing slots for a TopBar, BottomBar, etc.
                 */
                Scaffold(
                    containerColor = if (viewModel.shaderBackgroundEnabled) Color.Transparent else MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.height(120.dp),
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            tonalElevation = 8.dp,
                        ) {
                            mainScreens.forEach { screen ->
                                NavigationBarItem(
                                    selected = currentScreen == screen,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        val iconRes =
                                            when (screen) {
                                                Screen.GAME -> Res.drawable.gardenmainmenu_strip
                                                Screen.STORE -> Res.drawable.shopmainmenu_strip
                                                Screen.LIBRARY -> Res.drawable.librarymainmenu_strip
                                                Screen.PROFILE -> Res.drawable.profilemainmenu_strip
                                                Screen.MISC -> Res.drawable.miscmainmenu_strip
                                                else -> null
                                            }
                                        if (iconRes != null) {
                                            // Animate icons in the bottom bar
                                            SpriteAnimation(
                                                painter = painterResource(iconRes),
                                                frameCount = 3,
                                                modifier = Modifier.size(56.dp),
                                            )
                                        } else {
                                            Text(text = "❓", fontSize = 24.sp)
                                        }
                                    },
                                    label = null,
                                    alwaysShowLabel = false,
                                    colors =
                                        NavigationBarItemDefaults.colors(
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        ),
                                )
                            }
                        }
                    },
                ) { innerPadding ->
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                    ) {
                        /**
                         * --- NAVIGATION LOGIC ---
                         * Screens in the Bottom Bar use a Pager for the Cube effect.
                         * Secondary screens (Settings, Stats) are swapped using standard Compose logic.
                         */
                        if (currentScreen.showInBottomBar) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = false, // Manual scrolling is disabled to prioritize clicking
                            ) { page ->
                                val screen = mainScreens[page]

                                /**
                                 * --- 3D CUBE EFFECT (Graphics Math) ---
                                 * We manipulate the 'graphicsLayer' based on the scroll progress.
                                 * - rotationY: Turns the page like a face of a cube.
                                 * - transformOrigin: Sets the "hinge" at the edge.
                                 * - cameraDistance: Adds perspective depth.
                                 */
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                                            rotationY = pageOffset * 90f
                                            transformOrigin = if (pageOffset > 0f) TransformOrigin(0f, 0.5f) else TransformOrigin(1f, 0.5f)
                                            cameraDistance = 12f * density
                                            alpha = 1f - abs(pageOffset).coerceIn(0f, 1f) * 0.3f
                                        },
                                ) {
                                    when (screen) {
                                        Screen.GAME ->
                                            GameScreen(
                                                viewModel = viewModel,
                                                onNavigateToStore = { currentScreen = Screen.STORE },
                                            )
                                        Screen.STORE -> StoreScreen(viewModel)
                                        Screen.LIBRARY -> LibraryScreen(viewModel)
                                        Screen.PROFILE -> ProfileScreen(viewModel)
                                        Screen.MISC ->
                                            MiscScreen(
                                                viewModel = viewModel,
                                                onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                                                onNavigateToThemes = { currentScreen = Screen.THEMES },
                                                onNavigateToStats = { currentScreen = Screen.STATS },
                                                onNavigateToAbout = { currentScreen = Screen.ABOUT },
                                                onNavigateToLogin = { currentScreen = Screen.LOGIN },
                                                onNavigateToGallery = { currentScreen = Screen.GALLERY },
                                                onNavigateToTutorial = { viewModel.replayTutorial() },
                                                onNavigateToWeather = { currentScreen = Screen.WEATHER },
                                            )
                                        else -> Unit
                                    }
                                }
                            }
                        } else {
                            // Non-pager screens (Overlay style)
                            when (currentScreen) {
                                Screen.STATS -> StatsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.MISC })
                                Screen.SETTINGS -> SettingsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.MISC })
                                Screen.THEMES -> ThemesScreen(viewModel = viewModel, onBack = { currentScreen = Screen.MISC })
                                Screen.ABOUT -> AboutScreen(viewModel = viewModel, onBack = { currentScreen = Screen.MISC })
                                Screen.LOGIN ->
                                    LoginScreen(
                                        viewModel = viewModel,
                                        onBack = { currentScreen = Screen.MISC },
                                        onGoogleSignIn = onGoogleSignIn,
                                    )
                                Screen.GALLERY -> GalleryScreen(viewModel = viewModel, onBack = { currentScreen = Screen.MISC })
                                Screen.WEATHER -> WeatherScreen(viewModel = viewModel, onBack = { currentScreen = Screen.MISC })
                                else -> Unit
                            }
                        }
                    }
                }

                // Overlay Tutorial: Appears over everything if not seen yet
                if (viewModel.showTutorial) {
                    TutorialScreen(viewModel)
                }

                /**
                 * --- ANIMATED UI COMPONENTS ---
                 * This is an Achievement Toast. It uses 'AnimatedVisibility' to slide in/out.
                 * It also adapts its color palette based on the active game theme.
                 */
                AnimatedVisibility(
                    visible = viewModel.achievementToast != null,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 100.dp)
                            .zIndex(10f),
                ) {
                    viewModel.achievementToast?.let { achievement ->
                        val isLight = !viewModel.isDarkTheme && !viewModel.shaderBackgroundEnabled && !viewModel.isAutumnTheme
                        val surfaceColor = if (isLight) Color(0xFF3E442B) else MaterialTheme.colorScheme.primaryContainer
                        Surface(
                            modifier =
                                Modifier
                                    .padding(horizontal = 32.dp)
                                    .clip(RoundedCornerShape(24.dp)),
                            color = surfaceColor,
                            tonalElevation = 8.dp,
                            shadowElevation = 8.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(achievement.emoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                val congrats = stringResource(Res.string.congratulations)
                                val unlockedText = stringResource(Res.string.unlocked_label, stringResource(achievement.nameRes))
                                Column {
                                    Text(
                                        congrats,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isLight) Color(0xFFCCD5AE) else MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        unlockedText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLight) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
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
