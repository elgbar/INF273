package no.uib.inf273

import no.uib.inf273.Logger.debug
import no.uib.inf273.data.VesselCargo
import no.uib.inf273.input.DataHolder
import java.io.File
import java.util.*


object Main {

    const val FALLBACK_FILE = "Call_7_Vehicle_3.txt"
    /**
     * The element to use as a barrier element.
     */
    const val BARRIER_ELEMENT: Int = -1

    /**
     * port id of home port (ie lookup when we see this port number)
     */
    const val HOME_PORT: Int = 0

    lateinit var data: DataHolder

    fun init(args: Array<String>) {

        Logger.logLevel = 2

        val content = if (args.isEmpty()) {
            readInternalFile()
        } else {
            val path = args[0]
            File(path).readText()
        }

        check(!content.isNullOrEmpty()) { "Failed to read file as it is null or empty" }

        data = DataHolder(content)

        println("Parsed file successfully")

        var sol: IntArray = generateRandomSolution()
        while (!checkFeasibility(sol)) {
            sol = generateRandomSolution()
            println("random valid solution = ${sol.toList()}")
        }

        println("random valid & feasible solution = ${sol.toList()}")
    }

    /**
     * Calculate how long the solution array must be to fit the current given data.
     *
     * This method returns `data.nrOfCargo * 2 + data.nrOfVessels` elements.
     *
     * where `data.nrOfCargo * 2` the number of actual cargoes. multiply by two as the we need to pickup and deliver each cargo.
     *
     * and `data.nrOfVessels` is the number of barrier elements. We have one barrier for each vessel, if any cargo is not transported they will be after the last barrier
     *
     */
    fun calculateSolutionLength(): Int {
        return data.nrOfCargo * 2 + data.nrOfVessels
    }

    /**
     * Generate a non-randomized solution where the `data.nrOfCargo*2` are pairs of two equal cargo id's and the last
     */
    fun generateStandardSolution(): IntArray {
        val sol = IntArray(calculateSolutionLength())
        debug { "length of solution is ${sol.size}" }
        var index = 0
        for (i in 1..data.nrOfCargo) {
            sol[index++] = i //pickup
            sol[index++] = i //delivery
        }
        for (i in 1..data.nrOfVessels) {
            sol[index++] = BARRIER_ELEMENT
        }
        check(index == sol.size) { "Failed to generate the standard solution as one or more of the elements is 0" }
        return sol
    }

    /**
     * @param swaps How many swaps to do. If negative the number of swaps will be [calculateSolutionLength]
     */
    fun generateRandomSolution(swaps: Int = -1, r: Random = Random()): IntArray {
        val solution: IntArray = generateStandardSolution()
        val swaps0 = if (swaps < 0) solution.size * 2 else swaps
        debug { "swapping $swaps0 times" }

        //randomize the order till the solution is valid
        do {
            for (i in 0..r.nextInt(swaps0)) {
                val first = r.nextInt(solution.size)
                val second = r.nextInt(solution.size)
                //swap the two elements, yes this is kotlin magic
                solution[first] = solution[second].also { solution[second] = solution[first] }
            }
        } while (!checkValidity(solution))

        println("after swap = ${solution.toList()}")

        return solution
    }

    /**
     * Check if a solution is valid (but not necessarily feasible)
     *
     * A solution is valid if for each subsection (split by [BARRIER_ELEMENT]) there are two of each number
     *
     * This assumes that the given array does not have more than two identical numbers (excluding [BARRIER_ELEMENT])
     *
     */
    fun checkValidity(sol: IntArray): Boolean {
        for (sub in splitToSubArray(sol)) {
            //make sure the sub array actually have an even number of elements

            //sets cannot contain duplicate elements so if the size of the set for this subarray is not
            // exactly half size of the original set it contains duplicate elements
            if (sub.size % 2 != 0 || sub.toHashSet().size * 2 != sub.size) {
                return false
            }
        }
        return true
    }

