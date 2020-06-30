package com.example.gamesolver

import com.example.hnhsolver.EMPTY
import com.example.hnhsolver.HARE
import com.example.hnhsolver.HOUND
import com.example.hnhsolver.HnHState
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.HashMap

data class Move<MoveData> (val move: MoveData, val state: GameState<MoveData>)

abstract class GameState<MoveData> {
    abstract fun moves(): List<Move<MoveData>>
    abstract fun evaluate(): Double
    abstract fun evaluateRepeatState(): Double

    abstract val mirrored: Boolean
    abstract fun mirrorMove(move: MoveData): MoveData
}



fun minMax(list: List<Double>): Pair<Int, Double> {
    val (i,d) =  list.foldIndexed(-1 to Double.MAX_VALUE) {i, acc, d ->
        if (d < acc.second) Pair(i, d) else acc
    }
    return i to -d
}

class GameSolver<MoveData> {
    val nodeValues: HashMap<GameState<MoveData>, Double> = hashMapOf()
    val nodes: HashMap<GameState<MoveData>, List<Move<MoveData>>> = hashMapOf()
    val nodesBackward: HashMap<GameState<MoveData>, MutableList<GameState<MoveData>>> = hashMapOf()
    private val solution: HashMap<GameState<MoveData>, MoveData> = hashMapOf()
    private val leaves: MutableList<GameState<MoveData>> = mutableListOf()
    private val frontier: Stack<GameState<MoveData>> = Stack()

    fun addState(state: GameState<MoveData>) {
        if (state in nodes) return
        frontier.add(state)
    }

    fun explore() {
        while (frontier.isNotEmpty()) {
            val state = frontier.pop()
            val moves = state.moves()
            nodes[state] = moves
            if (moves.isEmpty()){
                leaves.add(state)
                nodeValues[state] = state.evaluate()
            }
            moves.forEach {
                if(it.state in nodes) {
                    leaves.add(it.state)
                } else {
                    frontier.add(it.state)
                }

                if (it.state !in nodesBackward) nodesBackward[it.state] = mutableListOf()
                nodesBackward[it.state]!!.add(state)
            }
        }
    }

    fun solve_tree(strategy: (List<Double>) -> Pair<Int, Double> = ::minMax) {
        val backFrontier: LinkedList< GameState<MoveData> > = LinkedList()
        backFrontier.addAll(leaves)

        while (backFrontier.isNotEmpty()) {
            val state = backFrontier.pollFirst()
            val moves = nodes[state] ?: listOf()
            val values = moves.map { nodeValues[it.state] ?: 0.0 }

            if (values.isEmpty()) {
                backFrontier.addAll(nodesBackward[state] ?: listOf())
            } else {
                val (index, value) = strategy(values)
                if (value != nodeValues[state]) {
                    backFrontier.addAll(nodesBackward[state] ?: listOf())
                    nodeValues[state] = value
                    solution[state] = moves[index].move
                }
            }
        }
    }

    fun solve(state: GameState<MoveData>, strategy: (List<Double>) -> Pair<Int, Double> = ::minMax, visited: MutableSet<GameState<MoveData>> = mutableSetOf()): Pair<Double, MoveData?> {

        if (state in solution) return nodeValues[state]!! to solution[state]

        val moves = nodes[state] ?: listOf()
        if (moves.isEmpty()) return nodeValues[state]!! to null
        if (state in visited) return state.evaluateRepeatState() to null

        visited.add(state)
        val values = moves.map { solve(it.state, strategy=strategy, visited=visited).first }
        visited.remove(state)

        val (index, value) = strategy(values)
        nodeValues[state] = value
        solution[state] = moves[index].move
        return value to moves[index].move
    }

    fun bestMove(state: GameState<MoveData>, mirror: Boolean = true): MoveData? {
        if (state !in nodes){
            addState(state)
            explore()
        }
        if (state !in solution){
            solve(state)
        }

        /*
        println(state.hashCode())
        if((state as HnHState).player == HARE) println("HARE")
        else println("HOUND")
        println("Value = " + nodeValues[state])
        nodes[state]!!.forEach{
            println("Move " + it.move + ", " + nodeValues[it.state])
        }
        */

        val move = solution[state] ?: return null
        return if (state.mirrored && mirror) state.mirrorMove(move) else move
    }
}