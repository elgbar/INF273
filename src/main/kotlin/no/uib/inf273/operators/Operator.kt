package no.uib.inf273.operators

import no.uib.inf273.Main
import no.uib.inf273.extra.randomizeWithin
import no.uib.inf273.processor.Solution

interface Operator {

    /**
     * Run the operator on the given solution. When returning the solution is guaranteed to be [Solution.isFeasible].
     *
     * @param sol A feasible solution.
     *
     *
     */
    fun operate(sol: Solution)

    companion object {


        internal fun calculateNumberOfVessels(from: Int, until: Int): Int {
            return (until - from + 1) / 2
        }

        /**
         * Find a vessel within a solution that has at least [min] number of vessels within it.
         * If none is found `null` will be returned.
         *
         *
         *
         * @return vessel index, start of vessel array, end of vessel array in that order or `null` if there is no valid vessel
         */
        fun findNonEmptyVessel(sol: Solution, min: Int = 2): Triple<Int, Int, Int>? {
            require(min > 0) { "Minimum number of vessels must be > 0, got $min" }
            val barriers: List<Pair<Int, Int>> = sol.getVesselRanges()
            var vIndex: Int
            var from: Int
            var until: Int

            do {
                vIndex = Main.rand.nextInt(barriers.size)
                //two indices where the random value between them will always be within a vessel
                val (t, f) = barriers[vIndex]
                from = t
                until = f

                // Select a subarray that has more than n elements, the number of vessels within a subrange is
                //FIXME an edge-case where every vessel have less than two cargoes, ie if we have more vessels than cargoes
            } while (
                calculateNumberOfVessels(from, until) < min
            )
            return Triple(vIndex, from, until)
        }

        /**
         * The cargoes in the vessel til it is feasible. Well maybe
         *
         * @return if the shuffle was successful
         */
        fun shuffleVesselTilFeasible(sol: Solution, vIndex: Int, sub: IntArray): Boolean {
            //Number of combinations is `(length of sub / 2)!`, it's a bit big so after size of 10 ish we just tries 10k times
            // Note that the minimum size is 4 due to the barrier selection above and increases by two each time as a cargo
            // contains to element in the array.
            //
            //We have this method as we do not want to iterate too much for the smaller sizes
            val maxTries = when (sub.size) {
                4 -> 24
                6 -> 120
                8 -> 720
                10 -> 5040
                else -> 10_000
            }

            var tryNr = 1
            val init = sub.copyOf()

            do {
                //we're out of tries do nothing
                if (tryNr++ <= maxTries) {
                    return false
                }

                sub.randomizeWithin()
            } while (!sub.contentEquals(init) && !sol.isVesselFeasible(vIndex, sub))
            return true
        }
    }
}
