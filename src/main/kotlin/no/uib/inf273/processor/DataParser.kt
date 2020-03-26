package no.uib.inf273.processor

import no.uib.inf273.Logger
import no.uib.inf273.data.Arch
import no.uib.inf273.data.Cargo
import no.uib.inf273.data.Vessel
import no.uib.inf273.data.VesselCargo

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

            vesselCargo[index] = if (originTime == -1) {
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

    fun vesselFromId(id: Int): Vessel {
        return vessels[id - 1]
    }

    fun cargoFromId(id: Int): Cargo {
        return cargoes[id - 1]
    }

    fun canVesselTakeCargo(vIndex: Int, cargoId: Int): Boolean {
        return vIndex == nrOfVessels || vessels[vIndex].canTakeCargo(cargoId)
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
        return nrOfCargo * 2 + nrOfVessels
    }

    companion object {
        val log = Logger("Parser")
    }
}
