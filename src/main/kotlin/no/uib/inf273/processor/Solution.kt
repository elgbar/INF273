package no.uib.inf273.processor

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.data.VesselCargo
import no.uib.inf273.operators.Operator
import no.uib.inf273.processor.SolutionGenerator.Companion.BARRIER_ELEMENT

/**
 * @param data The instance to solve
 * @param arr The raw solution
 * @param split If we should cache [subRoutes] in constructor. Default value is `true` but can be turned off if you create a solution without real data in [arr]
 */
class Solution(val data: DataParser, val arr: IntArray, split: Boolean = true) {


    /**
     * Internal array of the current solution split into multiple sub-arrays. This will be updated when calling [splitToSubArray]
     */
    internal val subRoutes: Array<IntArray>
    internal val ranges: MutableList<Pair<Int, Int>> = ArrayList()

    init {
        require(arr.size == data.calculateSolutionLength()) {
            "Given solution is not compatible with the given data. Expecting an array of length ${data.calculateSolutionLength()} but got ${arr.size}"
        }
        subRoutes = Array(data.nrOfVessels + 1) { IntArray(0) }
        if (split) {
            splitToSubArray(true)
        }
    }

    /**
     * Give metadata on how a vessel is doing for this solution
     */
    data class VesselRouteMetadata(
        /**
         * The vessel used
         */
        val vesselIndex: Int,
        /**
         * If this route is valid
         */
        val valid: Boolean,
        /**
         * How long the vessel waited at each port. A value of zero indicate that no waiting was done. The value cannot be negative as that indicate we are too late.
         */
        val portTardiness: IntArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is VesselRouteMetadata) return false

