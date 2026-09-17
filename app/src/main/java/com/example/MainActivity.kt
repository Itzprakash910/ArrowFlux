package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SoundManager
import com.example.game.classic.ClassicArrowScreen
import com.example.game.maze.ArrowMazeScreen
import com.example.game.threed.ThreeDArrowScreen
import com.example.haptics.HapticManager
import com.example.model.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

enum class MainTab {
    HOME,
    LEVELS,
    DAILY,
    PROFILE
}

enum class ActiveScreen {
    TABS,
    MODES,
    ACHIEVEMENTS,
    THEMES,
    SHOP,
    SETTINGS,
    PLAYING
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ArrowFluxApp()
            }
        }
    }
}

@Composable
fun ArrowFluxApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefsManager = remember { PreferencesManager(context) }
    val soundManager = remember { SoundManager(coroutineScope) }
    val hapticManager = remember { HapticManager(context) }

    val playerData by prefsManager.playerData.collectAsStateWithLifecycle()

    // Active theme lookup
    val activeTheme = remember(playerData.activeThemeId) {
        GameTheme.values().find { it.id == playerData.activeThemeId } ?: GameTheme.NEON
    }

    // Sync audio/vibration toggles
    LaunchedEffect(playerData.soundEnabled, playerData.vibrationEnabled) {
        soundManager.isSoundEnabled = playerData.soundEnabled
        soundManager.isMusicEnabled = playerData.musicEnabled
        hapticManager.isVibrationEnabled = playerData.vibrationEnabled
    }

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var activeScreen by remember { mutableStateOf(ActiveScreen.TABS) }
    var currentGameMode by remember { mutableStateOf(GameMode.CLASSIC) }
    var currentLevelId by remember { mutableStateOf(1) }

    BackHandler(enabled = activeScreen != ActiveScreen.TABS || currentTab != MainTab.HOME) {
        if (activeScreen != ActiveScreen.TABS) {
            activeScreen = ActiveScreen.TABS
        } else if (currentTab != MainTab.HOME) {
            currentTab = MainTab.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = activeTheme.background,
        bottomBar = {
            if (activeScreen == ActiveScreen.TABS) {
                NavigationBar(
                    containerColor = activeTheme.surface,
                    contentColor = activeTheme.primary,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.HOME,
                        onClick = {
                            currentTab = MainTab.HOME
                            soundManager.playTap()
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("HOME", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeTheme.primary,
                            selectedTextColor = activeTheme.primary,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = activeTheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.LEVELS,
                        onClick = {
                            currentTab = MainTab.LEVELS
                            soundManager.playTap()
                        },
                        icon = { Icon(Icons.Default.GridOn, contentDescription = "Levels") },
                        label = { Text("LEVELS", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeTheme.primary,
                            selectedTextColor = activeTheme.primary,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = activeTheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_levels")
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.DAILY,
                        onClick = {
                            currentTab = MainTab.DAILY
                            soundManager.playTap()
                        },
                        icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Daily") },
                        label = { Text("DAILY", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeTheme.primary,
                            selectedTextColor = activeTheme.primary,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = activeTheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_daily")
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.PROFILE,
                        onClick = {
                            currentTab = MainTab.PROFILE
                            soundManager.playTap()
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("PROFILE", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeTheme.primary,
                            selectedTextColor = activeTheme.primary,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = activeTheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (activeScreen == ActiveScreen.TABS) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            AnimatedContent(
                targetState = activeScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.96f, animationSpec = tween(220)) togetherWith
                            fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 1.02f, animationSpec = tween(180))
                },
                label = "screenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    ActiveScreen.TABS -> {
                        when (currentTab) {
                            MainTab.HOME -> HomeScreen(
                                playerData = playerData,
                                theme = activeTheme,
                                onQuickPlay = {
                                    val nextLvl = (1..100).firstOrNull { !playerData.completedClassic.containsKey(it) } ?: 1
                                    currentGameMode = GameMode.CLASSIC
                                    currentLevelId = nextLvl
                                    activeScreen = ActiveScreen.PLAYING
                                },
                                onOpenDaily = { currentTab = MainTab.DAILY },
                                onOpenLevels = { currentTab = MainTab.LEVELS },
                                onOpenModes = { activeScreen = ActiveScreen.MODES },
                                onOpenAchievements = { activeScreen = ActiveScreen.ACHIEVEMENTS },
                                onOpenThemes = { activeScreen = ActiveScreen.THEMES },
                                onOpenShop = { activeScreen = ActiveScreen.SHOP },
                                onOpenSettings = { activeScreen = ActiveScreen.SETTINGS },
                                onOpenProfile = { currentTab = MainTab.PROFILE }
                            )
                            MainTab.LEVELS -> LevelsScreen(
                                initialMode = currentGameMode,
                                playerData = playerData,
                                theme = activeTheme,
                                onBack = { currentTab = MainTab.HOME },
                                onStartLevel = { mode, lvl ->
                                    currentGameMode = mode
                                    currentLevelId = lvl
                                    activeScreen = ActiveScreen.PLAYING
                                }
                            )
                            MainTab.DAILY -> DailyChallengeScreen(
                                playerData = playerData,
                                theme = activeTheme,
                                onBack = { currentTab = MainTab.HOME },
                                onStartDaily = { mode, lvl ->
                                    currentGameMode = mode
                                    currentLevelId = lvl
                                    activeScreen = ActiveScreen.PLAYING
                                }
                            )
                            MainTab.PROFILE -> ProfileScreen(
                                playerData = playerData,
                                theme = activeTheme,
                                onBack = { currentTab = MainTab.HOME },
                                onOpenSettings = { activeScreen = ActiveScreen.SETTINGS },
                                onOpenAchievements = { activeScreen = ActiveScreen.ACHIEVEMENTS }
                            )
                        }
                    }
                    ActiveScreen.MODES -> GameModesScreen(
                        playerData = playerData,
                        theme = activeTheme,
                        onBack = { activeScreen = ActiveScreen.TABS },
                        onSelectMode = { mode ->
                            currentGameMode = mode
                            currentTab = MainTab.LEVELS
                            activeScreen = ActiveScreen.TABS
                        }
                    )
                    ActiveScreen.ACHIEVEMENTS -> AchievementsScreen(
                        playerData = playerData,
                        theme = activeTheme,
                        onBack = { activeScreen = ActiveScreen.TABS }
                    )
                    ActiveScreen.THEMES -> ThemesScreen(
                        playerData = playerData,
                        theme = activeTheme,
                        prefsManager = prefsManager,
                        soundManager = soundManager,
                        onBack = { activeScreen = ActiveScreen.TABS }
                    )
                    ActiveScreen.SHOP -> ShopScreen(
                        playerData = playerData,
                        theme = activeTheme,
                        prefsManager = prefsManager,
                        soundManager = soundManager,
                        onBack = { activeScreen = ActiveScreen.TABS }
                    )
                    ActiveScreen.SETTINGS -> SettingsScreen(
                        playerData = playerData,
                        theme = activeTheme,
                        prefsManager = prefsManager,
                        soundManager = soundManager,
                        hapticManager = hapticManager,
                        onBack = { activeScreen = ActiveScreen.TABS }
                    )
                    ActiveScreen.PLAYING -> {
                        when (currentGameMode) {
                            GameMode.CLASSIC -> ClassicArrowScreen(
                                levelId = currentLevelId,
                                theme = activeTheme,
                                playerData = playerData,
                                soundManager = soundManager,
                                hapticManager = hapticManager,
                                prefsManager = prefsManager,
                                onBack = { activeScreen = ActiveScreen.TABS },
                                onNextLevel = { nextLvl ->
                                    currentLevelId = nextLvl.coerceAtMost(100)
                                }
                            )
                            GameMode.MAZE -> ArrowMazeScreen(
                                levelId = currentLevelId,
                                theme = activeTheme,
                                playerData = playerData,
                                soundManager = soundManager,
                                hapticManager = hapticManager,
                                prefsManager = prefsManager,
                                onBack = { activeScreen = ActiveScreen.TABS },
                                onNextLevel = { nextLvl ->
                                    currentLevelId = nextLvl.coerceAtMost(100)
                                }
                            )
                            GameMode.THREE_D -> ThreeDArrowScreen(
                                levelId = currentLevelId,
                                theme = activeTheme,
                                playerData = playerData,
                                soundManager = soundManager,
                                hapticManager = hapticManager,
                                prefsManager = prefsManager,
                                onBack = { activeScreen = ActiveScreen.TABS },
                                onNextLevel = { nextLvl ->
                                    currentLevelId = nextLvl.coerceAtMost(50)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
