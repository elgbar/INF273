package no.uib.inf273.processor

import no.uib.inf273.Main
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

internal class SolutionGeneratorTest {


    private val data: DataParser = DataParser(Main.readInternalFile("Call_7_Vehicle_3.txt")!!)
    private val data2: DataParser = DataParser(Main.readInternalFile("Call_6_Vehicle_2.txt")!!)

    @Test
    fun generateStandardSolution() {
        val sol = SolutionGenerator(data).generateStandardSolution()
        assertArrayEquals(intArrayOf(0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7), sol.arr)
    }

    @Test
    fun generateStandardSolution2() {
        val sol = SolutionGenerator(data2).generateStandardSolution()
        assertArrayEquals(intArrayOf(0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6), sol.arr)
    }
}
