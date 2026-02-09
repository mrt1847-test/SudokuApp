package com.example.sudokuapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.sudokuapp.ui.theme.SudokuAppTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SudokuAppTheme {
                var currentScreen by remember { mutableStateOf("level_select") }
                var showContinueDialog by remember { mutableStateOf(false) }
                var showLoadErrorDialog by remember { mutableStateOf(false) }
                var loadErrorMessage by remember { mutableStateOf("") }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    if (gameViewModel.hasSavedGame()) {
                        showContinueDialog = true
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(),color = Color.White) {
                    when (currentScreen) {
                        "level_select" -> LevelSelectScreen(
                            hasSavedGame = gameViewModel.hasSavedGame(),
                            onContinue = {
                                if (gameViewModel.loadGame()) {
                                    currentScreen = "game"
                                } else {
                                    loadErrorMessage = gameViewModel.loadErrorMessage
                                        ?: "저장된 게임을 불러오지 못했습니다."
                                    showLoadErrorDialog = true
                                }
                            },
                            onLevelSelected = { level ->
                                coroutineScope.launch {
                                    if (gameViewModel.newGameAsync(level)) {
                                        currentScreen = "game"
                                    } else {
                                        loadErrorMessage = gameViewModel.loadErrorMessage
                                            ?: "퍼즐 생성에 실패했습니다."
                                        showLoadErrorDialog = true
                                    }
                                }
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
                    if (gameViewModel.isGenerating) {
                        AlertDialog(
                            onDismissRequest = {},
                            confirmButton = {},
                            title = { Text("퍼즐 생성 중") },
                            text = { CircularProgressIndicator() }
                        )
                    }
                }
                if (showContinueDialog) {
                    AlertDialog(
                        onDismissRequest = { showContinueDialog = false },
                        confirmButton = {
                            Button(onClick = {
                                showContinueDialog = false
                                if (gameViewModel.loadGame()) {
                                    currentScreen = "game"
                                } else {
                                    loadErrorMessage = gameViewModel.loadErrorMessage
                                        ?: "저장된 게임을 불러오지 못했습니다."
                                    showLoadErrorDialog = true
                                }
                            }) {
                                Text("이어하기")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showContinueDialog = false }) {
                                Text("새 게임")
                            }
                        },
                        title = { Text("저장된 게임이 있습니다") },
                        text = { Text("이어하기를 선택하면 마지막 게임을 불러옵니다.") }
                    )
                }
                if (showLoadErrorDialog) {
                    AlertDialog(
                        onDismissRequest = { showLoadErrorDialog = false },
                        confirmButton = {
                            Button(onClick = {
                                gameViewModel.clearSavedGame()
                                showLoadErrorDialog = false
                            }) {
                                Text("저장 데이터 삭제")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showLoadErrorDialog = false }) {
                                Text("닫기")
                            }
                        },
                        title = { Text("불러오기 실패") },
                        text = { Text(loadErrorMessage) }
                    )
                }
            }
        }
    }
}
