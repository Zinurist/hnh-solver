package com.example.hnhsolver

import com.example.gamesolver.GameState
import com.example.gamesolver.GameSolver
import com.example.gamesolver.Move
import java.lang.IllegalArgumentException

const val EMPTY: Byte = 0
const val HARE: Byte = 1
const val HOUND: Byte = 2

val MIRROR_MAP = intArrayOf(0,3,2,1,6,5,4,9,8,7,10)

/*
*    1  4  7
* 0  2  5  8  10
*    3  6  9
*/
val HARE_MOVES = hashMapOf(
        0 to listOf(),
        1 to listOf(0, 2, 4, 5),
        2 to listOf(0, 1, 3, 5),
        3 to listOf(0, 2, 5, 6),
        4 to listOf(1, 5, 7),
        5 to listOf(1, 2, 3, 4, 6, 7, 8, 9),
        6 to listOf(3, 5, 9),
        7 to listOf(4, 5, 8, 10),
        8 to listOf(5, 7, 9, 10),
        9 to listOf(5, 6, 8, 10),
        10 to listOf(7, 8, 9)
)
val HOUND_MOVES = hashMapOf(
        0 to listOf(1, 2, 3),
        1 to listOf(2, 4, 5),
        2 to listOf(1, 3, 5),
        3 to listOf(2, 5, 6),
        4 to listOf(5, 7),
        5 to listOf(4, 6, 7, 8, 9),
        6 to listOf(5, 9),
        7 to listOf(8, 10),
        8 to listOf(7, 9, 10),
        9 to listOf(8, 10),
        10 to listOf()
)

data class HnHState(var board: ByteArray, val player: Byte) : GameState<Pair<Int, Int>>() {
    val hash: Int
    val harePos: Int
    val houndsPos: List<Int>
    override val mirrored: Boolean

    private fun ByteArray.isMirrored(): Boolean {
        for ((index,value) in withIndex()) {
            if (index == 0) continue
            if (value == HOUND && index%3 != 2) return index%3 == 0
        }
        return false
    }

    init {
        mirrored = board.isMirrored()
        if (mirrored) {
            val newBoard = board.copyOf()
            for ((i,v) in board.withIndex()) {
                newBoard[MIRROR_MAP[i]] = v
            }
            board = newBoard
        }

        var harePosTmp = -1
        val houndsList = mutableListOf<Int>()
        hash = board.foldIndexed(player.toInt().shl(4*4)) { pos, acc, board_state ->
            when (board_state) {
                HARE -> {
                    if (harePosTmp != -1) throw IllegalArgumentException("More than 1 hare found!")
                    harePosTmp = pos
                    acc + pos
                }
                HOUND -> {
                    if (houndsList.size >= 3) throw IllegalArgumentException("More than 3 hounds found!")
                    houndsList.add(pos)
                    acc + pos.shl(4*houndsList.size)
                }
                else -> acc
            }
        }

        harePos = harePosTmp
        houndsPos = houndsList
    }

    override fun hashCode(): Int {
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return hashCode() == other.hashCode()
    }

    override fun moves(): List<Move<Pair<Int, Int>>> =
        when (player) {
            HARE -> movesHare()
            HOUND -> movesHound()
            else -> throw IllegalStateException("Unknown player")
        }

    private fun movesHare(): List<Move<Pair<Int, Int>>> {
        if (evaluate() > 0.0) return listOf()
        val moveList = HARE_MOVES[harePos] ?: throw IllegalStateException("Hare position invalid")
        return moveList.filter { board[it] == EMPTY }.map { Move(harePos to it, executeMove(harePos to it)) }
    }

    private fun movesHound(): List<Move<Pair<Int, Int>>> {
        //no moves if hare is in escape position
        if (evaluate() < 0.0) return listOf()
        val moveList = mutableListOf<Pair<Int,Int>>()
        for (hound in houndsPos) {
            val moves = HOUND_MOVES[hound] ?: throw IllegalStateException("Hound position invalid")
            moveList.addAll(moves.map { hound to it } )
        }
        return moveList.filter { board[it.second] == EMPTY }.map { Move(it, executeMove(it)) }
    }

