package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameEngineCanvas
import com.example.ui.GameForgeViewModel
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishOnPrimaryContainer
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishTextSecondary

@Composable
fun GameForgeHomeScreen(
    viewModel: GameForgeViewModel
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isPlayingGame by viewModel.isPlayingGame.collectAsState()
    val activeConfig by viewModel.activeConfig.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBackground)
    ) {
        // Main Tab Content
        Scaffold(
            containerColor = PolishBackground,
            bottomBar = {
                if (!isPlayingGame) {
                    NavigationBar(
                        containerColor = PolishBackground,
                        contentColor = PolishOnPrimaryContainer,
                        tonalElevation = 3.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { viewModel.setTab(0) },
                            icon = { Icon(Icons.Default.Build, contentDescription = "Studio") },
                            label = { Text("Studio", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PolishOnPrimaryContainer,
                                unselectedIconColor = PolishTextSecondary,
                                selectedTextColor = PolishOnPrimaryContainer,
                                unselectedTextColor = PolishTextSecondary,
                                indicatorColor = PolishPrimaryContainer
                            ),
                            modifier = Modifier.testTag("tab_studio")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { viewModel.setTab(1) },
                            icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Arcade") },
                            label = { Text("Arcade", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PolishOnPrimaryContainer,
                                unselectedIconColor = PolishTextSecondary,
                                selectedTextColor = PolishOnPrimaryContainer,
                                unselectedTextColor = PolishTextSecondary,
                                indicatorColor = PolishPrimaryContainer
                            ),
                            modifier = Modifier.testTag("tab_arcade")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { viewModel.setTab(2) },
                            icon = { Icon(Icons.Default.Language, contentDescription = "Web PWA") },
                            label = { Text("Web PWA", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PolishOnPrimaryContainer,
                                unselectedIconColor = PolishTextSecondary,
                                selectedTextColor = PolishOnPrimaryContainer,
                                unselectedTextColor = PolishTextSecondary,
                                indicatorColor = PolishPrimaryContainer
                            ),
                            modifier = Modifier.testTag("tab_pwa")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { viewModel.setTab(3) },
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Architect") },
                            label = { Text("AI Studio", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PolishOnPrimaryContainer,
                                unselectedIconColor = PolishTextSecondary,
                                selectedTextColor = PolishOnPrimaryContainer,
                                unselectedTextColor = PolishTextSecondary,
                                indicatorColor = PolishPrimaryContainer
                            ),
                            modifier = Modifier.testTag("tab_ai")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { viewModel.setTab(4) },
                            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Stats") },
                            label = { Text("Stats", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PolishOnPrimaryContainer,
                                unselectedIconColor = PolishTextSecondary,
                                selectedTextColor = PolishOnPrimaryContainer,
                                unselectedTextColor = PolishTextSecondary,
                                indicatorColor = PolishPrimaryContainer
                            ),
                            modifier = Modifier.testTag("tab_stats")
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
                when (selectedTab) {
                    0 -> ForgeCreatorScreen(viewModel = viewModel)
                    1 -> ArcadeLibraryScreen(viewModel = viewModel)
                    2 -> PwaWebViewScreen()
                    3 -> GeminiStudioScreen(viewModel = viewModel)
                    4 -> StatsScreen(viewModel = viewModel)
                }
            }
        }

        // Fullscreen Play overlay
        AnimatedVisibility(
            visible = isPlayingGame,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                GameEngineCanvas(
                    config = activeConfig,
                    onScoreUpdate = { currentScore ->
                        viewModel.updateHighScore(currentScore)
                    },
                    onGameOver = { finalScore ->
                        viewModel.updateHighScore(finalScore)
                    }
                )

                // Top Exit / Back Button
                IconButton(
                    onClick = { viewModel.exitGame() },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.CircleShape)
                        .testTag("exit_game_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Game", tint = Color.White)
                }
            }
        }
    }
}

