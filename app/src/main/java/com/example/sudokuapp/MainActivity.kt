package com.example.sudokuapp
import ads_mobile_sdk.h4
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sudokuapp.ui.theme.SudokuAppTheme
import androidx.compose.material3.AlertDialog


class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SudokuAppTheme {
                var currentScreen by remember { mutableStateOf("level_select") }

                Surface(modifier = Modifier.fillMaxSize(),color = Color.White) {
                    when (currentScreen) {
                        "level_select" -> LevelSelectScreen { level ->
                            gameViewModel.newGame(level)
                            currentScreen = "game"
                        }
                        "game" -> GameScreen(
                            viewModel = gameViewModel,
                            onGameCompleted = {
                                gameViewModel.resetGame()
                                currentScreen = "level_select"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LevelSelectScreen(onLevelSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("스도쿠 난이도 선택", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))
        listOf("쉬움", "보통", "어려움").forEach { level ->
            Button(
                onClick = { onLevelSelected(level) },
                modifier = Modifier.padding(20.dp)
            ) {
                Text(level)
            }
        }
    }
}


@Composable
fun SudokuBoard(viewModel: GameViewModel) {
    Column {
        viewModel.board.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { colIndex, value ->
                    val isSelected =
                        viewModel.selectedRow == rowIndex && viewModel.selectedCol == colIndex
                    val isSameRowOrCol =
                        rowIndex == viewModel.selectedRow || colIndex == viewModel.selectedCol
                    val isCorrect = viewModel.correctness[rowIndex][colIndex]
                    val textColor = if (value == 0) Color.Transparent
                    else if (isCorrect) Color.Black
                    else Color.Red

                    val backgroundColor = when {
                        isSelected -> Color.White           // 선택된 셀은 흰색 (테두리로 강조)
                        isSameRowOrCol -> Color.LightGray   // 같은 행/열은 회색 배경
                        else -> Color.White                 // 나머지는 흰색
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(0.dp)
                            .border(
                                width = if (isSelected) 3.dp else 0.5.dp,
                                color = if (isSelected) Color.Blue else Color.Black
                            )
                            .background(backgroundColor)
                            .clickable { viewModel.selectCell(rowIndex, colIndex) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (value == 0) "" else value.toString(),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel, onGameCompleted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SudokuBoard(viewModel)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "선택한 숫자: ${if (viewModel.selectedNumber == 0) "없음" else viewModel.selectedNumber} ",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        NumberPad(
            onNumberClick = { number ->
                viewModel.selectNumber(number)  // 숫자 선택 먼저
                viewModel.inputNumber()         // 선택된 셀에 입력
            },
            selectedNumber = viewModel.selectedNumber
        )
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
fun NumberPad(onNumberClick: (Int) -> Unit, selectedNumber: Int) {
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
    }
}