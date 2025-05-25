
// GameViewModel.kt
package com.example.sudokuapp

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    var board by mutableStateOf(List(9) { MutableList(9) { 0 } })
        private set

    var selectedRow by mutableStateOf(-1)
    var selectedCol by mutableStateOf(-1)
    var solutionBoard by mutableStateOf(List(9) { MutableList(9) { 0 } })
        private set
    var correctness by mutableStateOf(List(9) { MutableList(9) { true } })
    var selectedNumber by mutableStateOf(0)
        private set


    fun selectNumber(number: Int) {
        selectedNumber = number
        inputNumber()
    }

    fun newGame(level: String) {
        // 초기화 예시 (빈 보드로 시작)
        board = List(9) { MutableList(9) { 0 } }

        // 예시 정답 (임시)
        solutionBoard = listOf(
            mutableListOf(5,3,4,6,7,8,9,1,2),
            mutableListOf(6,7,2,1,9,5,3,4,8),
            mutableListOf(1,9,8,3,4,2,5,6,7),
            mutableListOf(8,5,9,7,6,1,4,2,3),
            mutableListOf(4,2,6,8,5,3,7,9,1),
            mutableListOf(7,1,3,9,2,4,8,5,6),
            mutableListOf(9,6,1,5,3,7,2,8,4),
            mutableListOf(2,8,7,4,1,9,6,3,5),
            mutableListOf(3,4,5,2,8,6,1,7,9)
        )

        // 빈 보드 생성
        board = List(9) { MutableList(9) { 0 } }
        correctness = List(9) { MutableList(9) { true } }
        selectedRow = -1
        selectedCol = -1
        selectedNumber = 0

        // 난이도별 공개 개수 설정
        val revealCount = when (level) {
            "쉬움" -> 40
            "보통" -> 30
            "어려움" -> 20
            else -> 30
        }

        // 공개할 셀을 랜덤으로 선택
        val positions = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                positions.add(Pair(row, col))
            }
        }
        positions.shuffle()

        // 정답 중 일부를 보드에 복사
        positions.take(revealCount).forEach { (row, col) ->
            board = board.toMutableList().also { updated ->
                updated[row] = updated[row].toMutableList().also {
                    it[col] = solutionBoard[row][col]
                }
            }
        }
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

            // 정답 여부 저장
            correctness = correctness.toMutableList().also { updated ->
                updated[selectedRow] = updated[selectedRow].toMutableList().also {
                    it[selectedCol] = selectedNumber == solutionBoard[selectedRow][selectedCol]
                }
            }
        }
    }
}