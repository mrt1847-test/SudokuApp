package com.example.sudokuapp
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
import androidx.compose.foundation.layout.fillMaxSize
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

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SudokuAppTheme {
                var currentScreen by remember { mutableStateOf("level_select") }

                Surface(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        "level_select" -> LevelSelectScreen { level ->
                            gameViewModel.newGame(level)
                            currentScreen = "game"
                        }
                        "game" -> GameScreen(gameViewModel)
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
                    val isSelected = viewModel.selectedRow == rowIndex && viewModel.selectedCol == colIndex
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(1.dp)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.Blue else Color.Black
                            )
                            .background(Color.White)
                            .clickable { viewModel.selectCell(rowIndex, colIndex) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (value == 0) "" else value.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel) {
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
            text = "선택한 숫자: ${if (viewModel.selectedNumber == 0) "없음" else viewModel.selectedNumber}",
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
}

@Composable
fun NumberPad(onNumberClick: (Int) -> Unit, selectedNumber: Int) {
    Column {
        (1..9).chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.Center) {
                row.forEach { number ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .padding(4.dp)
                            .background(
                                if (selectedNumber == number) Color.LightGray else Color.White,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onNumberClick(number) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(number.toString(), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}