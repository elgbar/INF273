package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.Main.Companion.log
import no.uib.inf273.Main.Companion.rand
import no.uib.inf273.extra.filter
import no.uib.inf273.extra.forEachPermutation
import no.uib.inf273.extra.insert
import no.uib.inf273.processor.Solution
import kotlin.system.measureTimeMillis

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


    /**
     * Move the given cargo [cargoId] from the vessel [orgVesselIndex] to vessel [destVesselIndex]. How to insert a cargo into the destination vessel is a heuristic problem in it self. To make it easy to change how to insert the [operation] parameter is available
     *
     * @param sol The solution to move the cargo with
     * @param subs The arrays of each vessel in the solution
     * @param orgVesselIndex Vessel to move cargo from
     * @param destVesselIndex Vessel to move cargo to
     * @param cargoId What cargo to move from [orgVesselIndex] to [destVesselIndex]
     *
     * @return If the cargo was moved, note that [subs] array might have been moved
     */
    internal fun moveCargo(
        sol: Solution,
        subs: Array<IntArray>,
        orgVesselIndex: Int,
        destVesselIndex: Int,
        cargoId: Int
    ): Boolean {

        if (log.isDebugEnabled()) {
            require(sol.data.canVesselTakeCargo(destVesselIndex, cargoId)) {
                "Cannot move the cargo $cargoId to destination vessel $destVesselIndex as it is not compatible"
            }
        }

        val orgNew = removeCargo(sol, subs, orgVesselIndex, cargoId) ?: return false
        subs[orgVesselIndex] = orgNew

        val destNew = addCargo(sol, subs, destVesselIndex, cargoId) ?: return false
        subs[destVesselIndex] = destNew

        //reassemble the solution with the new vessel-cargo composition
        sol.joinToArray(subs)
        return true
    }

    private fun removeCargo(sol: Solution, sub: Array<IntArray>, orgVesselIndex: Int, cargoId: Int): IntArray? {
        //create new array with two less elements as they will no longer be here
        val orgOld = sub[orgVesselIndex]
        val orgNew = orgOld.filter(cargoId, IntArray(sub[orgVesselIndex].size - 2))

        if (sol.data.isDummyVessel(orgVesselIndex) || orgNew.size <= 2) {
            //nothing to optimize either the vessel is the dummy vessel or
            // it has zero or one cargoes
            return orgNew
        }

        //How can we optimize the vessel after removing a cargo?
        //What we know
        // Assuming this was the best configuration before removal we do not need to change anything before the origin of the removed cargo
        //

        //new size is small enough that we can brute force it
        if (orgNew.size <= 4 * 2) {
            log.debug { "Vessel small enough (${orgNew.size / 2} cargoes) to brute force a solution" }
            val time = measureTimeMillis {
                var bestMeta: Solution.VesselRouteMetadata? = null
                orgNew.forEachPermutation(true) {
                    val meta = sol.generateVesselRouteMetadata(orgVesselIndex, this, true)
                    if (meta.feasible && (meta.objectiveValue < bestMeta?.objectiveValue ?: Long.MAX_VALUE)) {
                        bestMeta = meta
                    }
                }
                val bestArr = bestMeta?.arr ?: return null
                bestArr.copyInto(orgNew)
            }

            log.debug { "Finish brute forcing vessel. Took $time ms for ${orgNew.size} elements" }
        } else {
            log.debug { "Vessel too large (${orgNew.size / 2} cargoes) to brute force" }
        }
//        optimizeVessel(sol, sub[orgVesselIndex], orgVesselIndex)

        //A vessel will always be feasible when removing a cargo.
        // All it does in the worst case is force the vessel to wait longer at each port
        if (log.isDebugEnabled()) {
            check(sol.isVesselFeasible(orgVesselIndex, orgNew)) {
                "Origin vessel $orgVesselIndex not feasible after removing a cargo"
            }
        }
        return orgNew
    }

    private fun addCargo(
        sol: Solution,
        sub: Array<IntArray>,
        destVesselIndex: Int,
        cargoId: Int
    ): IntArray? {

        val destOldSize = sub[destVesselIndex].size

        //nothing fancy to do when destination is empty or the dummy vessel
        if (destOldSize == 0 || destVesselIndex == sub.size - 1) {
            //the destination array needs to be two element larger for the new cargo to fit
            val destNew = sub[destVesselIndex].copyOf(destOldSize + 2)
            destNew[destNew.size - 1] = cargoId
            destNew[destNew.size - 2] = cargoId
            return if (sol.isVesselFeasible(destVesselIndex, destNew)) destNew else null
        }

        var bestFirstConfig: IntArray? = null
        var bestFirstObjVal = Long.MAX_VALUE //THE worst
        var firstInsertedIndex = -1

        //insert origin cargo til we find the best (feasible) insertion location

        for (i in 0..destOldSize) {
            val firstDestMaybe = sub[destVesselIndex].copyOf(destOldSize + 1)
            firstDestMaybe.insert(i, cargoId)
            val metadata = sol.generateVesselRouteMetadata(destVesselIndex, firstDestMaybe)

            //Only update when feasible and better
            if (metadata.feasible && metadata.objectiveValue < bestFirstObjVal) {
                bestFirstConfig = firstDestMaybe
                bestFirstObjVal = metadata.objectiveValue
                firstInsertedIndex = i
            }
        }

        val bestFirstConfigNN = bestFirstConfig
        if (bestFirstConfigNN == null) {
            log.trace { "Failed to find a place to insert the pickup of cargo $cargoId for vessel $destVesselIndex" }
            return null
        }

        var bestConfig: IntArray? = null
        var bestObjVal = Long.MAX_VALUE

        for (i in firstInsertedIndex + 1 until destOldSize + 2) {

            val destMaybe = bestFirstConfigNN.copyOf(destOldSize + 2)
            destMaybe.insert(i, cargoId)
            val metadata = sol.generateVesselRouteMetadata(destVesselIndex, destMaybe)

            //Only update when feasible and better
            if (metadata.feasible && metadata.objectiveValue < bestObjVal) {
                bestConfig = destMaybe
                bestObjVal = metadata.objectiveValue
            }
        }

        return bestConfig
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
            allowBruteForce: Boolean = true,
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
                initVesselArr.size == 2 -> return allowEqual && sol.isVesselFeasible(vIndex, initVesselArr)
                //The freight cargo is always feasible
                vIndex == sol.data.nrOfVessels -> return true

                else -> {

                    //keep a copy of the vessel array
                    val sub = initVesselArr.clone()

                    //if the initial solution is allowed
                    // check it now to return early
                    if (allowEqual && sol.isVesselFeasible(vIndex, sub)) {
                        return true
                    }

                    if (allowBruteForce && sub.size <= 4 * 2) {
                        log.debug { "Vessel small enough (${sub.size / 2} cargoes) to brute force a solution" }

                        var bestMeta: Solution.VesselRouteMetadata? = null
                        sub.forEachPermutation(true) {
                            val meta = sol.generateVesselRouteMetadata(vIndex, this, true)
                            if (meta.feasible && (meta.objectiveValue < bestMeta?.objectiveValue ?: Long.MAX_VALUE)) {
                                bestMeta = meta
                            }
                        }
                        val bestArr = bestMeta?.arr ?: return false
                        bestArr.copyInto(initVesselArr)
                        return true
                    } else {

                        val maxTries = MAX_TRIES
                        var tryNr = 0

                        do {
                            operation(sub)

                            if ((allowEqual || !initVesselArr.contentEquals(sub)) &&
                                sol.isVesselFeasible(vIndex, sub)
                            ) {
                                //we found a new feasible solution, update given solution
                                sub.copyInto(initVesselArr)
                                return true
                            } else if (tryNr++ >= maxTries) {
                                //we're out of tries
                                return false
                            } else {
                                //restore sub array to make sure we can only reach direct neighbors
                                initVesselArr.copyInto(sub)
                            }
                        } while (true)

                    }
                }
            }
        }


        /**
         * Select two random distinct vessels where the first vessel selected is non-empty
         *
         * @param discourageFreightPercent A double in range `[0.0, 1.0]` where `1.0` will never accept destination to be the dummy freight vessel (if it is chosen) and `0.0` will always accept it. Meaning that a value of `0.5` will accept destination to be freight 50% of the time it is selected
         */
        internal fun selectTwoRandomVessels(
            sub: Array<IntArray>,
            discourageFreightPercent: Double = 0.0
        ): Pair<Int, Int> {
            require(discourageFreightPercent in 0.0..1.0) { "discourageFreightPercent must be in range [0.0, 1.0] was $discourageFreightPercent" }

            fun allowFreight(): Boolean {
                return discourageFreightPercent < rand.nextDouble()
            }

            var orgVesselIndex: Int
            var destVesselIndex: Int
            do {
                orgVesselIndex = selectRandomVessel(sub, 1, true)
                destVesselIndex = selectRandomVessel(sub, 0, allowFreight())
            } while (orgVesselIndex == destVesselIndex)
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
            require(minCargo >= 0) { "Minimum number of cargoes must be a non-negative number. Given $minCargo" }
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
                vesselIndex = rand.nextInt(size)
            } while (invalidSize(sub[vesselIndex]))
            return vesselIndex
        }
    }
}
