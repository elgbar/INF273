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
}
