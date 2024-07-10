package kabre.m2.gameofgale

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kabre.m2.gameofgale.databinding.ActivityMainBinding
import java.util.Stack

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val boardSize = 7  // Taille de la grille pour Shannon Switching Game
    private val board = Array(boardSize) { Array(boardSize) { "" } }
    private var currentPlayer = "X"
    private lateinit var adapter: ShannonAdapter
    private var winningPath = mutableListOf<Pair<Int, Int>>()
    private var playerXScore = 0
    private var playerOScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding.recyclerView.layoutManager = GridLayoutManager(this, boardSize)
        adapter = ShannonAdapter(board, this, this::onItemClicked)
        binding.recyclerView.adapter = adapter

        binding.resetBt.setOnClickListener {
            resetBoard()
        }

        binding.rejouerBt.setOnClickListener {
            resetBoard()
        }

        initializeUI()
        resetBoard()
    }

    private fun initializeUI() {
        binding.joueurXScore.text = playerXScore.toString()
        binding.joueurOScore.text = playerOScore.toString()
        binding.statusTextView.text = "Le tour du joueur $currentPlayer"
        val bg = adapter.getBackgroundResource(currentPlayer)
        binding.statusTextView.setBackgroundResource(bg)
    }

    private fun onItemClicked(clickedPosition: Int) {
        val row = clickedPosition / boardSize
        val col = clickedPosition % boardSize
        if (board[row][col].isEmpty() && winningPath.isEmpty()) {
            board[row][col] = currentPlayer
            if (checkWinner(row, col)) {
                binding.statusTextView.text = "Le joueur $currentPlayer gagne !"
                updateScore()
                adapter.updateBoard(winningPath)
                Handler(Looper.getMainLooper()).postDelayed({
                    resetBoard()
                }, 5000)
            } else if (isBoardFull()) {
                binding.statusTextView.text = "Match nul !"
                Handler(Looper.getMainLooper()).postDelayed({
                    resetBoard()
                }, 5000)
            } else {
                currentPlayer = if (currentPlayer == "X") "O" else "X"
                binding.statusTextView.text = "Le tour du joueur $currentPlayer"
                val bg = adapter.getBackgroundResource(currentPlayer)
                binding.statusTextView.setBackgroundResource(bg)
            }
            adapter.updateBoard(winningPath)
        } else if (winningPath.isEmpty()) {
            binding.statusTextView.text = "Cellule déjà occupée !"
        }
    }

    private fun updateScore() {
        if (currentPlayer == "X") {
            playerXScore++
            binding.joueurXScore.text = playerXScore.toString()
        } else {
            playerOScore++
            binding.joueurOScore.text = playerOScore.toString()
        }
    }

    private fun resetBoard() {
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                board[i][j] = ""
            }
        }
        winningPath.clear()  // Vider la liste des chemins de victoire
        currentPlayer = "X"
        adapter.updateBoard(winningPath)
        binding.statusTextView.text = "Le tour du joueur $currentPlayer"
        val bg = adapter.getBackgroundResource(currentPlayer)
        binding.statusTextView.setBackgroundResource(bg)
    }

    private fun checkWinner(row: Int, col: Int): Boolean {
        return if (currentPlayer == "X") {
            hasPathForPlayerX()
        } else {
            hasPathForPlayerO()
        }
    }

    private fun hasPathForPlayerX(): Boolean {
        val visited = Array(boardSize) { BooleanArray(boardSize) }
        val stack = Stack<Pair<Int, Int>>()

        for (i in 0 until boardSize) {
            if (board[i][0] == "X") {
                stack.push(Pair(i, 0))
                visited[i][0] = true
            }
        }

        while (stack.isNotEmpty()) {
            val (r, c) = stack.pop()
            if (c == boardSize - 1) {
                updateWinningPathForX(r, c, visited)
                return true
            }
            getNeighbors(r, c).forEach { (nr, nc) ->
                if (board[nr][nc] == "X" && !visited[nr][nc]) {
                    stack.push(Pair(nr, nc))
                    visited[nr][nc] = true
                }
            }
        }
        return false
    }

    private fun hasPathForPlayerO(): Boolean {
        val visited = Array(boardSize) { BooleanArray(boardSize) }
        val stack = Stack<Pair<Int, Int>>()

        for (j in 0 until boardSize) {
            if (board[0][j] == "O") {
                stack.push(Pair(0, j))
                visited[0][j] = true
            }
        }

        while (stack.isNotEmpty()) {
            val (r, c) = stack.pop()
            if (r == boardSize - 1) {
                updateWinningPathForO(r, c, visited)
                return true
            }
            getNeighbors(r, c).forEach { (nr, nc) ->
                if (board[nr][nc] == "O" && !visited[nr][nc]) {
                    stack.push(Pair(nr, nc))
                    visited[nr][nc] = true
                }
            }
        }
        return false
    }

    private fun updateWinningPathForX(row: Int, col: Int, visited: Array<BooleanArray>) {
        winningPath.clear()
        tracePath(row, col, visited, "X")
        Log.d("verification", "winningPath $winningPath")
    }

    private fun updateWinningPathForO(row: Int, col: Int, visited: Array<BooleanArray>) {
        winningPath.clear()
        tracePath(row, col, visited, "O")
        Log.d("verification", "winningPath $winningPath")
    }

    private fun tracePath(row: Int, col: Int, visited: Array<BooleanArray>, player: String) {
        val stack = Stack<Pair<Int, Int>>()
        stack.push(Pair(row, col))

        while (stack.isNotEmpty()) {
            val (r, c) = stack.pop()
            if (visited[r][c] && board[r][c] == player) {
                winningPath.add(Pair(r, c))
                visited[r][c] = false // Marquer comme visité pour éviter de revisiter

                getNeighbors(r, c).forEach { (nr, nc) ->
                    if (board[nr][nc] == player && visited[nr][nc]) {
                        stack.push(Pair(nr, nc))
                    }
                }
            }
        }
    }

    private fun getNeighbors(row: Int, col: Int): List<Pair<Int, Int>> {
        val directions = listOf(
            Pair(-1, 0), Pair(1, 0),  // Haut, Bas
            Pair(0, -1), Pair(0, 1),  // Gauche, Droite
            Pair(-1, -1), Pair(1, 1), // Diagonale Gauche-Haut, Diagonale Droite-Bas
            Pair(-1, 1), Pair(1, -1)  // Diagonale Droite-Haut, Diagonale Gauche-Bas
        )
        return directions.map { (dr, dc) -> Pair(row + dr, col + dc) }
            .filter { (r, c) -> r in 0 until boardSize && c in 0 until boardSize }
    }

    private fun isBoardFull(): Boolean {
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                if (board[i][j].isEmpty()) {
                    return false
                }
            }
        }
        return true
    }
}
