package no.uib.inf273.processor

import no.uib.inf273.Logger
import no.uib.inf273.data.Arch
import no.uib.inf273.data.Cargo
import no.uib.inf273.data.Vessel
import no.uib.inf273.data.VesselCargo
import kotlin.math.abs

class DataParser(content: String) {

    val nrOfNodes: Int
    val nrOfCargo: Int
    val nrOfVessels: Int


    val vessels: Array<Vessel>
    val cargoes: Array<Cargo>

    /**
     * pair in order: vessel, cargo
     */
    val vesselCargo: Map<Pair<Int, Int>, VesselCargo>

    /**
     * Triple in order: vessel, origin, destination
     */
    val archs: Map<Triple<Int, Int, Int>, Arch>

    init {

        val time = System.currentTimeMillis()
        var currLine = 0

        //list of all non-comment lines
        val lines = content.replace("\r", "").split("\n").filter { !it.startsWith('%') }

        nrOfNodes = lines[currLine++].toInt()
        log.debug { "nrOfNodes= $nrOfNodes" }

        nrOfVessels = lines[currLine++].toInt()
        log.debug { "nrOfVessels= $nrOfVessels" }

        vessels = Array(nrOfVessels) {
            val line = lines[currLine++].split(',').map { it.toInt() }
            log.trace { "vessel $it= $line" }
            val id = line[0]
            val home = line[1]
            if (home <= 0 || home > nrOfNodes) {
                throw IllegalArgumentException("Invalid node $home, Min is 1 (inc) max is $nrOfNodes (inc)")
            }
            val start = line[2]
            val cap = line[3]
            Vessel(id, home, start, cap)
        }

        nrOfCargo = lines[currLine++].toInt()
        log.debug { "nrOfCalls= $nrOfCargo" }

        for (i in 1..nrOfVessels) {
            val line = lines[currLine++].split(',').map { it.toInt() }
            log.trace { "vessel cargo compat $i = $line" }

            val vessel = vesselFromId(line[0])
            vessel.compatibleCalls = line.subList(1, line.size).toSet()
        }

        cargoes = Array(nrOfCargo) {
            val line = lines[currLine++].split(',').map { it.toInt() }
            log.trace { "cargo $it= $line" }
            val index = line[0]
            val origin = line[1]
            val dest = line[2]
            val size = line[3]
            val ntCost = line[4]
            val lowerPickup = line[5]
            val upperPickup = line[6]
            val lowerDelivery = line[7]
            val upperDelivery = line[8]
            Cargo(index, origin, dest, size, ntCost, lowerPickup, upperPickup, lowerDelivery, upperDelivery)
        }


        archs = HashMap()

        for (i in 1..(nrOfVessels * nrOfNodes * nrOfNodes)) {
            val line = lines[currLine++].split(',').map { it.toInt() }

            val vessel = line[0]
            val origin = line[1]
            val dest = line[2]
            val index = Triple(vessel, origin, dest)

            log.trace { "arch [$index] = ${line.subList(3, line.size)}" }

            val archTime = line[3]
            val archCost = line[4]

            archs[index] = Arch(vessel, origin, dest, archTime, archCost)
        }

        vesselCargo = HashMap()

        for (i in 1..(nrOfCargo * nrOfVessels)) {
            val line = lines[currLine++].split(',').map { it.toInt() }
            val vessel = line[0]
            val call = line[1]
            val index = Pair(vessel, call)

            val originTime = line[2]
            log.trace { "vcc [$index] = ${line.subList(2, line.size)}" }

            vesselCargo[index] = if (originTime == INCOMPATIBLE) {
                VesselCargo.incompatibleVesselCargo
            } else {
                val originCost = line[3]
                val destTime = line[4]
                val destCost = line[5]
                VesselCargo(originTime, originCost, destTime, destCost)
            }
        }

        //make sure we read the whole file correctly
        check(currLine == lines.size) {
            "Finished loading file, but current line is $currLine while number of lines in the files is ${lines.size}"
        }

        log.log { "Successfully parsed ${lines.size} lines of data in ${System.currentTimeMillis() - time} ms" }
    }

    private val simCache: MutableMap<Vessel, Map<Pair<Int, Int>, Double>> = HashMap()