    fun splitToSubArray(sol: IntArray): Array<IntArray> {
        //get the indices the barrier elements are located at
        val barrierIndices = sol.mapIndexed { index, i -> Pair(index, i) }.filter { (_, i) -> i == BARRIER_ELEMENT }
            .map { (index, _) -> index }.toIntArray()
        debug { "Found barrier elements at ${barrierIndices.toList()}" }

        check(barrierIndices.size == data.nrOfVessels) { "Number of barriers found does not match the expected amount" }

        return Array(data.nrOfVessels + 1) {
            val from = if (it > 0) barrierIndices[it - 1] + 1 else 0 //start at 0 for the first range
            val to =
                if (it != barrierIndices.size) barrierIndices[it] else sol.size //end at length - 1for last range
            val a = sol.copyOfRange(from, to)
            println("range from $from to $to: ${a.toList()}")
            return@Array a
        }
    }

    /**
     * Join a split array back into the original
     */
    fun joinToArray(original: IntArray, merge: Array<IntArray>) {
        var offset = 0
        for (ints in merge) {
            ints.copyInto(original, offset)
            offset += ints.size + 1
            if (offset != original.size) { //after the last array we do not want to add a barrier element
                original[offset++] = BARRIER_ELEMENT
            }
        }
    }

    fun checkFeasibility(original: IntArray): Boolean {

        val subroutes: Array<IntArray> = splitToSubArray(original)

        for ((index, sub) in subroutes.withIndex()) {

            //skip last array as it is only the tramp transports, and always allowed
            if (index == subroutes.size - 1) continue

            //false if we are currently picking it up, true if we are delivering
            val seen = BooleanArray(data.nrOfCargo)

            val vesselId = index + 1
            val vessel = data.vesselFromId(vesselId)
            var currWeight = 0
            var currTime = 0
            var lastPort = HOME_PORT //vessel start at home port


            for (cargoId in sub) {
                val cargoIndex = cargoId - 1
                val cargo = data.cargoes[cargoIndex]

                val currPort = if (seen[cargoIndex]) cargo.destPort else cargo.origin_port
                val vc: VesselCargo = data.vesselCargo[Pair(vesselId, cargoId)] ?: VesselCargo.IncompatibleVesselCargo

                //substitute the dummy home port id with the vessels actual homeport
                if (lastPort == HOME_PORT) {
                    lastPort = vessel.homePort
                }

                //add the sailing time to the current time
                currTime += data.archs[Triple(vesselId, lastPort, currPort)]!!.time

                if (!seen[cargoIndex]) {
                    seen[cargoIndex] = true

                    //check compatibility, but only do so for first encounter
                    if (!vessel.canTakeCargo(cargoId)) {
                        debug { "Vessel $vesselId is not compatible with $cargoId" }
                        return false
                    }
                    currWeight += cargo.size //first encounter, load the cargo

                    //check for cargo pickup time

                    currTime = checkTime(cargo.lowerPickup, cargo.upperPickup, vc.originPortTime, currTime)
                    if (currTime < 0) {
                        debug { "We are trying to pickup the cargo $cargoId after upper pickup time" }
                    }

                } else {
                    currWeight -= cargo.size //second encounter, unload the cargo

                    //check for cargo delivery time
                    currTime = checkTime(cargo.lowerDelivery, cargo.upperDelivery, vc.destPortTime, currTime)
                    if (currTime < 0) {
                        debug { "We are trying to deliver the cargo $cargoId after upper delivery time" }
                    }

                }

                //check that we are not overloaded
                if (currWeight > vessel.capacity) {
                    debug { "Invalid as vessel $vesselId is trying to carry more than it has capacity for ($currWeight > ${vessel.capacity})" }
                    return false
                }


                //update port for next round
                lastPort = currPort
            }
        }

        return true
    }


    /**
     *
     *
     * @return negative number of not valid, new current time if valid
     */
    private fun checkTime(lowerTime: Int, upperTime: Int, portTime: Int, currTime: Int): Int {
        var time = currTime
        //check for cargo pickup time

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

    fun calculateObjectiveFunction(): Int {
        TODO()
    }

    /**
     * Read an internal file to text
     *
     * @return The content of the file or `null` if the file cannot be read
     */
    private fun readInternalFile(path: String = FALLBACK_FILE): String? {
        return Main::class.java.classLoader.getResource(path)?.readText()
    }
}

fun main(args: Array<String>) {
    Main.init(args)
}
