package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.processor.DataParser
import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class ReinsertOnceOperatorOldTest {

    companion object {
        init {
            Logger.logLevel = Logger.DEBUG
        }

        private val data: DataParser = DataParser(Main.readInternalFile("Call_7_Vehicle_3.txt")!!)
    }


    //Copy of reinsert once select, must be synced to make sense for the tests
    private fun getIndicesFor(seed: Int, arr: IntArray): Pair<Int, Int> {

        Main.rand = Random(seed)

        var orgIndex: Int
        var destIndex: Int
        do {
            orgIndex = Main.rand.nextInt(arr.size)
            destIndex = Main.rand.nextInt(arr.size)
        } while (orgIndex == destIndex || arr[orgIndex] == SolutionGenerator.BARRIER_ELEMENT || arr[destIndex] == SolutionGenerator.BARRIER_ELEMENT)
        return orgIndex to destIndex
    }

    @Test
    internal fun reinsertOnceOperatorOld_MovesCargoesFromOrgToDest_OrgLssDest() {

        val sol = Solution(data, intArrayOf(1, 1, 0, 2, 2, 0, 3, 3, 0, 4, 4, 5, 5, 6, 6, 7, 7))


        var s = 0
        var org: Int
        var dest: Int
        do {
            val indices = getIndicesFor(++s, sol.arr)
            org = indices.first
            dest = indices.second
        } while (org != 0 || dest != 3)

        Logger.log("Wanted seed is $s")

        Main.rand = Random(s)
        ReinsertOnceOperatorOld.operate(sol)

        val expect = intArrayOf(0, 2, 2, 1, 1, 0, 3, 3, 0, 4, 4, 5, 5, 6, 6, 7, 7)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

    @Test
    internal fun reinsertOnceOperatorOld_MovesCargoesFromOrgToDest_OrgGrtDest() {

        val sol = Solution(data, intArrayOf(1, 1, 0, 2, 2, 0, 3, 3, 0, 4, 4, 5, 5, 6, 6, 7, 7))

        var s = 0
        var org: Int
        var dest: Int
        do {
            val indices = getIndicesFor(++s, sol.arr)
            org = indices.first
            dest = indices.second
        } while (org != 3 || dest != 1)

        Logger.log("Wanted seed is $s")

        Main.rand = Random(s)
        ReinsertOnceOperatorOld.operate(sol)

        val expect = intArrayOf(1, 1, 2, 2, 0, 0, 3, 3, 0, 4, 4, 5, 5, 6, 6, 7, 7)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

    @Test
    internal fun reinsertOnceOperatorOld_ReshufflesWhenDiffCargoesWithinVessel_OrgGrtDest() {

        val sol = Solution(data, intArrayOf(0, 2, 1, 1, 3, 2, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7))


        var s = 0
        var org: Int
        var dest: Int
        do {
            val indices = getIndicesFor(++s, sol.arr)
            org = indices.first
            dest = indices.second
        } while (org != 5 || dest != 2)

        Logger.log("Wanted seed is $s")

        Main.rand = Random(s)
        ReinsertOnceOperatorOld.operate(sol)

        val expect = intArrayOf(0, 2, 2, 1, 1, 3, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

    @Test
    internal fun reinsertOnceOperatorOld_ReshufflesWhenDiffCargoesWithinVessel_OrgLssDest() {

        val sol = Solution(data, intArrayOf(0, 2, 1, 1, 3, 2, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7))

        var s = 0
        var org: Int
        var dest: Int
        do {
            val indices = getIndicesFor(++s, sol.arr)
            org = indices.first
            dest = indices.second
        } while (org != 2 || dest != 5)

        Logger.log("Wanted seed is $s")

        Main.rand = Random(s)
        ReinsertOnceOperatorOld.operate(sol)

        val expect = intArrayOf(0, 2, 1, 3, 2, 1, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

    @Test
    internal fun reinsertOnceOperatorOld_ReshufflesSameCargoWithinVessel() {

        val sol = Solution(data, intArrayOf(0, 2, 1, 1, 3, 2, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7))

        var s = 0
        var org: Int
        var dest: Int
        do {
            val indices = getIndicesFor(++s, sol.arr)
            org = indices.first
            dest = indices.second
        } while (org != 1 || dest != 5)

        Logger.log("Wanted seed is $s")

        Main.rand = Random(s)
        ReinsertOnceOperatorOld.operate(sol)

        val expect = intArrayOf(0, 1, 1, 3, 2, 2, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

    @Test
    internal fun reinsertOnceOperatorOld_NoReshuffleWhenInFreightArray() {

        val sol = Solution(data, intArrayOf(1, 1, 0, 2, 3, 2, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7))


        var s = 0
        var org: Int
        var dest: Int
        do {
            val indices = getIndicesFor(++s, sol.arr)
            org = indices.first
            dest = indices.second
        } while (org != sol.arr.size - 1 || dest != sol.arr.size - 3)

        Logger.log("Wanted seed is $s")

        Main.rand = Random(s)
        ReinsertOnceOperatorOld.operate(sol)

        val expect = intArrayOf(1, 1, 0, 2, 3, 2, 3, 0, 0, 4, 4, 5, 5, 6, 6, 7, 7)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }
}
