package no.uib.inf273.search

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.operators.MinimizeNotTransported
import no.uib.inf273.processor.DataParser
import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class A5TabooTest {


    companion object {
        init {
            Main.log.logLevel = Logger.DEBUG
            Solution.log.logLevel = Logger.DEBUG
            A5.log.logLevel = Logger.TRACE
        }

        private val data: DataParser = DataParser(Main.readInternalFile("Call_7_Vehicle_3.txt")!!)
        private val solgen: SolutionGenerator = SolutionGenerator(data)
    }

    @Test
    fun tabooSizeRangeIsFollowed_VeryFewIterations() {
        val taboo = A5.Taboo(0)
        assertEquals(1, taboo.minTabooSize)
        assertEquals(2, taboo.maxTabooSize)
    }

    @Test
    fun tabooSizeRangeIsFollowed_10kIterations() {
        val taboo = A5.Taboo(10_000)
        assertEquals(10, taboo.minTabooSize)
        assertEquals(100, taboo.maxTabooSize)
    }

    @Test
    fun tabooSizeFollowedWhenPushing() {
        val sol1 = solgen.generateStandardSolution()
        val sol2 = solgen.generateStandardSolution()
        MinimizeNotTransported.operate(sol2)


        assertFalse(sol1.arr.contentEquals(sol2.arr))
        assertTrue(sol1.isFeasible(true))
        assertTrue(sol2.isFeasible(true))

        val taboo = A5.Taboo(0)
        assertTrue(taboo.currentMaxTabooSize > 1)
        taboo.reduceSize()
        assertTrue(taboo.currentMaxTabooSize == 1)

        //at first taboo does not contain either
        assertFalse(taboo.checkTaboo(sol1))
        assertFalse(taboo.checkTaboo(sol2))

        //after the first push it contains sol1
        taboo.push(sol1)
        assertTrue(taboo.checkTaboo(sol1))
        assertFalse(taboo.checkTaboo(sol2))

        //When pushing the second time the first is removed and the second is still there
        taboo.push(sol2)
        assertFalse(taboo.checkTaboo(sol1))
        assertTrue(taboo.checkTaboo(sol2))
    }

    @Test
    fun reduceSizeIsAlwaysAboveMin() {
        val taboo = A5.Taboo(0)
        assertTrue(taboo.currentMaxTabooSize == 2)
        taboo.reduceSize()
        assertTrue(taboo.currentMaxTabooSize == 1)
        taboo.reduceSize()
        assertTrue(taboo.currentMaxTabooSize == 1)
    }

    @Test
    fun reduceSizeRemoveFirstInserted() {
        val sol1 = solgen.generateStandardSolution()
        val sol2 = solgen.generateStandardSolution()
        MinimizeNotTransported.operate(sol2)


        assertFalse(sol1.arr.contentEquals(sol2.arr))
        assertTrue(sol1.isFeasible(true))
        assertTrue(sol2.isFeasible(true))

        val taboo = A5.Taboo(0)
        assertTrue(taboo.currentMaxTabooSize > 1)

        //at first taboo does not contain either
        assertFalse(taboo.checkTaboo(sol1))
        assertFalse(taboo.checkTaboo(sol2))

        //after the first push it contains both
        taboo.push(sol1)
        taboo.push(sol2)
        assertTrue(taboo.checkTaboo(sol1))
        assertTrue(taboo.checkTaboo(sol2))

        //When reducing size the oldest is removed
        taboo.reduceSize()
        assertFalse(taboo.checkTaboo(sol1))
        assertTrue(taboo.checkTaboo(sol2))
    }
}
