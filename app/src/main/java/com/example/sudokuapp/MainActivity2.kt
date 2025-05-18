//package com.example.sudokuapp
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.viewModels
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import com.example.sudokuapp.ui.theme.SudokuAppTheme
//
//class MainActivity2 : ComponentActivity() {
//    private val gameViewModel: GameViewModel by viewModels()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            SudokuAppTheme {
//                var currentScreen by remember { mutableStateOf("level_select") }
//
//                Surface(modifier = Modifier.fillMaxSize()) {
//                    when (currentScreen) {
//                        "level_select" -> LevelSelectScreen { level ->
//                            gameViewModel.newGame(level)
//                            currentScreen = "game"
//                        }
//                        "game" -> GameScreen(gameViewModel)
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun LevelSelectScreen(onLevelSelected: (String) -> Unit) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("스도쿠 난이도 선택", style = MaterialTheme.typography.headlineSmall)
//        Spacer(Modifier.height(20.dp))
//        listOf("쉬움", "보통", "어려움").forEach { level ->
//            Button(
//                onClick = { onLevelSelected(level) },
//                modifier = Modifier.padding(8.dp)
//            ) {
//                Text(level)
//            }
//        }
//    }
//}
//
//@Composable
//fun GameScreen(viewModel: GameViewModel) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Top,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        SudokuBoard(viewModel)
//        Spacer(modifier = Modifier.height(24.dp))
//        NumberPad { number -> viewModel.inputNumber(number) }
//    }
//}
//
//@Composable
//fun SudokuBoard(viewModel: GameViewModel) {
//    Column {
//        viewModel.board.forEachIndexed { rowIndex, row ->
//            Row {
//                row.forEachIndexed { colIndex, value ->
//                    val isSelected = viewModel.selectedRow == rowIndex && viewModel.selectedCol == colIndex
//                    Button(
//                        onClick = { viewModel.selectCell(rowIndex, colIndex) },
//                        modifier = Modifier
//                            .size(40.dp)
//                            .padding(1.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = if (isSelected) Color.Gray else Color.LightGray
//                        )
//                    ) {
//                        Text(if (value == 0) "" else value.toString())
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun NumberPad(onNumberClick: (Int) -> Unit) {
//    Column {
//        (1..9).chunked(3).forEach { row ->
//            Row {
//                row.forEach { number ->
//                    Button(
//                        onClick = { onNumberClick(number) },
//                        modifier = Modifier
//                            .size(60.dp)
//                            .padding(4.dp)
//                    ) {
//                        Text(number.toString())
//                    }
//                }
//            }
//        }
//    }
//}
