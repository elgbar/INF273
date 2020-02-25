package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.processor.DataParser
import no.uib.inf273.processor.Solution
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class TreeExchangeOperatorTest {


    companion object {
        init {
            Main.log.logLevel = Logger.DEBUG
            TreeExchangeOperator.log.logLevel = Logger.TRACE
        }

        private val data: DataParser = DataParser(Main.readInternalFile("Call_7_Vehicle_3.txt")!!)
    }

    @Test
    internal fun threeExchangeOperator_SwapsElements() {

        val sol = Solution(data, intArrayOf(0, 7, 1, 7, 1, 3, 3, 0, 5, 5, 6, 6, 2, 2, 0, 4, 4))
        val solCpy = sol.arr.copyOf()

        val firstIndex = 4
        val secondIndex = 3
        val thirdIndex = 6

        //restore the random to initial state
        Main.rand = Random(2)

        TreeExchangeOperator.operate(sol)

        assertEquals(sol.arr[firstIndex], solCpy[thirdIndex])
        assertEquals(sol.arr[secondIndex], solCpy[firstIndex])
        assertEquals(sol.arr[thirdIndex], solCpy[secondIndex])
    }
}
