package no.uib.inf273.processor

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.extra.nextInt0

class SolutionGenerator(val data: DataParser) {

    companion object {

        private val log = Logger()

        /**
         * The element to use as a barrier element.
         */
        const val BARRIER_ELEMENT: Int = 0

        /**
         * port id of home port (ie lookup when we see this port number)
         */
        const val HOME_PORT: Int = -1
    }

    private lateinit var std: Solution

    /**
     * Generate a non-randomized solution with all barrier elements then two elements of each cargo side by side.
     * The returned solution is guaranteed to be valid, but not necessarily feasible
     *
     * @return A valid, feasible solution where all cargoes are handled with freight
     */
    fun generateStandardSolution(): Solution {
        if (!::std.isInitialized) {
            val arr = IntArray(data.calculateSolutionLength())
            log.debug { "length of solution is ${arr.size}" }
            var index = 0

            for (i in 1..data.nrOfVessels) {
                arr[index++] = BARRIER_ELEMENT
            }
            for (i in 1..data.nrOfCargo) {
                arr[index++] = i //pickup
                arr[index++] = i //delivery
            }
            check(index == arr.size) { "Failed to generate the standard solution as one or more of the elements is 0" }
            std = Solution(data, arr)
        }
        return Solution(data, std.arr.clone())
    }

    /**
     * @return A random valid solution
     */
    fun generateRandomSolution(): Solution {

        val list = Array<MutableList<Int>>(data.nrOfVessels + 1) { ArrayList() }
        val sol = Solution(data, IntArray(data.calculateSolutionLength()), split = false)

        list.forEach { it.clear() }
        for (i in 1..data.nrOfCargo) {
            val vessel: MutableList<Int> = list.random(Main.rand)
            vessel.add(Main.rand.nextInt0(vessel.size), i) //pickup
            vessel.add(Main.rand.nextInt0(vessel.size), i) //delivery
        }
        sol.joinToArray(list)
        return sol
    }
}
