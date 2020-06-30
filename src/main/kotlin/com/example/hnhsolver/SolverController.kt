package com.example.hnhsolver

import com.example.gamesolver.GameSolver
import com.fasterxml.jackson.annotation.JsonCreator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class BoardState @JsonCreator constructor(val board: ByteArray, val player: Byte)

@RestController
class SolverController {

    val tree = GameSolver<Pair<Int,Int>>()
    val logger: Logger = LoggerFactory.getLogger("Solver Controller")

    @RequestMapping("/solver")
    fun solve(@RequestBody boardState: BoardState): Pair<Int,Int> {
        logger.info(boardState.toString())

        return try {
            val state = HnHState(boardState.board, boardState.player)
            logger.info(tree.bestMove(state).toString())
            tree.bestMove(state) ?: -1 to -1
        } catch (e: Exception) {
            logger.error(e.toString())
            //logger.error(boardState.toString())
            -2 to -2
        }
    }

}