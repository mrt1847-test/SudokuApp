
// GameViewModel.kt
package com.example.sudokuapp

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    var board by mutableStateOf(List(9) { MutableList(9) { 0 } })
        private set

    var selectedRow by mutableStateOf(-1)
    var selectedCol by mutableStateOf(-1)

    fun newGame(level: String) {
        board = List(9) { MutableList(9) { 0 } } // 난이도에 따라 변경 가능
        selectedRow = -1
        selectedCol = -1
    }

    fun selectCell(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
    }

    fun inputNumber(number: Int) {
        if (selectedRow in 0..8 && selectedCol in 0..8) {
            board[selectedRow][selectedCol] = number
        }
    }
}