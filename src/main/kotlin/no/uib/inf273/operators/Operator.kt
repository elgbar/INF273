package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.extra.filter
import no.uib.inf273.extra.insert
import no.uib.inf273.processor.Solution

abstract class Operator {


    open val log: Logger = Logger(javaClass.simpleName)

    /**
     * Run the operator on the given solution. When returning the solution is guaranteed to be [Solution.isFeasible].
     *
     * @param sol A feasible solution.
     *
     *
     */
    abstract fun operate(sol: Solution)

    override fun toString(): String {
        return this::class.java.simpleName
    }

    companion object {

        /**
         * Number of times to try and change the solution till it's valid
         */
        private const val MAX_TRIES = 10

        /**
         * Vessel id returned if no valid vessel could be found
         */
        const val INVALID_VESSEL = -1

        internal fun calculateNumberOfVessels(from: Int, until: Int): Int {
            return (until - from + 1) / 2
        }


        /**
         * Move cargoes around within a vessel in an solution. No change is done to [sol]s [Solution.arr].
         * Note that this method only guarantees that the vessel subarray is feasible, not the whole solution
         *
         * @param sol The solution we are shuffling
         * @param vIndex The vessel index to use
         * @param initVesselArr The content of vessel [vIndex]
         * @param allowEqual If [initVesselArr] is allowed to be returned without change
         * @param operation How to change the given [initVesselArr]
         *
         * @return If a feasible solution has been found. [initVesselArr] will contain the new solution ONLY if this method returns `true`. If this returns `false` [initVesselArr] will be infeasible.
         *
         */
        internal fun operateVesselTilFeasible(
            sol: Solution,
            vIndex: Int,
            initVesselArr: IntArray,
            allowEqual: Boolean = false,
            operation: (sub: IntArray) -> Unit
        ): Boolean {

            require(initVesselArr.size % 2 == 0) {
                "The vessel array is invalid as it contains a odd number of elements: ${initVesselArr.contentToString()}"
            }

            //when empty it is always feasible//restore sub array to make sure we can only reach direct neighbors
            //we're out of tries, restore original
            //we found a new feasible solution, update given solution
            when {
                //Vessels with zero or one cargoes are special as they cannot be moved around
                initVesselArr.isEmpty() -> return true //empty always feasible
                //when there is only one cargo we cannot move it around so we can only return if it is feasible or not
                initVesselArr.size == 2 -> return sol.isVesselFeasible(vIndex, initVesselArr)
                //The freight cargo is always feasible
                vIndex == sol.data.nrOfVessels -> return true

                //if the initial solution is allowed
                // check it now to return early
                else -> {
                    val maxTries = MAX_TRIES
                    var tryNr = 0

                    //keep a copy of the vessel array
                    val sub = initVesselArr.clone()

                    //if the initial solution is allowed
                    // check it now to return early
                    if (allowEqual && sol.isVesselFeasible(vIndex, sub)) {
                        return true
                    }

                    do {
                        operation(sub)

                        if ((allowEqual || !initVesselArr.contentEquals(sub)) && sol.isVesselFeasible(vIndex, sub)) {
                            //we found a new feasible solution, update given solution
                            sub.copyInto(initVesselArr)
                            return true
                        } else if (tryNr++ >= maxTries) {
                            //we're out of tries, restore original
                            return false
                        } else {
                            //restore sub array to make sure we can only reach direct neighbors
                            initVesselArr.copyInto(sub)
                        }
                    } while (true)
                }
            }
        }

        /**
         * Move the given cargo [cargoId] from the vessel [orgVesselIndex] to vessel [destVesselIndex]. How to insert a cargo into the destination vessel is a heuristic problem in it self. To make it easy to change how to insert the [operation] parameter is available
         *
         * @param sol The solution to move the cargo with
         * @param sub The arrays of each vessel in the solution
         * @param orgVesselIndex Vessel to move cargo from
         * @param destVesselIndex Vessel to move cargo to
         * @param cargoId What cargo to move from [orgVesselIndex] to [destVesselIndex]
         * @param operation How to insert the cargo to the vessel at [destVesselIndex]
         */
        internal fun moveCargo(
            sol: Solution,
            sub: Array<IntArray>,
            orgVesselIndex: Int,
            destVesselIndex: Int,
            cargoId: Int,
            operation: (sub: IntArray) -> Unit
        ) {
            val orgNew = removeCargo(sol, sub, orgVesselIndex, cargoId) ?: return
            sub[orgVesselIndex] = orgNew

            val destNew = addCargo(sol, sub, destVesselIndex, cargoId, operation) ?: return
            sub[destVesselIndex] = destNew

            //reassemble the solution with the new vessel-cargo composition
            sol.joinToArray(sub)
        }

        private fun removeCargo(sol: Solution, sub: Array<IntArray>, orgVesselIndex: Int, cargoId: Int): IntArray? {
            //create new array with two less elements as they will no longer be here
            val orgNew = sub[orgVesselIndex].filter(cargoId, IntArray(sub[orgVesselIndex].size - 2))

            //A vessel will always be feasible when removing a cargo.
            // All it does in the worst case is force the vessel to wait longer at each port
            check(sol.isVesselFeasible(orgVesselIndex, orgNew)) {
                "Origin vessel $orgVesselIndex not feasible after removing a cargo"
            }
            return orgNew
        }

        private fun addCargo(
            sol: Solution,
            sub: Array<IntArray>,
            destVesselIndex: Int,
            cargoId: Int,
            operation: (sub: IntArray) -> Unit
        ): IntArray? {

            val destOldSize = sub[destVesselIndex].size
            //the destination array needs to be two element larger for the new cargo to fit
            val destNew = sub[destVesselIndex].copyOf(destOldSize + 2)
            if (destOldSize > 0 && destVesselIndex != sub.size - 1) {
                //insert the values randomly
                destNew.insert(Main.rand.nextInt(destOldSize), cargoId)
                //second time around we need to account for the element we just inserted
                destNew.insert(Main.rand.nextInt(destOldSize + 1), cargoId)
            } else {
                //no need for randomness when placing cargo into an empty vessel or
                // when placing it in the freight dummy vessel
                destNew[destNew.size - 1] = cargoId
                destNew[destNew.size - 2] = cargoId
            }

            if (!operateVesselTilFeasible(sol, destVesselIndex, destNew, true, operation)) {
                Main.log.trace { "Failed to add cargo $cargoId to vessel $destVesselIndex as no feasible arrangement could be found" }
                return null
            }

            if (Main.log.isDebugEnabled()) {
                check(sol.isVesselFeasible(destVesselIndex, destNew)) {
                    "Destination vessel $destVesselIndex not feasible"
                }
            }
            return destNew
        }

        /**
         * Select two random distinct vessels where the first vessel selected is non-empty
         *
         * visible in a function for testing
         */
        internal fun selectTwoRandomVessels(sub: Array<IntArray>): Pair<Int, Int> {
            var orgVesselIndex: Int
            var destVesselIndex: Int
            do {
                orgVesselIndex = Main.rand.nextInt(sub.size)
                destVesselIndex = Main.rand.nextInt(sub.size)
            } while (orgVesselIndex == destVesselIndex || sub[orgVesselIndex].isEmpty())
            return orgVesselIndex to destVesselIndex
        }

        /**
         * Return the index of a random vessel within [sub]. If [allowFreight] and [minCargo]` > 0` are both `true` this
         * might return [INVALID_VESSEL] as no valid vessel could ever be selected. There should not be any need to test for this if either is `false`
         *
         * @param minCargo The minimum number of cargoes that must exist in the given
         *
         */
        fun selectRandomVessel(sub: Array<IntArray>, minCargo: Int, allowFreight: Boolean): Int {
            require(minCargo >= 0) { "Minimum number of cargoes must be a non-negative number" }
            val size = sub.size - if (allowFreight) 0 else 1

            fun invalidSize(arr: IntArray): Boolean {
                return arr.size / 2 < minCargo
            }

            if (minCargo > 0 && !allowFreight && sub.toList().subList(0, size).all { invalidSize(it) }) {
                //can never select any valid vessel (probably every cargo is in freight)
                return INVALID_VESSEL
            }

            var vesselIndex: Int
            do {
                vesselIndex = Main.rand.nextInt(size)
            } while (invalidSize(sub[vesselIndex]))
            return vesselIndex
        }

    }
}
