
// GameViewModel.kt
package com.example.sudokuapp

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel

class GameViewModel(application: Application) : AndroidViewModel(application) {
    var board by mutableStateOf(List(9) { MutableList(9) { 0 } })
        private set
    var isGameCompleted by mutableStateOf(false)
        private set
    var givenCells by mutableStateOf(List(9) { MutableList(9) { false } })
        private set
    var conflictCells by mutableStateOf(List(9) { MutableList(9) { false } })
        private set
    var notes by mutableStateOf(List(9) { List(9) { emptySet<Int>() } })
        private set
    var isNoteMode by mutableStateOf(false)
        private set
    var elapsedSeconds by mutableStateOf(0)
        private set
    var errorCount by mutableStateOf(0)
        private set
    var hintsUsed by mutableStateOf(0)
        private set

    var selectedRow by mutableStateOf(-1)
    var selectedCol by mutableStateOf(-1)
    var solutionBoard by mutableStateOf(List(9) { MutableList(9) { 0 } })
        private set
    var correctness by mutableStateOf(List(9) { MutableList(9) { true } })
    var selectedNumber by mutableStateOf(0)
        private set

    private val prefs = application.getSharedPreferences("sudoku_prefs", 0)
    private var hasRecordedCompletion = false


    fun selectNumber(number: Int) {
        selectedNumber = number
    }

    fun toggleNoteMode() {
        isNoteMode = !isNoteMode
    }

    fun resetGame() {
        selectedRow = -1
        selectedCol = -1
        selectedNumber = 0
        isNoteMode = false
        isGameCompleted = false
        elapsedSeconds = 0
        errorCount = 0
        hintsUsed = 0
        hasRecordedCompletion = false
    }

    fun newGame(level: String) {
        solutionBoard = generateSolvedBoard()
        selectedRow = -1
        selectedCol = -1
        selectedNumber = 0
        isNoteMode = false
        givenCells = List(9) { MutableList(9) { false } }
        conflictCells = List(9) { MutableList(9) { false } }
        isGameCompleted = false
        notes = List(9) { List(9) { emptySet<Int>() } }
        elapsedSeconds = 0
        errorCount = 0
        hintsUsed = 0
        hasRecordedCompletion = false

        // 난이도별 공개 개수 설정
        val targetClues = when (level) {
            "쉬움" -> 40
            "보통" -> 30
            "어려움" -> 20
            else -> 30
        }

        val puzzleBoard = generatePuzzleBoard(solutionBoard, targetClues)
        board = puzzleBoard.map { it.toMutableList() }
        correctness = List(9) { row ->
            MutableList(9) { col ->
                puzzleBoard[row][col] != 0 && puzzleBoard[row][col] == solutionBoard[row][col]
            }
        }
        givenCells = List(9) { row ->
            MutableList(9) { col -> puzzleBoard[row][col] != 0 }
        }
        updateConflicts()
    }
    fun selectCell(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
    }

    fun inputNumber() {
        if (selectedRow !in 0..8 || selectedCol !in 0..8) {
            return
        }
        if (givenCells[selectedRow][selectedCol]) {
            return
        }
        if (isNoteMode) {
            toggleNote(selectedRow, selectedCol, selectedNumber)
            return
        }
        board = board.toMutableList().also { updated ->
            updated[selectedRow] = updated[selectedRow].toMutableList().also {
                it[selectedCol] = selectedNumber
            }
        }

        correctness = correctness.toMutableList().also { updated ->
            updated[selectedRow] = updated[selectedRow].toMutableList().also {
                it[selectedCol] =
                    selectedNumber != 0 && selectedNumber == solutionBoard[selectedRow][selectedCol]
            }
        }
        if (selectedNumber != 0 && selectedNumber != solutionBoard[selectedRow][selectedCol]) {
            errorCount += 1
        }
        updateConflicts()
        isGameCompleted = board.flatten() == solutionBoard.flatten() &&
            conflictCells.all { row -> row.all { !it } }
        if (isGameCompleted) {
            recordCompletion()
        }
    }

    fun clearNumber() {
        selectedNumber = 0
        if (selectedRow in 0..8 && selectedCol in 0..8 && !givenCells[selectedRow][selectedCol]) {
            board = board.toMutableList().also { updated ->
                updated[selectedRow] = updated[selectedRow].toMutableList().also {
                    it[selectedCol] = 0
                }
            }
            correctness = correctness.toMutableList().also { updated ->
                updated[selectedRow] = updated[selectedRow].toMutableList().also {
                    it[selectedCol] = false
                }
            }
            notes = notes.toMutableList().also { rows ->
                rows[selectedRow] = rows[selectedRow].toMutableList().also { cols ->
                    cols[selectedCol] = emptySet()
                }
            }
        }
        updateConflicts()
        isGameCompleted = board.flatten() == solutionBoard.flatten() &&
            conflictCells.all { row -> row.all { !it } }
        if (isGameCompleted) {
            recordCompletion()
        }
    }

