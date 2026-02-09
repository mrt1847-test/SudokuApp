package com.example.sudokuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun LevelSelectScreen(
    hasSavedGame: Boolean,
    onContinue: () -> Unit,
    onLevelSelected: (String) -> Unit,
    onViewStats: () -> Unit,
    onViewSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("스도쿠 난이도 선택", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))
        if (hasSavedGame) {
            Button(
                onClick = onContinue,
                modifier = Modifier.padding(12.dp)
            ) {
                Text("이어하기")
            }
        }
        listOf("쉬움", "보통", "어려움").forEach { level ->
            Button(
                onClick = { onLevelSelected(level) },
                modifier = Modifier.padding(20.dp)
            ) {
                Text(level)
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onViewStats, modifier = Modifier.padding(8.dp)) {
            Text("통계 보기")
        }
        Button(onClick = onViewSettings, modifier = Modifier.padding(8.dp)) {
            Text("설정")
        }
    }
}

@Composable
fun SudokuBoard(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // 정사각형 유지
    ) {
        viewModel.board.forEachIndexed { rowIndex, row ->
            Row(modifier = Modifier.weight(1f)) {
                row.forEachIndexed { colIndex, value ->
                    val isSelected =
                        viewModel.selectedRow == rowIndex && viewModel.selectedCol == colIndex
                    val isSameRowOrCol =
                        rowIndex == viewModel.selectedRow || colIndex == viewModel.selectedCol
                    val isCorrect = viewModel.correctness[rowIndex][colIndex]
                    val isGiven = viewModel.givenCells[rowIndex][colIndex]
                    val hasConflict = viewModel.conflictCells[rowIndex][colIndex]
                    val selectedValue = when {
                        viewModel.selectedNumber != 0 -> viewModel.selectedNumber
                        viewModel.selectedRow in 0..8 && viewModel.selectedCol in 0..8 ->
                            viewModel.board[viewModel.selectedRow][viewModel.selectedCol]
                        else -> 0
                    }
                    val isSameNumber = value != 0 && selectedValue != 0 && value == selectedValue

                    val textColor = when {
                        value == 0 -> Color.Transparent
                        isGiven -> Color(0xFF1B5E20)
                        hasConflict -> Color.Red
                        isCorrect -> Color.Black
                        else -> Color.Red
                    }

                    val backgroundColor = when {
                        isSelected -> Color(0xFFE3F2FD)
                        isSameNumber -> Color(0xFFFFF9C4)
                        isSameRowOrCol -> Color(0xFFEFEFEF)
                        else -> Color.White
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(0.dp)
                            .drawBehind {
                                val strokeWidthThin = 1.dp.toPx()
                                val strokeWidthThick = 3.dp.toPx()

                                // 세포 위치에 따라 선 굵기 결정
                                val topLineWidth =
                                    if (rowIndex % 3 == 0) strokeWidthThick else strokeWidthThin
                                val startLineWidth =
                                    if (colIndex % 3 == 0) strokeWidthThick else strokeWidthThin

                                // 각 방향에 선 그리기
                                // 상단
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = topLineWidth
                                )
                                // 왼쪽
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = startLineWidth
                                )
                                // 하단
                                if (rowIndex == 8) {
                                    drawLine(
                                        color = Color.Black,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = strokeWidthThick
                                    )
                                }
                                // 오른쪽
                                if (colIndex == 8) {
                                    drawLine(
                                        color = Color.Black,
                                        start = Offset(size.width, 0f),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = strokeWidthThick
                                    )
                                }
                            }
                            .background(backgroundColor)
                            .clickable { viewModel.selectCell(rowIndex, colIndex) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (value == 0) {
                            val noteText = viewModel.notes[rowIndex][colIndex]
                                .sorted()
                                .joinToString(" ")
                            Text(
                                text = noteText,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall
                            )
                        } else {
                            Text(
                                text = value.toString(),
                                color = textColor,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameCompleted: () -> Unit,
    onBackToMenu: () -> Unit
) {
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            if (!viewModel.isGenerating) {
                viewModel.tickTimer()
            }
        }
    }
    val progress = viewModel.getProgress()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("시간: ${viewModel.elapsedSeconds}s", style = MaterialTheme.typography.titleSmall)
            Text("오류: ${viewModel.errorCount}", style = MaterialTheme.typography.titleSmall)
            Text(
                "진행: ${progress.first}/${progress.second}",
                style = MaterialTheme.typography.titleSmall
            )
        }
        SudokuBoard(viewModel)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "선택한 숫자: ${if (viewModel.selectedNumber == 0) "없음" else viewModel.selectedNumber} ",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        NumberPad(
            onNumberClick = { number ->
                viewModel.selectNumber(number)
                viewModel.inputNumber()
            },
            onClearClick = { viewModel.clearNumber() },
            onHintClick = { viewModel.applyHint() },
            onToggleNote = { viewModel.toggleNoteMode() },
            isNoteMode = viewModel.isNoteMode,
            selectedNumber = viewModel.selectedNumber
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBackToMenu, modifier = Modifier.fillMaxWidth()) {
            Text("메뉴로 돌아가기")
        }
    }
    if (viewModel.isGameCompleted) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(onClick = { onGameCompleted() }) {
                    Text("확인")
                }
            },
            title = { Text("🎉 게임 완료!") },
            text = { Text("모든 정답을 맞췄습니다!\n축하합니다!") }
        )
    }
}

@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onClearClick: () -> Unit,
    onHintClick: () -> Unit,
    onToggleNote: () -> Unit,
    isNoteMode: Boolean,
    selectedNumber: Int
) {
    Column {
        (1..9).chunked(9).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { number ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(4.dp)
                            .background(
                                if (selectedNumber == number) Color.LightGray else Color.White,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onNumberClick(number) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            number.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onClearClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                modifier = Modifier.weight(1f)
            ) {
                Text("지우기", color = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onHintClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5)),
                modifier = Modifier.weight(1f)
            ) {
                Text("힌트", color = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onToggleNote,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isNoteMode) Color(0xFF7E57C2) else Color(0xFFB39DDB)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isNoteMode) "메모 ON" else "메모 OFF", color = Color.White)
            }
        }
    }
}

@Composable
fun StatsScreen(stats: GameStats, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("통계", style = MaterialTheme.typography.headlineSmall)
        Text("완료한 게임: ${stats.gamesCompleted}")
        Text("최고 기록: ${if (stats.bestTimeSeconds == 0) "-" else "${stats.bestTimeSeconds}s"}")
        Text("누적 오류: ${stats.totalErrors}")
        Text("누적 힌트: ${stats.totalHints}")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBack) {
            Text("뒤로가기")
        }
    }
}

@Composable
fun SettingsScreen(onClearSavedGame: () -> Unit, onResetStats: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("설정", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onClearSavedGame) {
            Text("저장된 게임 삭제")
        }
        Button(onClick = onResetStats) {
            Text("통계 초기화")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBack) {
            Text("뒤로가기")
        }
    }
}
