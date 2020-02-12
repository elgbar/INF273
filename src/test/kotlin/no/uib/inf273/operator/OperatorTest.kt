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


    /////////////////////////////
    // ReinsertOnceOperatorOld //
    /////////////////////////////

    //Copy of reinsert once select, must be synced to make sense for the tests
    private fun getIndicesFor(seed: Int, arr: IntArray): Pair<Int, Int> {

        rand = Random(seed)

        var orgIndex: Int
        var destIndex: Int
        do {
            orgIndex = rand.nextInt(arr.size)
            destIndex = rand.nextInt(arr.size)
        } while (orgIndex == destIndex || arr[orgIndex] == SolutionGenerator.BARRIER_ELEMENT || arr[destIndex] == SolutionGenerator.BARRIER_ELEMENT)
        return orgIndex to destIndex
    }

    @Test
    internal fun ReinsertOnceOperatorOld_MovesCargoesFromOrgToDest_OrgLssDest() {

        val sol = Solution(data, intArrayOf(1, 1, 0, 2, 2, 0, 3, 3, 0, 4, 4, 5, 5, 6, 6, 7, 7))


        var s = 0
        var org: Int
        var dest: Int
        do {
            val indices = getIndicesFor(++s, sol.arr)
            org = indices.first
            dest = indices.second
        } while (org != 0 || dest != 3)

        log("Wanted seed is $s")

        rand = Random(s)
        Operator.ReinsertOnceOperatorOld.operate(sol)

        val expect = intArrayOf(0, 2, 2, 1, 1, 0, 3, 3, 0, 4, 4, 5, 5, 6, 6, 7, 7)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

    @Test
    internal fun ReinsertOnceOperatorOld_MovesCargoesFromOrgToDest_OrgGrtDest() {

        val sol = Solution(data, intArrayOf(1, 1, 0, 2, 2, 0, 3, 3, 0, 4, 4, 5, 5, 6, 6, 7, 7))

        var s = 0
        var org: Int
        var dest: Int
        do {
            val indices = getIndicesFor(++s, sol.arr)
            org = indices.first
            dest = indices.second
        } while (org != 3 || dest != 1)

        log("Wanted seed is $s")

        rand = Random(s)
        Operator.ReinsertOnceOperatorOld.operate(sol)

        val expect = intArrayOf(1, 1, 2, 2, 0, 0, 3, 3, 0, 4, 4, 5, 5, 6, 6, 7, 7)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

    @Test
    internal fun ReinsertOnceOperatorOld_ReshufflesWhenDiffCargoesWithinVessel_OrgGrtDest() {

        val sol = Solution(data, intArrayOf(0, 2, 1, 1, 3, 2, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7))


        var s = 0
        var org: Int
        var dest: Int
        do {
            val indices = getIndicesFor(++s, sol.arr)
            org = indices.first
            dest = indices.second
        } while (org != 5 || dest != 2)

        log("Wanted seed is $s")

        rand = Random(s)
        Operator.ReinsertOnceOperatorOld.operate(sol)

        val expect = intArrayOf(0, 2, 2, 1, 1, 3, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

    @Test
    internal fun ReinsertOnceOperatorOld_ReshufflesWhenDiffCargoesWithinVessel_OrgLssDest() {

        val sol = Solution(data, intArrayOf(0, 2, 1, 1, 3, 2, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7))

        var s = 0
        var org: Int
        var dest: Int
        do {
            val indices = getIndicesFor(++s, sol.arr)
            org = indices.first
            dest = indices.second
        } while (org != 2 || dest != 5)

        log("Wanted seed is $s")

        rand = Random(s)
        Operator.ReinsertOnceOperatorOld.operate(sol)

        val expect = intArrayOf(0, 2, 1, 3, 2, 1, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

    @Test
    internal fun ReinsertOnceOperatorOld_ReshufflesSameCargoWithinVessel() {

        val sol = Solution(data, intArrayOf(0, 2, 1, 1, 3, 2, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7))

        var s = 0
        var org: Int
        var dest: Int
        do {
            val indices = getIndicesFor(++s, sol.arr)
            org = indices.first
            dest = indices.second
        } while (org != 1 || dest != 5)

        log("Wanted seed is $s")

        rand = Random(s)
        Operator.ReinsertOnceOperatorOld.operate(sol)

        val expect = intArrayOf(0, 1, 1, 3, 2, 2, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

    @Test
    internal fun ReinsertOnceOperatorOld_NoReshuffleWhenInFreightArray() {

        val sol = Solution(data, intArrayOf(1, 1, 0, 2, 3, 2, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7))


        var s = 0
        var org: Int
        var dest: Int
        do {
            val indices = getIndicesFor(++s, sol.arr)
            org = indices.first
            dest = indices.second
        } while (org != sol.arr.size - 1 || dest != sol.arr.size - 3)

        log("Wanted seed is $s")

        rand = Random(s)
        Operator.ReinsertOnceOperatorOld.operate(sol)

        val expect = intArrayOf(1, 1, 0, 2, 3, 2, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }
}
