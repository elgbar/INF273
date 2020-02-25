package no.uib.inf273.operators

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OperatorTest {

    @Test
    internal fun calculateNumberOfVessels() {
        assertEquals(0, Operator.calculateNumberOfVessels(0, 0))
        assertEquals(1, Operator.calculateNumberOfVessels(0, 1))
        assertEquals(1, Operator.calculateNumberOfVessels(0, 2))
        assertEquals(2, Operator.calculateNumberOfVessels(0, 3))
        assertEquals(2, Operator.calculateNumberOfVessels(0, 4))
        assertEquals(3, Operator.calculateNumberOfVessels(0, 5))
        assertEquals(3, Operator.calculateNumberOfVessels(0, 6))
        assertEquals(4, Operator.calculateNumberOfVessels(0, 7))
        assertEquals(4, Operator.calculateNumberOfVessels(0, 8))
        assertEquals(5, Operator.calculateNumberOfVessels(0, 9))
        assertEquals(5, Operator.calculateNumberOfVessels(0, 10))
    }


    @Test
    internal fun getMaxTries() {

        fun duplicateIntArray(size: Int): IntArray {
            val arr = IntArray(size * 2)
            for (i in 0 until size) {
                arr[i * 2] = i
                arr[i * 2 + 1] = i
            }
            return arr
        }

        assertEquals(6, Operator.getMaxTries(duplicateIntArray(2)))
        assertEquals(90, Operator.getMaxTries(duplicateIntArray(3)))
        assertEquals(2520, Operator.getMaxTries(duplicateIntArray(4)))
        assertEquals(113_400, Operator.getMaxTries(duplicateIntArray(5)))
        assertEquals(7_484_400, Operator.getMaxTries(duplicateIntArray(6)))
        assertEquals(681_080_400, Operator.getMaxTries(duplicateIntArray(7)))
        assertEquals(Int.MAX_VALUE, Operator.getMaxTries(duplicateIntArray(8)))
        assertEquals(Int.MAX_VALUE, Operator.getMaxTries(duplicateIntArray(9)))
    }
}
