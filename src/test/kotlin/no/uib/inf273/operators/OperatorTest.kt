package no.uib.inf273.operators

import no.uib.inf273.Main
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class OperatorTest {

    init {
        Main.rand = Random(0)
    }

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
    fun selectRandomVessel_invalidIsInvalid() {
        val retVessel = Operator.selectRandomVessel(arrayOf(IntArray(0), IntArray(2) { 1 }), 1, false)
        assertEquals(Operator.INVALID_VESSEL, retVessel)
    }

    @Test
    fun selectRandomVessel_selectFreightWhenAllowed() {
        val retVessel = Operator.selectRandomVessel(arrayOf(IntArray(0), IntArray(2) { 1 }), 1, true)
        assertEquals(1, retVessel)
    }

    @Test
    fun selectRandomVessel_neverSelectFreightWhenDisallowed() {
        val retVessel = Operator.selectRandomVessel(arrayOf(IntArray(2) { 1 }, IntArray(2) { 1 }), 1, false)
        assertEquals(0, retVessel)
    }

    @Test
    fun selectRandomVessel_OnlySelectAboveOrEqualToMinCargo() {
        val retVessel =
            Operator.selectRandomVessel(arrayOf(IntArray(4) { 0 }, IntArray(2) { 0 }, IntArray(8) { 1 }), 2, false)
        assertEquals(0, retVessel)
    }
}