    /**
     * Calculate how similar cargoes are when transported with the given vessel. Any cargo that is not compatiple with the vessel will not be calculated.
     * The returned lis.
     *
     * @return A list of how similar two cargoes the value is normalized to be between `[0, 1]`
     */
    private fun calcSimilarity(vessel: Vessel): Map<Pair<Int, Int>, Double> {

        //Distance between each cargo for for the given vessel
        val dataMap = HashMap<Pair<Int, Int>, Pair<Int, Int>>()


        for (c1 in cargoes) {
            if (!vessel.canTakeCargo(c1.id)) continue
            for (c2 in cargoes) {
                if (c1 >= c2 || !vessel.canTakeCargo(c2.id)) continue

                val distance = archs[Triple(vessel.id, c1.originPort, c2.originPort)]!!.time +
                        archs[Triple(vessel.id, c1.destPort, c2.destPort)]!!.time

                val timeWindow = abs(c1.lowerPickup - c2.lowerPickup) + abs(c1.upperPickup - c2.upperPickup) +
                        abs(c1.lowerDelivery - c2.lowerDelivery) + abs(c1.upperDelivery - c2.upperDelivery)

                dataMap[c1.id to c2.id] = distance to timeWindow
            }
        }

        val maxDist = dataMap.values.map { it.first }.max()!!.toDouble()
        val maxTimeWindow = dataMap.values.map { it.second }.max()!!.toDouble()

        return dataMap.mapValues { (_, it) ->
            //normalize each component individually
            val normalizedDist = it.first / maxDist
            val timeWindowPickup = it.second / maxTimeWindow

            //then normalize all components
            // distance have equal weight as the two
            (normalizedDist + timeWindowPickup) / 2
        }
    }

    /**
     * The similarity score for the given route of the vessel [vIndex].
     *
     * @return A value in range `[0, 1]` where `0` means the cargo is perfectly similar and `1` means the most dissimilar of all cargoes
     */
    fun getRouteSimilarityScore(vIndex: Int, sub: IntArray): Double {
        return getRouteSimilarity(vIndex, sub).values.average()
    }

    /**
     *
     * @see getRouteSimilarityScore
     */
    fun getRouteSimilarity(vIndex: Int, sub: IntArray): Map<Pair<Int, Int>, Double> {
        val cargoes = sub.toSet()
        return getSimilarityMap(vIndex).filterKeys { (c1, c2) -> cargoes.contains(c1) && cargoes.contains(c2) }
    }

    /**
     * @return Map of pairs of cargoes mapped to how similar they are
     */
    fun getSimilarityMap(vIndex: Int): Map<Pair<Int, Int>, Double> {
        return getSimilarityMap(vessels[vIndex])
    }

    /**
     * @return Map of pairs of cargoes mapped to how similar they are
     */
    fun getSimilarityMap(vessel: Vessel): Map<Pair<Int, Int>, Double> {
        return simCache.getOrPut(vessel) { calcSimilarity(vessel) }
    }

    /**
     * @returnList of most to least similar cargo pair
     */
    fun getSortedSimilarityList(vIndex: Int): List<Pair<Int, Int>> {
        return getSimilarityMap(vessels[vIndex]).toList().sortedBy { it.second }.map { it.first }
    }


    /**
     * @return A value in range `[0, 1]` where `0` means identical and `1` means the most dissimilar of all cargoes
     */
    fun getSimilarity(vIndex: Int, cargoIdA: Int, cargoIdB: Int): Double {
        val vessel = vessels[vIndex]
        if (!vessel.canTakeCargo(cargoIdA) || !vessel.canTakeCargo(cargoIdB)) {
            error("Vessel $vIndex cannot take cargoes $cargoIdA and/or $cargoIdB")
        }
        val sim = getSimilarityMap(vessel)

        return when {
            cargoIdA == cargoIdB -> 0.0
            cargoIdA > cargoIdB -> sim[cargoIdB to cargoIdA]!!
            else -> sim[cargoIdA to cargoIdB]!!
        }
    }

    fun vesselFromId(id: Int): Vessel {
        return vessels[id - 1]
    }

    fun cargoFromId(id: Int): Cargo {
        return cargoes[id - 1]
    }

    fun canVesselTakeCargo(vIndex: Int, cargoId: Int): Boolean {
        return isDummyVessel(vIndex) || vessels[vIndex].canTakeCargo(cargoId)
    }

    fun isDummyVessel(vIndex: Int): Boolean {
        return vIndex == dummyVesselIndex
    }

    val dummyVesselIndex = nrOfVessels

    fun getArch(vesselId: Int, orgId: Int, destId: Int): Arch {
        return archs[Triple(vesselId, orgId, destId)]
            ?: error("Failed to get arch for vessel id $vesselId from $orgId to $destId")
    }


    /**
     * @return Map of vessel id with a list of the closest cargoes
     */
    fun cluserize(vIndex: Vessel): Map<Int, List<Int>> {


        /**
         * @return How similar two cargoes are in the context of [vessel]. If [INCOMPATIBLE] the cargo is incompatible with the vessel
         */
        fun similarity(vessel: Vessel, cargoId: Int): Int {
            if (!vessel.canTakeCargo(cargoId)) return INCOMPATIBLE

            val cargoA = cargoFromId(cargoId)

            return getArch(vessel.id, vessel.homePort, cargoA.originPort).cost +
                    getArch(vessel.id, vessel.homePort, cargoA.destPort).cost
        }

        val map = HashMap<Int, List<Int>>()

        for (vessel in vessels) {

        }
        return map
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
        return nrOfCargo * ELEMENTS_PER_CARGO + nrOfVessels
    }

    companion object {
        val log = Logger("Parser")
        const val INCOMPATIBLE = -1

        /**
         * How many elements there is in the vessel arrays per cargo
         */
        const val ELEMENTS_PER_CARGO = 2
    }
}
