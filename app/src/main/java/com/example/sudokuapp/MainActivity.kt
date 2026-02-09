package com.example.sudokuapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.sudokuapp.ui.theme.SudokuAppTheme


class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SudokuAppTheme {
                var currentScreen by remember { mutableStateOf("level_select") }

                LaunchedEffect(Unit) {
                    if (gameViewModel.hasSavedGame()) {
                        if (gameViewModel.loadGame()) {
                            currentScreen = "game"
                        }
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(),color = Color.White) {
                    when (currentScreen) {
                        "level_select" -> LevelSelectScreen(
                            hasSavedGame = gameViewModel.hasSavedGame(),
                            onContinue = {
                                if (gameViewModel.loadGame()) {
                                    currentScreen = "game"
                                }
                            },
                            onLevelSelected = { level ->
                                gameViewModel.newGame(level)
                                currentScreen = "game"
                            },
                            onViewStats = { currentScreen = "stats" },
                            onViewSettings = { currentScreen = "settings" }
                        )
                        "game" -> GameScreen(
                            viewModel = gameViewModel,
                            onGameCompleted = {
                                gameViewModel.resetGame()
                                currentScreen = "level_select"
                            },
                            onBackToMenu = {
                                gameViewModel.saveGame()
                                currentScreen = "level_select"
                            }
                        )
                        "stats" -> StatsScreen(
                            stats = gameViewModel.getStats(),
                            onBack = { currentScreen = "level_select" }
                        )
                        "settings" -> SettingsScreen(
                            onClearSavedGame = { gameViewModel.clearSavedGame() },
                            onResetStats = { gameViewModel.resetStats() },
                            onBack = { currentScreen = "level_select" }
                        )
                    }
                }
            }
        }
    }
}
