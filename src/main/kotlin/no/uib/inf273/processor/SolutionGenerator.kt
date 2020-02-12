package no.uib.inf273.processor;

import no.uib.inf273.Logger.debug
import no.uib.inf273.Main
import no.uib.inf273.extra.randomizeWithin

class SolutionGenerator(val data: DataParser) {

    companion object {

        /**
         * The element to use as a barrier element.
         */
        const val BARRIER_ELEMENT: Int = 0

        /**
         * port id of home port (ie lookup when we see this port number)
         */
        const val HOME_PORT: Int = -1
    }

    /**
     * Generate a non-randomized solution with all barrier elements then two elements of each cargo side by side.
     * The returned solution is guaranteed to be valid, but not necessarily feasible
     *
     * @return A valid, feasible solution where all cargoes are handled with freight
     */
    fun generateStandardSolution(): Solution {
        val arr = IntArray(data.calculateSolutionLength())
        debug { "length of solution is ${arr.size}" }
        var index = 0

        for (i in 1..data.nrOfVessels) {
            arr[index++] = BARRIER_ELEMENT
        }
        for (i in 1..data.nrOfCargo) {
            arr[index++] = i //pickup
            arr[index++] = i //delivery
        }
        check(index == arr.size) { "Failed to generate the standard solution as one or more of the elements is 0" }
        val sol = Solution(data, arr)
        check(sol.isFeasible(false)) { "The standard solution is not feasible" }
        return sol
    }

    /**
     * @param swaps How many swaps to do. If negative the number of swaps will be [DataParser.calculateSolutionLength]
     * @param sol Existing solution to shuffle, if none specified a new solution will be generated with [generateStandardSolution]
     *
     * @return [sol], but randomized and valid
     */
    fun generateRandomSolution(
        swaps: Int = -1,
        sol: Solution = generateStandardSolution()
    ): Solution {
        val arrSize = sol.arr.size
        val nrOfSwaps = if (swaps < 0) arrSize * 2 else swaps
        debug { "swapping $nrOfSwaps times" }

        //randomize the order till the solution is valid
        do {
            for (i in 0 until nrOfSwaps) {
                sol.arr.randomizeWithin(0, arrSize, Main.rand)
            }
        } while (!sol.isValid())

        debug { "after swap = ${sol.arr.toList()}" }

        return sol
    }
}