    fun applyHint() {
        if (isGameCompleted) {
            return
        }
        val emptyPositions = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == 0 && !givenCells[row][col]) {
                    emptyPositions.add(Pair(row, col))
                }
            }
        }
        if (emptyPositions.isEmpty()) {
            return
        }
        val (row, col) = emptyPositions.random()
        board = board.toMutableList().also { updated ->
            updated[row] = updated[row].toMutableList().also {
                it[col] = solutionBoard[row][col]
            }
        }
        correctness = correctness.toMutableList().also { updated ->
            updated[row] = updated[row].toMutableList().also {
                it[col] = true
            }
        }
        notes = notes.toMutableList().also { rows ->
            rows[row] = rows[row].toMutableList().also { cols ->
                cols[col] = emptySet()
            }
        }
        hintsUsed += 1
        updateConflicts()
        isGameCompleted = board.flatten() == solutionBoard.flatten() &&
            conflictCells.all { r -> r.all { !it } }
        if (isGameCompleted) {
            recordCompletion()
        }
    }

    fun tickTimer() {
        if (!isGameCompleted) {
            elapsedSeconds += 1
        }
    }

    fun getProgress(): Pair<Int, Int> {
        val filled = board.sumOf { row -> row.count { it != 0 } }
        return Pair(filled, 81)
    }

    fun saveGame() {
        prefs.edit()
            .putString("board", serializeBoard(board))
            .putString("solution", serializeBoard(solutionBoard))
            .putString("given", serializeBoolBoard(givenCells))
            .putString("notes", serializeNotes(notes))
            .putInt("elapsedSeconds", elapsedSeconds)
            .putInt("errorCount", errorCount)
            .putInt("hintsUsed", hintsUsed)
            .apply()
    }

    fun loadGame(): Boolean {
        val boardData = prefs.getString("board", null) ?: return false
        val solutionData = prefs.getString("solution", null) ?: return false
        val givenData = prefs.getString("given", null) ?: return false
        val notesData = prefs.getString("notes", null) ?: ""
        board = deserializeBoard(boardData)
        solutionBoard = deserializeBoard(solutionData)
        givenCells = deserializeBoolBoard(givenData)
        notes = deserializeNotes(notesData)
        selectedRow = -1
        selectedCol = -1
        selectedNumber = 0
        isNoteMode = false
        correctness = List(9) { row ->
            MutableList(9) { col ->
                board[row][col] != 0 && board[row][col] == solutionBoard[row][col]
            }
        }
        elapsedSeconds = prefs.getInt("elapsedSeconds", 0)
        errorCount = prefs.getInt("errorCount", 0)
        hintsUsed = prefs.getInt("hintsUsed", 0)
        updateConflicts()
        isGameCompleted = board.flatten() == solutionBoard.flatten() &&
            conflictCells.all { row -> row.all { !it } }
        hasRecordedCompletion = false
        return true
    }

    fun hasSavedGame(): Boolean =
        prefs.contains("board") && prefs.contains("solution") && prefs.contains("given")

    fun clearSavedGame() {
        prefs.edit()
            .remove("board")
            .remove("solution")
            .remove("given")
            .remove("notes")
            .remove("elapsedSeconds")
            .remove("errorCount")
            .remove("hintsUsed")
            .apply()
    }

    fun getStats(): GameStats {
        val completed = prefs.getInt("gamesCompleted", 0)
        val bestTime = prefs.getInt("bestTimeSeconds", 0)
        val totalErrors = prefs.getInt("totalErrors", 0)
        val totalHints = prefs.getInt("totalHints", 0)
        return GameStats(completed, bestTime, totalErrors, totalHints)
    }

    fun resetStats() {
        prefs.edit()
            .putInt("gamesCompleted", 0)
            .putInt("bestTimeSeconds", 0)
            .putInt("totalErrors", 0)
            .putInt("totalHints", 0)
            .apply()
    }

    private fun recordCompletion() {
        if (hasRecordedCompletion) {
            return
        }
        hasRecordedCompletion = true
        val currentCompleted = prefs.getInt("gamesCompleted", 0) + 1
        val bestTime = prefs.getInt("bestTimeSeconds", 0)
        val newBest = if (bestTime == 0 || elapsedSeconds < bestTime) {
            elapsedSeconds
        } else {
            bestTime
        }
        val totalErrors = prefs.getInt("totalErrors", 0) + errorCount
        val totalHints = prefs.getInt("totalHints", 0) + hintsUsed
        prefs.edit()
            .putInt("gamesCompleted", currentCompleted)
            .putInt("bestTimeSeconds", newBest)
            .putInt("totalErrors", totalErrors)
            .putInt("totalHints", totalHints)
            .apply()
    }

    private fun toggleNote(row: Int, col: Int, number: Int) {
        if (number == 0) {
            return
        }
        notes = notes.toMutableList().also { rows ->
            rows[row] = rows[row].toMutableList().also { cols ->
                val current = cols[col].toMutableSet()
                if (current.contains(number)) {
                    current.remove(number)
                } else {
                    current.add(number)
                }
                cols[col] = current.toSet()
            }
        }
    }

    private fun generateSolvedBoard(): List<MutableList<Int>> {
        fun pattern(row: Int, col: Int): Int = (row * 3 + row / 3 + col) % 9
        val baseRows = (0..8).toList()
        val baseCols = (0..8).toList()

        val rowBands = listOf(0, 1, 2).shuffled()
        val rows = rowBands.flatMap { band ->
            baseRows.slice(band * 3 until band * 3 + 3).shuffled()
        }
        val colBands = listOf(0, 1, 2).shuffled()
        val cols = colBands.flatMap { band ->
            baseCols.slice(band * 3 until band * 3 + 3).shuffled()
        }
        val numbers = (1..9).toList().shuffled()

        return rows.map { row ->
            cols.map { col -> numbers[pattern(row, col)] }.toMutableList()
        }
    }

    private fun generatePuzzleBoard(
        solvedBoard: List<MutableList<Int>>,
        targetClues: Int
    ): List<MutableList<Int>> {
        val puzzle = solvedBoard.map { it.toMutableList() }
        val positions = mutableListOf<Pair<Int, Int>>()
        for (row in 0..8) {
            for (col in 0..8) {
                positions.add(Pair(row, col))
            }
        }
        positions.shuffle()

        var cluesLeft = 81
        for ((row, col) in positions) {
            if (cluesLeft <= targetClues) {
                break
            }
            val backup = puzzle[row][col]
            puzzle[row][col] = 0
            if (countSolutions(puzzle, limit = 2) != 1) {
                puzzle[row][col] = backup
            } else {
                cluesLeft -= 1
            }
        }
        return puzzle
    }

    private fun countSolutions(
        board: List<MutableList<Int>>,
        limit: Int
    ): Int {
        var solutionCount = 0
        fun solve(): Boolean {
            var rowIndex = -1
            var colIndex = -1
            var found = false
            loop@ for (row in 0..8) {
                for (col in 0..8) {
                    if (board[row][col] == 0) {
                        rowIndex = row
                        colIndex = col
                        found = true
                        break@loop
                    }
                }
            }
            if (!found) {
                solutionCount += 1
                return solutionCount >= limit
            }
            for (num in 1..9) {
                if (isValidMove(board, rowIndex, colIndex, num)) {
                    board[rowIndex][colIndex] = num
                    if (solve()) {
                        return true
                    }
                    board[rowIndex][colIndex] = 0
                }
            }
            return false
        }
        solve()
        return solutionCount
    }

    private fun isValidMove(
        currentBoard: List<MutableList<Int>>,
        row: Int,
        col: Int,
        number: Int
    ): Boolean {
        for (index in 0..8) {
            if (currentBoard[row][index] == number || currentBoard[index][col] == number) {
                return false
            }
        }
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (currentBoard[r][c] == number) {
                    return false
                }
            }
        }
        return true
    }

    private fun updateConflicts() {
        conflictCells = List(9) { MutableList(9) { false } }
        for (row in 0..8) {
            for (col in 0..8) {
                val value = board[row][col]
                if (value == 0) {
                    continue
                }
                board[row][col] = 0
                val hasConflict = !isValidMove(board, row, col, value)
                board[row][col] = value
                if (hasConflict) {
                    conflictCells = conflictCells.toMutableList().also { updated ->
                        updated[row] = updated[row].toMutableList().also {
                            it[col] = true
                        }
                    }
                }
            }
        }
    }

    private fun serializeBoard(board: List<MutableList<Int>>): String {
        return board.flatten().joinToString(",")
    }

    private fun serializeBoolBoard(board: List<MutableList<Boolean>>): String {
        return board.flatten().joinToString(",") { if (it) "1" else "0" }
    }

    private fun deserializeBoard(data: String): List<MutableList<Int>> {
        val values = data.split(",").map { it.toInt() }
        return List(9) { row ->
            MutableList(9) { col -> values[row * 9 + col] }
        }
    }

    private fun deserializeBoolBoard(data: String): List<MutableList<Boolean>> {
        val values = data.split(",").map { it == "1" }
        return List(9) { row ->
            MutableList(9) { col -> values[row * 9 + col] }
        }
    }

    private fun serializeNotes(notes: List<List<Set<Int>>>): String {
        return notes.flatten().joinToString("|") { set ->
            if (set.isEmpty()) "" else set.sorted().joinToString("")
        }
    }

    private fun deserializeNotes(data: String): List<List<Set<Int>>> {
        if (data.isBlank()) {
            return List(9) { List(9) { emptySet<Int>() } }
        }
        val parts = data.split("|")
        return List(9) { row ->
            List(9) { col ->
                val raw = parts.getOrNull(row * 9 + col).orEmpty()
                raw.map { it.toString().toInt() }.toSet()
            }
        }
    }
}

data class GameStats(
    val gamesCompleted: Int,
    val bestTimeSeconds: Int,
    val totalErrors: Int,
    val totalHints: Int
)
