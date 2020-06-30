package com.example.hnhsolver

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class HnHSolverTests {

    @Test
    fun hashTest() {
        val board1 = byteArrayOf(HOUND, HOUND, HOUND, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, HARE, EMPTY)
        val hash1 = 0b0010_0001_0000_1001 + HARE.toInt().shl(4*4)
        val board1_mirrored = byteArrayOf(HOUND, EMPTY, HOUND, HOUND, EMPTY, EMPTY, EMPTY, HARE, EMPTY, EMPTY, EMPTY)
        val board2 = byteArrayOf(EMPTY, EMPTY, HOUND, EMPTY, EMPTY, HOUND, EMPTY, HOUND, HARE, EMPTY, EMPTY)
        val hash2 = 0b0111_0101_0010_1000 + HARE.toInt().shl(4*4)
        val board3 = byteArrayOf(EMPTY, EMPTY, HOUND, EMPTY, EMPTY, HOUND, EMPTY, EMPTY, HARE, EMPTY, HOUND)
        val hash3 = 0b1010_0101_0010_1000 + HOUND.toInt().shl(4*4)

        Assertions.assertEquals(false, HnHState(board1, HARE).mirrored)
        Assertions.assertEquals(true, HnHState(board1_mirrored, HARE).mirrored)
        Assertions.assertEquals(HnHState(board1, HARE).hashCode(), HnHState(board1_mirrored, HARE).hashCode())

        Assertions.assertEquals(hash1, HnHState(board1, HARE).hashCode())
        Assertions.assertEquals(hash2, HnHState(board2, HARE).hashCode())
        Assertions.assertEquals(hash3, HnHState(board3, HOUND).hashCode())
        Assertions.assertNotEquals(HnHState(board2, HARE).hashCode(), HnHState(board3, HOUND).hashCode())
        Assertions.assertEquals(hash1, HnHState(board1, HARE).hashCode())
    }

}
