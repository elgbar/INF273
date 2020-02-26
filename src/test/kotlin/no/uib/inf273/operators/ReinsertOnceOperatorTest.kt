package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.processor.DataParser
import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class ReinsertOnceOperatorTest {

    companion object {
        init {
            Main.log.logLevel = Logger.DEBUG
            ReinsertOnceOperator.log.logLevel = Logger.TRACE
        }

        private val data: DataParser = DataParser(Main.readInternalFile("Call_7_Vehicle_3.txt")!!)
    }

    @Test
    internal fun reinsertOnceOperator_MovesCargoesFromOrgToDummy() {

        val sol = Solution(data, intArrayOf(1, 1, 0, 2, 2, 0, 3, 3, 0, 4, 4, 5, 5, 6, 6, 7, 7))
        val sub = sol.splitToSubArray(false)

        var s = 0
        var org: Int
        var dest: Int
        do {
            s++
            Main.rand = Random(s)
            val indices = ReinsertOnceOperator.getVesselIndices(sub)
            org = indices.first
            dest = indices.second
        } while (org != 0 || dest != 3)

        Main.rand = Random(s)
        ReinsertOnceOperator.operate(sol)

        val expect = intArrayOf(0, 2, 2, 0, 3, 3, 0, 4, 4, 5, 5, 6, 6, 7, 7, 1, 1)
        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

    @Test
    internal fun reinsertOnceOperator_MovesCargoesToEmptyVessel() {

        val sol = SolutionGenerator(data).generateStandardSolution()
        val sub = sol.splitToSubArray(false)


        val expect = intArrayOf(1, 1, 0, 0, 0, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7)
        assertTrue(Solution(data, expect).isFeasible(false))

        var s = 0
        var org: Int
        var dest: Int
        var moveVessel: Int
        do {
            s++
            Main.rand = Random(s)
            val indices = ReinsertOnceOperator.getVesselIndices(sub)
            org = indices.first
            dest = indices.second
            moveVessel = sub[org].random(Main.rand)

        } while (org != 3 || dest != 0 || moveVessel != 1)

        Main.rand = Random(s)
        ReinsertOnceOperator.operate(sol)

        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

    @Test
    internal fun reinsertOnceOperator_MoveCargoToNonEmpty() {

        val sol = Solution(data, intArrayOf(1, 1, 0, 0, 2, 2, 0, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7))
        assertTrue(sol.isFeasible(false))
        val sub = sol.splitToSubArray(false)


        val expect = intArrayOf(0, 0, 1, 2, 1, 2, 0, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7)
        assertTrue(Solution(data, expect).isFeasible(false))

        var s = 0
        var org: Int
        var dest: Int
        var moveVessel: Int
        do {
            s++
            Main.rand = Random(s)
            val indices = ReinsertOnceOperator.getVesselIndices(sub)
            org = indices.first
            dest = indices.second
            moveVessel = sub[org].random(Main.rand)

        } while (org != 3 || dest != 0 || moveVessel != 1)

        Main.rand = Random(s)
        ReinsertOnceOperator.operate(sol)

        assertArrayEquals(expect, sol.arr) {
            "expect: \n${expect.contentToString()}\nfound:\n${sol.arr.contentToString()}"
        }
    }

}