    fun executeMove(move: Pair<Int,Int>): HnHState {
        val newBoard = board.copyOf()
        newBoard[move.first] = EMPTY
        newBoard[move.second] = player
        return HnHState(newBoard, if(player==HARE) HOUND else HARE)
    }

    override fun evaluate(): Double {
        var pos = 0
        var hareWon = false
        var houndFound = false
        search@ while (true) {
            when(board[pos]) {
                HARE -> {
                    hareWon = true
                    break@search
                }
                HOUND -> houndFound = true
            }
            if (pos%3 == 0 && houndFound) break@search
            pos++
            if (pos >= 11) throw IllegalStateException("No hare/hound found")
        }

        return if ((hareWon && player == HARE) || (!hareWon && player == HOUND)) 1.0 else -1.0
    }

    override fun mirrorMove(move: Pair<Int, Int>): Pair<Int, Int> {
        val (p1,p2) = move
        return MIRROR_MAP[p1] to MIRROR_MAP[p2]
    }

    override fun toString(): String {
        var str = if (player == HARE) "Hare to move\n" else "Hound to move\n"
        val charMap = hashMapOf( EMPTY to ".", HARE to "H", HOUND to "D" )
        str += "   "+charMap[board[1]]+"  "+charMap[board[4]]+"  "+charMap[board[7]] +"\n"
        str += charMap[board[0]]+"  "+charMap[board[2]]+"  "+charMap[board[5]]+"  "+charMap[board[8]]+"  "+charMap[board[10]] +"\n"
        str += "   "+charMap[board[3]]+"  "+charMap[board[6]]+"  "+charMap[board[9]] +"\n"
        return str
    }

    override fun evaluateRepeatState(): Double = if (player == HARE) 1.0 else -1.0

}

class HareAndHoundsGame(board: ByteArray = byteArrayOf(HOUND, HOUND, EMPTY, HOUND, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, HARE)) {
    var state: HnHState
    val tree = GameSolver<Pair<Int,Int>>()
    var gameOver = false

    init {
        state = HnHState(board, HOUND)
        tree.addState(state)
        tree.explore()
        tree.solve(state)
    }

    fun makeMove(move: Pair<Int,Int>){
        //state = state.executeMove(if(state.mirrored) state.mirrorMove(move) else move)
        state = state.executeMove(move)
    }

    fun moveCPU(){
        val move = tree.bestMove(state, mirror=false)
        if(move == null) gameOver = true
        else makeMove(move)
        val move2 = tree.bestMove(state, mirror=false)
        if(move2 == null) gameOver = true
    }

    fun over(): Boolean = gameOver

    override fun toString(): String {
        return state.toString()
    }
}


fun interactiveTest() {
    val board = byteArrayOf(HOUND, HOUND, EMPTY, HOUND, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, HARE)
    //val board = byteArrayOf(EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, HOUND, EMPTY, HOUND, HOUND, EMPTY, HARE)
    //val board = byteArrayOf(0, 0, 0, 0, 2, 2, 0, 1, 0, 2, 0)
    val game = HareAndHoundsGame(board)

    println("Do you want play as (H)are or (D)oggo?")
    var playerIsSecond = readLine() == "H"
    //var playerIsSecond = true

    while (true) {
        if(playerIsSecond) {
            println(game)
            if (game.over()) break
            game.moveCPU()
        }
        playerIsSecond = true

        println(game)
        if (game.over()) break

        val playerInput = readLine()
        if (playerInput == null || playerInput == "") break
        val ilist = playerInput.split(" ")
        val movePlayer = (ilist[0].toInt()) to (ilist[1].toInt())
        game.makeMove(movePlayer)
    }

}