            if (vesselIndex != other.vesselIndex) return false
            if (valid != other.valid) return false
            if (!portTardiness.contentEquals(other.portTardiness)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = vesselIndex
            result = 31 * result + valid.hashCode()
            result = 31 * result + portTardiness.contentHashCode()
            return result
        }


    }

    /**
     * @return a deep copy of this solution
     */
    fun copy(): Solution {
        return Solution(data, arr.clone())
    }

    /**
     * Check if a solution is valid (but not necessarily feasible). A solution is valid if for each subsection (split by [BARRIER_ELEMENT]) there are two of each number. This assumes that the given array does not have more than two identical numbers (excluding [BARRIER_ELEMENT])
     *
     * @param modified If the solution array have been modified since last time we called any function in this class. If you're unsure the default is `true`
     *
     * @return If this solution is valid
     *
     */
    fun isValid(modified: Boolean): Boolean {
        val cargoCount = IntArray(data.nrOfCargo + 1)
        for (value in arr) {
            cargoCount[value]++
        }

        if (data.nrOfVessels != cargoCount[0]) {
            log.debug { "Solution not valid as number of barrier elements is not equal to number if vessels. It is ${cargoCount[0]} expected ${data.nrOfVessels}" }
            return false
        }

        for ((cargoId, count) in cargoCount.withIndex()) {
            if (cargoId != 0 && count != 2) {
                log.debug { "The cargo $cargoId was seen $count times, they can only appear twice." }
                return false
            }
        }

        for (sub in splitToSubArray(modified)) {
            //make sure the sub array actually have an even number of elements

            //sets cannot contain duplicate elements so if the size of the set for this subarray is not
            // exactly half size of the original set it contains duplicate elements
            if (sub.size % 2 != 0) {
                log.debug { "Size of ${sub.toList()} is odd: ${sub.size % 2 != 0}" }
                return false
            } else if (sub.toHashSet().size * 2 != sub.size) {
                log.debug { "as set is _not_ half of sub size: ${sub.toHashSet().size * 2 != sub.size}" }
                return false
            }
        }

        return true
    }

    /**
     * Check the feasibility of this solution.
     *
     * @param modified If the solution array have been modified since last time we called any function in this class. If you're unsure the default is `true`
     * @param checkValid If we should check if this solution is valid, otherwise this is just assumed
     *
     * @return If this solution is both feasible (and valid if [checkValid] is `true`)
     */
    fun isFeasible(modified: Boolean, checkValid: Boolean = true): Boolean {
        val subroutes: Array<IntArray> = splitToSubArray(modified)

        if (checkValid && !isValid(false)) {
            log.debug { "Checked validity of solution before feasibility and it is not valid" }
            return false
        }

        for ((index, sub) in subroutes.withIndex()) {
            if (!isVesselFeasible(index, sub)) return false
        }

        return true
    }

    /**
     * check if a vessel is feasible
     * */
    fun isVesselFeasible(vIndex: Int, sub: IntArray): Boolean {
        return generateVesselRouteMetadata(vIndex, sub).valid
    }

    /**
     * Generate metadata of the vessel route [sub] for index [vIndex]
     */
    fun generateVesselRouteMetadata(vIndex: Int, sub: IntArray): VesselRouteMetadata {
        val portTardiness = IntArray(sub.size) { -1 }

        fun createMetadata(valid: Boolean): VesselRouteMetadata {
            return VesselRouteMetadata(vIndex, valid, portTardiness)
        }

        /**
         * @return negative number of not valid, new current time if valid
         */
        fun checkTime(index: Int, lowerTime: Int, upperTime: Int, portTime: Int, currTime: Int): Int {
            var time = currTime

            //How long we are we waiting for the port to open?
            portTardiness[index] = (lowerTime - time).coerceAtLeast(0)

            //we must wait for the port to open
            if (time < lowerTime) {
                time = lowerTime
            }

            //then add how long it takes at the port
            time += portTime

            //check if we are within the upper time window
            if (time > upperTime) {
                //well that failed
                return -1
            }
            return time
        }

        //skip last array as it is only the tramp transports, and always allowed
        if (vIndex == data.nrOfVessels) return createMetadata(true)

        //false if we are currently picking it up, true if we are delivering
        val seen = BooleanArray(data.nrOfCargo)

        val vesselId = vIndex + 1
        val vessel = data.vesselFromId(vesselId)
        var currWeight = 0
        var currTime = vessel.startTime
        var lastPort = SolutionGenerator.HOME_PORT //vessel start at home port
        for ((index, cargoId) in sub.withIndex()) {
            val cargoIndex = cargoId - 1
            val cargo = data.cargoes[cargoIndex]

            val currPort = if (seen[cargoIndex]) cargo.destPort else cargo.origin_port
            val vc: VesselCargo = data.vesselCargo[Pair(vesselId, cargoId)]
                ?: error("Failed to find data connecting vessel $vesselId and cargo $cargoId")

            //substitute the dummy home port id with the vessels actual home port
            if (lastPort == SolutionGenerator.HOME_PORT) {
                lastPort = vessel.homePort
            }

            //add the sailing time to the current time
            currTime += (data.archs[Triple(vesselId, lastPort, currPort)]
                ?: error("Failed to find an arch for vessel $vesselId between the ports $lastPort and $currPort")).time

            if (!seen[cargoIndex]) {
                seen[cargoIndex] = true

                //check compatibility, but only do so for first encounter
                if (!vessel.canTakeCargo(cargoId)) {
                    log.debug { "Vessel $vesselId is not compatible with $cargoId" }
                    return createMetadata(false)
                }
                currWeight += cargo.size //first encounter, load the cargo

                //check for cargo pickup time

                currTime = checkTime(index, cargo.lowerPickup, cargo.upperPickup, vc.originPortTime, currTime)
                if (currTime < 0) {
                    log.debug { "We are trying to pickup the cargo $cargoId after upper pickup time" }
                    return createMetadata(false)
                }

            } else {
                currWeight -= cargo.size //second encounter, unload the cargo

                //check for cargo delivery time
                currTime = checkTime(index, cargo.lowerDelivery, cargo.upperDelivery, vc.destPortTime, currTime)
                if (currTime < 0) {
                    log.debug { "We are trying to deliver the cargo $cargoId after upper delivery time" }
                    return createMetadata(false)
                }
            }

            //check that we are not overloaded
            if (currWeight > vessel.capacity) {
                log.debug { "Invalid as vessel $vesselId is trying to carry more than it has capacity for ($currWeight > ${vessel.capacity})" }
                return createMetadata(false)
            }

            //update port for next round
            lastPort = currPort
        }
        return createMetadata(true)
    }


    /**
     * Calculate the objective value. result is not cached.
     *
     * @param modified If the solution array have been modified since last time we called any function in this class. If you're unsure the default is `true`
     *
     * @return The objective value of this solution
     *
     */
    fun objectiveValue(modified: Boolean = true): Long {
        var value = 0L
        val subroutes: Array<IntArray> = splitToSubArray(modified)


        for ((index, sub) in subroutes.withIndex()) {
            value += objectiveVesselValue(index, sub)
        }
        return value
    }

    fun objectiveVesselValue(index: Int, sub: IntArray): Long {
        var value = 0L
        //skip last array as it is only the tramp transports, and always allowed
        if (index == data.vessels.size) {
            //for each cargo not transported add the not transport value
            value += sub.toSet().map { data.cargoFromId(it).ntCost }.sum()
        } else {

            val seen = HashSet<Int>()

            val vesselId = index + 1
            val vessel = data.vesselFromId(vesselId)
            var lastPort = SolutionGenerator.HOME_PORT //vessel start at home port

            for (cargoId in sub) {
                val cargoIndex = cargoId - 1
                val cargo = data.cargoes[cargoIndex]

                val currPort = if (seen.contains(cargoIndex)) cargo.destPort else cargo.origin_port
                val vc: VesselCargo = data.vesselCargo[Pair(vesselId, cargoId)] ?: VesselCargo.incompatibleVesselCargo

                //substitute the dummy home port id with the vessels actual home port
                if (lastPort == SolutionGenerator.HOME_PORT) {
                    lastPort = vessel.homePort
                }

                //add the sailing time to the current time
                value += (data.archs[Triple(vesselId, lastPort, currPort)]
                    ?: error("Failed to find arch for vessel $vesselId from $lastPort to $currPort")).cost

                value +=
                    if (seen.contains(cargoIndex)) {
                        vc.destPortCost
                    } else {
                        seen.add(cargoIndex)
                        vc.originPortCost
                    }

                //update port for next round
                lastPort = currPort
            }
        }
        return value
    }


    ///////////////////////
    //   Helper methods  //
    ///////////////////////

    /**
     * Return this solution in a more managing way
     *
     * @param modified if we trust that no modification have been done to this array since last time it was split
     */
    fun splitToSubArray(modified: Boolean): Array<IntArray> {
        if (modified) {
            val barrierIndices = getVesselRanges(modified)

            for ((i, pair) in barrierIndices.withIndex()) {
                val (from, to) = pair //cannot do this in the loop

                val subArr = if (to != from) arr.copyOfRange(from, to) else EMPTY_SUB
                log.trace { "range from $from to $to: ${subArr.toList()}" }
                subRoutes[i] = subArr
            }
        }
        return subRoutes
    }

    fun joinToArray(merge: Array<MutableList<Int>>) {
        joinToArray(merge.map { it.toIntArray() }.toTypedArray())
    }

    /**
     * Join a split array back into the original.
     * The caches will be updated so there is no need to use modified after this method
     */
    fun joinToArray(merge: Array<IntArray>) {
        require(arr.size == merge.map { it.size }.sum() + data.nrOfVessels) {
            "Given array of arrays does not have the total size equal to size of this solution's array. Expected ${arr.size} got ${merge.map { it.size }
                .sum() + data.nrOfVessels}"
        }

        log.trace { "Merging array ${merge.map { it.contentToString() + "\n" }}" }

        ranges.clear()

        var offset = 0
        for ((index, sub) in merge.withIndex()) {
            log.traces {
                listOf(
                    "current array = ${arr.contentToString()}",
                    "Appending vessel ${sub.contentToString()} from offset $offset"
                )
            }

            sub.copyInto(arr, offset)

            //update cache
            subRoutes[index] = sub
            ranges.add(offset to offset + sub.size)

            offset += sub.size
            if (offset != arr.size) { //after the last array we do not want to add a barrier element
                arr[offset++] = BARRIER_ELEMENT
                log.trace { "not last, appending barrier" }
            }
        }
    }


    /**
     * Calculate where the cargoes are for each vessel. The last element are those cargoes who are carried by freights.
     *
     * All ranges behave like standard ranges, first element is inclusive while last element is exclusive.
     *
     * @return List of ranges
     */
    fun getVesselRanges(modified: Boolean): List<Pair<Int, Int>> {
        if (modified || ranges.isEmpty()) {
            ranges.clear()

            //TODO use a simple loop, predefine a array of size nrofvessels then loop till 0 is found (inc by 2 when a non barrier is found)
            val barrierIndices = arr.mapIndexed { index, i -> Pair(index, i) }.filter { (_, i) -> i == BARRIER_ELEMENT }
                .map { (index, _) -> index }.toIntArray()

            log.trace {
                //only do the check while log.debugging to reduce overhead
                check(barrierIndices.size == data.nrOfVessels) {
                    "Number of barriers found does not match the expected amount. Expected ${data.nrOfVessels} barriers but got ${barrierIndices.size} for solution ${arr.contentToString()}"
                }
                "Found barrier elements at ${barrierIndices.contentToString()} for solution ${arr.contentToString()}"
            }

            // map the ranges after we have all indices to allow for referencing last barrier indices
            barrierIndices.mapIndexedTo(ranges) { i, barrierIndex ->
                val from =
                    // This is the first iteration, we must start at zero
                    if (i == 0) 0
                    // We start from the element after last barrier
                    else barrierIndices[i - 1] + 1
                return@mapIndexedTo from to barrierIndex
            }
            //add the cargoes that travel by freight
            ranges += barrierIndices[barrierIndices.size - 1] + 1 to arr.size
        }
        return ranges
    }

    /**
     * Get what vessel (index) the given [index] is within.
     *
     * @param index index within [arr] to get the vessel of
     * @param vesselRange The ranges of vessels, default value is calling [getVesselRanges] with no arguments
     *
     * @throws IllegalArgumentException If the vessel is a [BARRIER_ELEMENT] within [arr]
     */
    fun getVesselIndex(
        index: Int,
        vesselRange: List<Pair<Int, Int>> = getVesselRanges(true)
    ): Int {
        //range check is done here implicitly
        require(arr[index] != BARRIER_ELEMENT) { "Given index corresponds to a barrier element" }

        for ((i, pair) in vesselRange.withIndex()) {
            val (start, stop) = pair
            if (index in start until stop) {
                return i
            }
        }
        throw IllegalStateException("Failed to find a vessel that index $index corresponds to in ${arr.contentToString()}")
    }


    /**
     * Find a vessel within a solution that has at least [min] number of vessels within it.
     * If none is found `null` will be returned.
     *
     * @return vessel index, start of vessel array, end of vessel array in that order or `null` if there is no valid vessel
     */
    internal fun findNonEmptyVessel(modified: Boolean, min: Int = 2): Triple<Int, Int, Int>? {
        require(min > 0) { "Minimum number of vessels must be > 0, got $min" }
        val validVessels = getVesselRanges(modified).mapIndexed { index, pair ->
            //Find index of each vessel, this must be done first to get correct indices
            Triple(index, pair.first, pair.second)
        }.filter { (index, from, until) ->
            //remove any instance that is not valid
            index != data.nrOfVessels && Operator.calculateNumberOfVessels(from, until) >= min
        }
        return if (validVessels.isEmpty()) null else validVessels.random(Main.rand)
    }

    override fun toString(): String {
        return arr.contentToString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Solution) return false

        if (data != other.data) return false
        if (!arr.contentEquals(other.arr)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + arr.contentHashCode()
        return result
    }

    companion object {
        val log = Logger("Sol")
        private val EMPTY_SUB = IntArray(0)
    }
}
