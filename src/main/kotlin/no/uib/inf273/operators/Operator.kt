package no.uib.inf273.operators

import no.uib.inf273.Logger
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
         * Move cargoes around within a vessel in an solution. No change is done to [sol]s [Solution.arr], however [init] will contain the new solution.
         *
         * @param sol The solution we are shuffling
         * @param vIndex The vessel index to use
         * @param init The original array of this vessel to use.
         *
         * @return If a feasible solution has been found. (note that this method only guarantees that the vessel subarray is feasible, not the whole solution)
         */
        internal fun operateVesselTilFeasible(
            sol: Solution,
            vIndex: Int,
            init: IntArray,
            allowEqual: Boolean = false,
            operation: (sub: IntArray) -> Unit
        ): Boolean {
            //when empty it is always feasible//restore sub array to make sure we can only reach direct neighbors
            //we're out of tries, restore original
            //we found a new feasible solution, update given solution
            when {
                init.isEmpty() -> return true
                //when there is only one cargo we cannot move it around so we can only return if it is feasible or not
                init.size == 2 -> return sol.isVesselFeasible(vIndex, init)
                //The freight cargo is always feasible
                vIndex == sol.data.nrOfVessels -> return true

                //if the initial solution is allowed
                // check it now to return early
                else -> {
                    val maxTries = MAX_TRIES
                    var tryNr = 0

                    val sub = init.clone()

                    //if the initial solution is allowed
                    // check it now to return early
                    if (allowEqual && sol.isVesselFeasible(vIndex, sub)) {
                        return true
                    }

                    do {
                        operation(sub)

                        if ((allowEqual || !init.contentEquals(sub)) && sol.isVesselFeasible(vIndex, sub)) {
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
            }

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
        internal fun exchangeOnceTilFeasible(
            sol: Solution,
            vIndex: Int,
            init: IntArray,
            allowEqual: Boolean = false
        ): Boolean {
            return operateVesselTilFeasible(sol, vIndex, init, allowEqual) {
                it.randomizeExchange()
            }
        }
    }
}
