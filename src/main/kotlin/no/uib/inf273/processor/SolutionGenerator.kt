package no.uib.inf273.processor;

import no.uib.inf273.Logger.debug
import java.util.*

class SolutionGenerator(val data: DataHolder) {

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
     * Generate a non-randomized solution with two elements of each cargo side by side then all barrier elements.
     * The returned solution is guaranteed to be valid, but not necessarily feasible
     *
     * @return A valid solution, duplicate solutions returned each time
     */
    fun generateStandardSolution(): Solution {
        val arr = IntArray(data.calculateSolutionLength())
        debug { "length of solution is ${arr.size}" }
        var index = 0
        for (i in 1..data.nrOfCargo) {
            arr[index++] = i //pickup
            arr[index++] = i //delivery
        }
        for (i in 1..data.nrOfVessels) {
            arr[index++] = BARRIER_ELEMENT
        }
        check(index == arr.size) { "Failed to generate the standard solution as one or more of the elements is 0" }
        val sol = Solution(data, arr)
        check(sol.isValid(true)) { "The standard solution is not valid" }
        return sol
    }

    /**
     * @param swaps How many swaps to do. If negative the number of swaps will be [DataHolder.calculateSolutionLength]
     * @param rng Random instance to use,
     *
     * @return A random, valid solution
     */
    fun generateRandomSolution(swaps: Int = -1, rng: Random = Random()): Solution {
        val solution: Solution = generateStandardSolution()
        val arrSize = solution.solArr.size
        val swaps0 = if (swaps < 0) arrSize * 2 else swaps
        debug { "swapping $swaps0 times" }

        //randomize the order till the solution is valid
        do {
            for (i in 0..rng.nextInt(swaps0)) {
                val first = rng.nextInt(arrSize)
                val second = rng.nextInt(arrSize)
                //swap the two elements, yes this is kotlin magic
                solution.solArr[first] =
                    solution.solArr[second].also { solution.solArr[second] = solution.solArr[first] }
            }
        } while (!solution.isValid())

        debug { "after swap = ${solution.solArr.toList()}" }

        return solution
    }
}
