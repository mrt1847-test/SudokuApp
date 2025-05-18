
// GameViewModel.kt
package com.example.sudokuapp

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    var board by mutableStateOf(List(9) { MutableList(9) { 0 } })
        private set

    var selectedRow by mutableStateOf(-1)
    var selectedCol by mutableStateOf(-1)

    var selectedNumber by mutableStateOf(0)
        private set

    fun selectNumber(number: Int) {
        selectedNumber = number
        inputNumber()
    }

    fun newGame(level: String) {
        board = List(9) { MutableList(9) { 0 } } // 난이도에 따라 변경 가능
        selectedRow = -1
        selectedCol = -1
    }

    fun selectCell(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
    }

    fun inputNumber() {
        if (selectedRow in 0..8 && selectedCol in 0..8 && selectedNumber != 0) {
            board = board.toMutableList().also { updated ->
                updated[selectedRow] = updated[selectedRow].toMutableList().also {
                    it[selectedCol] = selectedNumber
                }
            }
        }
    }
}