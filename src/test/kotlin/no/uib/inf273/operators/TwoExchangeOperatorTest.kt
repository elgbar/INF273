package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.processor.DataParser
import no.uib.inf273.processor.Solution
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class TwoExchangeOperatorTest {


    companion object {
        init {
            Main.log.logLevel = Logger.DEBUG
            TwoExchangeOperator.log.logLevel = Logger.TRACE
        }

        private val data: DataParser = DataParser(Main.readInternalFile("Call_7_Vehicle_3.txt")!!)
    }

    @Test
    internal fun twoExchangeOperator_SwapsElements() {

        val sol = Solution(data, intArrayOf(3, 3, 0, 7, 1, 7, 1, 0, 5, 5, 6, 6, 0, 2, 2, 4, 4))
        val solCpy = sol.arr.copyOf()

        val firstIndex = 8
        val secondIndex = 10

        //restore the random to initial state
        Main.rand = Random(7)

        TwoExchangeOperator.operate(sol)

        assertEquals(
            sol.arr[firstIndex],
            solCpy[secondIndex],
            "Element at origin index not equal to original dest value | \narr ${sol.arr.toList()}\norg ${solCpy.toList()}"
        )
        assertEquals(sol.arr[secondIndex], solCpy[firstIndex])
    }

}
