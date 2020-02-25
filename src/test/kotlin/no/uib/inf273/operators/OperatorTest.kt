package no.uib.inf273.operators

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class OperatorTest {

    @Test
    internal fun calculateNumberOfVessels() {
        Assertions.assertEquals(0, Operator.calculateNumberOfVessels(0, 0))
        Assertions.assertEquals(1, Operator.calculateNumberOfVessels(0, 1))
        Assertions.assertEquals(1, Operator.calculateNumberOfVessels(0, 2))
        Assertions.assertEquals(2, Operator.calculateNumberOfVessels(0, 3))
        Assertions.assertEquals(2, Operator.calculateNumberOfVessels(0, 4))
        Assertions.assertEquals(3, Operator.calculateNumberOfVessels(0, 5))
        Assertions.assertEquals(3, Operator.calculateNumberOfVessels(0, 6))
        Assertions.assertEquals(4, Operator.calculateNumberOfVessels(0, 7))
        Assertions.assertEquals(4, Operator.calculateNumberOfVessels(0, 8))
        Assertions.assertEquals(5, Operator.calculateNumberOfVessels(0, 9))
        Assertions.assertEquals(5, Operator.calculateNumberOfVessels(0, 10))
    }

    @Test
    internal fun findNonEmptyVessel() {

    }
}
