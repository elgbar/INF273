package no.uib.inf273.processor

import no.uib.inf273.Logger
import no.uib.inf273.Main
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SolutionTest {

    companion object {
        init {
            Logger.logLevel = Logger.DEBUG
        }

        private val data: DataParser = DataParser(Main.readInternalFile("Call_7_Vehicle_3.txt")!!)
        private val data2: DataParser = DataParser(Main.readInternalFile("Call_6_Vehicle_2.txt")!!)

        private fun checkSolution(givenData: IntArray, objVal: Int) {
            val sol = Solution(data, givenData)
            assertTrue(sol.isValid(modified = false))
            assertTrue(sol.isFeasible(modified = false, checkValid = false))
            assertEquals(objVal, sol.objectiveValue(modified = false))
        }
    }

    ////////////////
    //  Validity  //
    ////////////////

    @Test
    fun isValid() {
        val givenData = intArrayOf(1, 1, 0, 2, 2, 0, 3, 4, 5, 6, 4, 5, 3, 6)
        assertTrue(Solution(data2, givenData).isValid())
    }

    @Test
    fun isValid_InvalidDiffDeliAndPickup() {
        val givenData = intArrayOf(1, 1, 2, 0, 2, 0, 3, 4, 5, 6, 4, 5, 3, 6)
        assertFalse(Solution(data2, givenData).isValid())
    }

    @Test
    fun isValid_InvalidDiffDeliAndPickup_butEven() {
        val givenData = intArrayOf(1, 1, 2, 3, 0, 2, 3, 0, 4, 5, 6, 4, 5, 6)
        assertFalse(Solution(data2, givenData).isValid())
    }

    @Test
    fun isValid_InvalidDiffDeliAndPickup_butAlsoUsingFreight() {
        val givenData = intArrayOf(1, 1, 2, 2, 0, 2, 0, 4, 5, 6, 4, 5, 6, 3)
        assertFalse(Solution(data2, givenData).isValid())
    }

    /////////////////
    // feasibility //
    /////////////////


    @Test
    fun isFeasible_CheckInfeasiblePickupAfterDelivery() {
        val givenData = intArrayOf(1, 1, 3, 3, 0, 0, 0, 2, 4, 6, 7, 2, 5, 4, 7, 6, 5)
        val sol = Solution(data, givenData)
        assertTrue(sol.isValid(modified = false))
        assertFalse(sol.isFeasible(modified = false, checkValid = false))
    }

    @Test
    fun isFeasible_exampleSolution() {
        val givenData = intArrayOf(3, 3, 0, 7, 1, 7, 1, 0, 5, 5, 0, 2, 2, 4, 4, 6, 6)
        val objVal = 1940470
        checkSolution(givenData, objVal)
    }

    @Test
    fun isFeasible_globalBestSolution() {
        val givenData = intArrayOf(3, 3, 0, 7, 1, 7, 1, 0, 5, 5, 6, 6, 0, 2, 2, 4, 4)
        val objVal = 1476444
        checkSolution(givenData, objVal)
    }

    @Test
    fun isFeasible_sol0() {
        val givenData = intArrayOf(0, 7, 7, 3, 3, 0, 5, 5, 0, 1, 1, 2, 2, 4, 4, 6, 6)
        val objVal = 2478319
        checkSolution(givenData, objVal)
    }

    @Test
    fun isFeasible_sol1() {
        val givenData = intArrayOf(0, 3, 3, 0, 1, 1, 0, 5, 6, 2, 7, 7, 6, 4, 2, 4, 5)
        val objVal = 2672316
        checkSolution(givenData, objVal)
    }

    @Test
    fun isFeasible_sol2() {
        val givenData = intArrayOf(3, 3, 0, 0, 7, 7, 1, 1, 0, 5, 4, 6, 2, 5, 6, 4, 2)
        val objVal = 2346070
        checkSolution(givenData, objVal)
    }

    @Test
    fun isFeasible_sol3() {
        val givenData = intArrayOf(7, 7, 0, 1, 1, 0, 5, 5, 6, 6, 0, 3, 2, 3, 4, 2, 4)
        val objVal = 1617415
        checkSolution(givenData, objVal)
    }

    @Test
    fun isFeasible_sol4() {
        val givenData = intArrayOf(0, 7, 7, 3, 3, 0, 5, 5, 0, 1, 4, 1, 2, 6, 2, 6, 4)
        val objVal = 2478319
        checkSolution(givenData, objVal)
    }

    @Test
    fun isFeasible_sol5() {
        val givenData = intArrayOf(1, 1, 0, 7, 7, 0, 2, 2, 0, 3, 4, 5, 6, 4, 5, 3, 6)
        val objVal = 2166916
        checkSolution(givenData, objVal)
    }

    ///////////////////////
    //  getVesselRanges  //
    ///////////////////////

    @Test
    fun getVesselRanges_sol4() {
        val sol = Solution(data, intArrayOf(0, 7, 7, 3, 3, 0, 5, 5, 0, 1, 4, 1, 2, 6, 2, 6, 4))
        val subArrays = listOf(0 to 0, 1 to 5, 6 to 8, 9 to 17)
        assertEquals(subArrays, sol.getVesselRanges())
    }

    @Test
    fun getVesselRanges_sol5() {
        val sol = Solution(data, intArrayOf(1, 1, 0, 7, 7, 0, 2, 2, 0, 3, 4, 5, 6, 4, 5, 3, 6))
        val subArrays = listOf(0 to 2, 3 to 5, 6 to 8, 9 to 17)
        assertEquals(subArrays, sol.getVesselRanges())
    }

    @Test
    fun getVesselRanges_std() {
        val sol = Solution(data, intArrayOf(0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7))
        val subArrays = listOf(0 to 0, 1 to 1, 2 to 2, 3 to 17)
        assertEquals(subArrays, sol.getVesselRanges())
    }

    ///////////////////////
    //  objective value  //
    ///////////////////////

    @Test
    internal fun objval_std() {
        val gen = SolutionGenerator(data)
        assertEquals(3286422, gen.generateStandardSolution().objectiveValue(modified = false))
    }

    @Test
    internal fun objval_random() {
        val gen = SolutionGenerator(data)

        val arr = IntArray(data.calculateSolutionLength())
        Logger.debug { "length of solution is ${arr.size}" }
        var index = 0

        for (i in 1..data.nrOfVessels) {
            arr[index++] = SolutionGenerator.BARRIER_ELEMENT
        }
        for (i in 1..data.nrOfCargo) {
            arr[index++] = i //pickup
        }
        for (i in 1..data.nrOfCargo) {
            arr[index++] = i //pickup
        }

        val stdObjVal = gen.generateStandardSolution().objectiveValue(modified = false)

        assertEquals(stdObjVal, Solution(data, arr).objectiveValue(false))
    }


    ////////////////////////
    //   getVesselIndex   //
    ////////////////////////

    @Test
    internal fun getVesselIndex_succeedsWhenValid() {
        val sol = Solution(data, intArrayOf(1, 1, 0, 2, 2, 0, 3, 3, 0, 4, 4, 5, 5, 6, 6, 7, 7))
        val ranges = sol.getVesselRanges(false)

        assertEquals(0, sol.getVesselIndex(0, ranges))
        assertEquals(0, sol.getVesselIndex(1, ranges))

        assertEquals(1, sol.getVesselIndex(3, ranges))
        assertEquals(1, sol.getVesselIndex(4, ranges))

        assertEquals(2, sol.getVesselIndex(6, ranges))
        assertEquals(2, sol.getVesselIndex(7, ranges))
        for (i in 9 until sol.arr.size)
            assertEquals(3, sol.getVesselIndex(i, ranges)) { "index $i not vessel 3" }
    }

    @Test
    internal fun getVesselIndex_failsWhenBarrierElement() {
        val sol = SolutionGenerator(data).generateStandardSolution()
        val ranges = sol.getVesselRanges(false)
        assertThrows(IllegalArgumentException::class.java) { sol.getVesselIndex(0, ranges) }
        assertThrows(IllegalArgumentException::class.java) { sol.getVesselIndex(1, ranges) }
        assertThrows(IllegalArgumentException::class.java) { sol.getVesselIndex(2, ranges) }
    }

    /////////////////////
    //   joinToArray   //
    /////////////////////

    @Test
    internal fun joinToArray() {
        val sol = Solution(data, intArrayOf(1, 1, 0, 2, 2, 0, 3, 3, 0, 4, 4, 5, 5, 6, 6, 7, 7))
        val sub = sol.splitToSubArray(false)

        //simulate a two exchange
        sub[0][0] = 2
        sub[1][0] = 1

        val expected = intArrayOf(2, 1, 0, 1, 2, 0, 3, 3, 0, 4, 4, 5, 5, 6, 6, 7, 7)

        sol.joinToArray(sub)

        assertArrayEquals(expected, sol.arr)
    }
}
