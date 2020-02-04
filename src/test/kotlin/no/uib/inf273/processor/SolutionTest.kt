package no.uib.inf273.processor

import no.uib.inf273.Main
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class SolutionTest {


    companion object {

        @JvmStatic
        @BeforeAll
        fun setUp() {
            Main.init(emptyArray())
        }
    }

    private fun assertFeasibility(givenData: IntArray, objVal: Int) {

        val sol = Solution(Main.data, givenData)
        assertTrue(sol.isValid(modified = false))
        assertTrue(sol.isFeasible(modified = false, checkValid = false))
        assertEquals(objVal, sol.objectiveValue(modified = false))
    }

    @Test
    fun isFeasible_CheckInfeasiblePickupAfterDelivery() {
        val givenData = intArrayOf(1, 1, 3, 3, 0, 0, 0, 2, 4, 6, 7, 2, 5, 4, 7, 6, 5)
        val sol = Solution(Main.data, givenData)
        assertTrue(sol.isValid(modified = false))
        assertFalse(sol.isFeasible(modified = false, checkValid = false))
    }

    @Test
    fun isFeasible_exampleSolution() {
        val givenData = intArrayOf(3, 3, 0, 7, 1, 7, 1, 0, 5, 5, 0, 2, 2, 4, 4, 6, 6)
        val objVal = 1940470
        assertFeasibility(givenData, objVal)
    }

    @Test
    fun isFeasible_globalBestSolution() {
        val givenData = intArrayOf(3, 3, 0, 7, 1, 7, 1, 0, 5, 5, 6, 6, 0, 2, 2, 4, 4)
        val objVal = 1476444
        assertFeasibility(givenData, objVal)
    }

    @Test
    fun isFeasible_sol0() {
        val givenData = intArrayOf(0, 7, 7, 3, 3, 0, 5, 5, 0, 1, 1, 2, 2, 4, 4, 6, 6)
        val objVal = 2478319
        assertFeasibility(givenData, objVal)
    }

    @Test
    fun isFeasible_sol1() {
        val givenData = intArrayOf(0, 3, 3, 0, 1, 1, 0, 5, 6, 2, 7, 7, 6, 4, 2, 4, 5)
        val objVal = 2672316
        assertFeasibility(givenData, objVal)
    }

    @Test
    fun isFeasible_sol2() {
        val givenData = intArrayOf(3, 3, 0, 0, 7, 7, 1, 1, 0, 5, 4, 6, 2, 5, 6, 4, 2)
        val objVal = 2346070
        assertFeasibility(givenData, objVal)
    }

    @Test
    fun isFeasible_sol3() {
        val givenData = intArrayOf(7, 7, 0, 1, 1, 0, 5, 5, 6, 6, 0, 3, 2, 3, 4, 2, 4)
        val objVal = 1617415
        assertFeasibility(givenData, objVal)
    }

    @Test
    fun isFeasible_sol4() {
        val givenData = intArrayOf(0, 7, 7, 3, 3, 0, 5, 5, 0, 1, 4, 1, 2, 6, 2, 6, 4)
        val objVal = 2478319
        assertFeasibility(givenData, objVal)
    }

    @Test
    fun isFeasible_sol5() {
        val givenData = intArrayOf(1, 1, 0, 7, 7, 0, 2, 2, 0, 3, 4, 5, 6, 4, 5, 3, 6)
        val objVal = 2166916
        assertFeasibility(givenData, objVal)
    }
}
