package no.uib.inf273.operator

import no.uib.inf273.Logger
import no.uib.inf273.Logger.log
import no.uib.inf273.Main
import no.uib.inf273.Main.Companion.rand
import no.uib.inf273.processor.DataParser
import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class OperatorTest {

    private val size: Int = data.calculateSolutionLength()

    companion object {
        init {
            Logger.logLevel = Logger.DEBUG
        }

        private val data: DataParser = DataParser(Main.readInternalFile("Call_7_Vehicle_3.txt")!!)
    }


    ///////////////////////////
    //  TwoExchangeOperator  //
    ///////////////////////////


    @Test
    internal fun TwoExchangeOperator_SwapsElements() {

        val sol = Solution(data, intArrayOf(3, 3, 0, 7, 1, 7, 1, 0, 5, 5, 6, 6, 0, 2, 2, 4, 4))
        val solCpy = sol.arr.copyOf()

        val firstIndex = 8
        val secondIndex = 10

        //restore the random to initial state
        rand = Random(7)

        Operator.TwoExchangeOperator.operate(sol)

        assertEquals(
            sol.arr[firstIndex],
            solCpy[secondIndex],
            "Element at origin index not equal to original dest value | \narr ${sol.arr.toList()}\norg ${solCpy.toList()}"
        )
        assertEquals(sol.arr[secondIndex], solCpy[firstIndex])
    }

    /////////////////////////////
    //  ThreeExchangeOperator  //
    /////////////////////////////


    @Test
    internal fun ThreeExchangeOperator_SwapsElements() {

        val sol = Solution(data, intArrayOf(0, 7, 1, 7, 1, 3, 3, 0, 5, 5, 6, 6, 2, 2, 0, 4, 4))
        val solCpy = sol.arr.copyOf()

        val firstIndex = 4
        val secondIndex = 3
        val thirdIndex = 6

        //restore the random to initial state
        rand = Random(2)

        Operator.TreeExchangeOperator.operate(sol)

        assertEquals(sol.arr[firstIndex], solCpy[thirdIndex])
        assertEquals(sol.arr[secondIndex], solCpy[firstIndex])
        assertEquals(sol.arr[thirdIndex], solCpy[secondIndex])
    }


    ////////////////////////////
    //  ReinsertOnceOperator  //
    ////////////////////////////


    @Test
    internal fun ReinsertOnceOperator_SwapsElementsDestLssOrg() {

        val sol = SolutionGenerator(data).generateStandardSolution()

        //restore the random to initial state
        rand = Random(0)

        Operator.ReinsertOnceOperator.operate(sol)

        assertArrayEquals(intArrayOf(0, 0, 0, 1, 1, 2, 2, 3, 4, 4, 5, 5, 6, 6, 7, 3, 7), sol.arr)
    }

    @Test
    internal fun ReinsertOnceOperator_SwapsElementsDestGrtOrg() {

        val sol = SolutionGenerator(data).generateStandardSolution()

        //restore the random to initial state
        rand = Random(1)
        Operator.ReinsertOnceOperator.operate(sol)

        assertArrayEquals(intArrayOf(0, 0, 0, 1, 1, 2, 6, 2, 3, 3, 4, 4, 5, 5, 6, 7, 7), sol.arr)
    }

    @Test
    internal fun ReinsertOnceOperator_DoesNotFailWhenZeroSize() {

        val sol = SolutionGenerator(data).generateStandardSolution()

        //Find a random seed that result in 0 at first next Int call
        var s = 0
        do {
            rand = Random(++s)
        } while (rand.nextInt(size) != 0)
        log("zero seed is $s")

        rand = Random(s)
        assertEquals(0, rand.nextInt(size)) { "Failed to make sure the next int is 0" }
        assertEquals(16, s) { "Random seed changed" }
        rand = Random(s)
        Operator.ReinsertOnceOperator.operate(sol)

        assertArrayEquals(intArrayOf(0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 0, 5, 5, 6, 6, 7, 7), sol.arr) {
            sol.arr.contentToString()
        }
    }

    @Test
    internal fun ReinsertOnceOperator_ReinsertLastFirst() {

        val sol = SolutionGenerator(data).generateStandardSolution()

        //Find a random seed that result in 0 at first next Int call
        var s = 0
        do {
            rand = Random(++s)
        } while (rand.nextInt(size) != size - 1 || rand.nextInt(size) != 0)
        log("zero seed is $s")

        rand = Random(s)
        assertEquals(size - 1, rand.nextInt(size)) { "Failed to make sure the next int is 16" }
        assertEquals(0, rand.nextInt(size)) { "Failed to make sure the next int is 0" }
        rand = Random(s)
        Operator.ReinsertOnceOperator.operate(sol)

        val expect = intArrayOf(7, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

    @Test
    internal fun ReinsertOnceOperator_ReinsertFirstLast() {

        val sol = SolutionGenerator(data).generateStandardSolution()

        //Find a random seed that result in 0 at first next Int call
        var s = 0
        do {
            rand = Random(++s)
        } while (rand.nextInt(size) != 0 || rand.nextInt(size) != size - 1)
        log("zero seed is $s")

        rand = Random(s)
        assertEquals(0, rand.nextInt(size)) { "Failed to make sure the next int is 0" }
        assertEquals(size - 1, rand.nextInt(size)) { "Failed to make sure the next int is 16" }
        rand = Random(s)
        Operator.ReinsertOnceOperator.operate(sol)

        val expect = intArrayOf(0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 0)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }
}
