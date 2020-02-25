package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.extra.randomizeExchange
import no.uib.inf273.processor.Solution

interface Operator {


    val log: Logger

    /**
     * Run the operator on the given solution. When returning the solution is guaranteed to be [Solution.isFeasible].
     *
     * @param sol A feasible solution.
     *
     *
     */
    fun operate(sol: Solution)

    companion object {

        const val MAX_TRIES = 10

        internal fun calculateNumberOfVessels(from: Int, until: Int): Int {
            return (until - from + 1) / 2
        }

        /**
         * Find a vessel within a solution that has at least [min] number of vessels within it.
         * If none is found `null` will be returned.
         *
         * @return vessel index, start of vessel array, end of vessel array in that order or `null` if there is no valid vessel
         */
        internal fun findNonEmptyVessel(sol: Solution, min: Int = 2): Triple<Int, Int, Int>? {
            require(min > 0) { "Minimum number of vessels must be > 0, got $min" }
            val validVessels = sol.getVesselRanges().mapIndexed { index, pair ->
                //Find index of each vessel, this must be done first to get correct indices
                Triple(index, pair.first, pair.second)
            }.filter { (index, from, until) ->
                //remove any instance that is not valid
                index != sol.data.nrOfVessels && calculateNumberOfVessels(from, until) >= min
            }
            return if (validVessels.isEmpty()) null else validVessels.random(Main.rand)
        }

        /**
         * Move cargoes around within a vessel in an solution. No change is done to [sol]s [Solution.arr], however [init] will contain the new solution.
         *
         * @param sol The solution we are shuffling
         * @param vIndex The vessel index to use
         * @param init The original array of this vessel to use.
         *
         * @return If a feasible solution has been found.
         */
        internal fun operateVesselTilFeasible(
            sol: Solution,
            vIndex: Int,
            init: IntArray,
            operation: (sub: IntArray) -> Unit
        ): Boolean {
            //when empty it is always feasible
            if (init.isEmpty()) return true
            //when there is only one cargo we cannot move it around so we can only return if it is feasible or not
            else if (init.size == 2) {
                return sol.isVesselFeasible(vIndex, init)
            }

            val maxTries = MAX_TRIES
            var tryNr = 0

            val sub = init.clone()

            do {
                operation(sub)

                if (!init.contentEquals(sub) && sol.isVesselFeasible(vIndex, sub)) {
                    //we found a new feasible solution, update given solution
                    sub.copyInto(init)
                    return true
                } else if (tryNr++ >= maxTries) {
                    //we're out of tries, restore original
                    return false
                } else {
                    //restore sub array to make sure we can only reach direct neighbors
                    init.copyInto(sub)
                }
            } while (true)
        }

        /**
         * Move cargoes around within a vessel in an solution.
         *
         * The operation is to call [IntArray.randomizeExchange] once.
         *
         *
         * @param sol The solution we are shuffling
         * @param vIndex The vessel index to use
         * @param init The original array of this vessel to use.
         *
         * @return A feasible solution for the given vessel (if the [init] solution was feasible). This might be identical to the initial solution given if no new solution has been found.
         */
        internal fun exchangeOnceTilFeasible(sol: Solution, vIndex: Int, init: IntArray): Boolean {
            return operateVesselTilFeasible(sol, vIndex, init) {
                it.randomizeExchange()
            }
        }
    }
}
