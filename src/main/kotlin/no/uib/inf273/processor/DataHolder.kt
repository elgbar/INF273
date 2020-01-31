package no.uib.inf273.processor

import no.uib.inf273.Logger.debug
import no.uib.inf273.Logger.log
import no.uib.inf273.Logger.trace
import no.uib.inf273.data.Arch
import no.uib.inf273.data.Cargo
import no.uib.inf273.data.Vessel
import no.uib.inf273.data.VesselCargo

class DataHolder(content: String) {

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

        var currLine = 0

        //list of all non-comment lines
        val lines = content.replace("\r", "").split("\n").filter { !it.startsWith('%') }

        nrOfNodes = lines[currLine++].toInt()
        debug { "nrOfNodes= $nrOfNodes" }

        nrOfVessels = lines[currLine++].toInt()
        debug { "nrOfVessels= $nrOfVessels" }

        vessels = Array(nrOfVessels) {
            val line = lines[currLine++].split(',').map { it.toInt() }
            trace { "vessel $it= $line" }
            val id = line[0]
            val home = checkNodeRange(line[1])
            val start = line[2]
            val cap = line[3]
            Vessel(id, home, start, cap)
        }

        nrOfCargo = lines[currLine++].toInt()
        debug { "nrOfCalls= $nrOfCargo" }

        for (i in 1..nrOfVessels) {
            val line = lines[currLine++].split(',').map { it.toInt() }
            trace { "vessel cargo compat $i = $line" }

            val vessel = vesselFromId(line[0])
            vessel.compatibleCalls = line.subList(1, line.size).toIntArray()
        }

        cargoes = Array(nrOfCargo) {
            val line = lines[currLine++].split(',').map { it.toInt() }
            trace { "cargo $it= $line" }
            val index = line[0]
            val origin = line[1]
            val dest = line[2]
            val size = line[3]
            val ntCost = line[4]
            val lowerPickup = line[5]
            val upperPickup = line[6]
            val lowerDelivery = line[7]
            val upperDelivery = line[8]
            Cargo(
                index,
                origin,
                dest,
                size,
                ntCost,
                lowerPickup,
                upperPickup,
                lowerDelivery,
                upperDelivery
            )
        }


        val archmap = HashMap<Triple<Int, Int, Int>, Arch>()
        archs = archmap

        for (i in 1..(nrOfVessels * nrOfNodes * nrOfNodes)) {
            val line = lines[currLine++].split(',').map { it.toInt() }

            val vessel = line[0]
            val origin = line[1]
            val dest = line[2]
            val index = Triple(vessel, origin, dest)

            trace { "arch [$index] = ${line.subList(3, line.size)}" }

            val time = line[3]
            val cost = line[4]

            archmap[index] = Arch(vessel, origin, dest, time, cost)
        }

        debug {
            "arch: v 2, ori 38, dest 34: \n${archs[Triple(2, 38, 34)]}\nexpecting:\n2,38,34,72,48824"
        }

        val vcmap = HashMap<Pair<Int, Int>, VesselCargo>()
        vesselCargo = vcmap

        for (i in 1..(nrOfCargo * nrOfVessels)) {
            val line = lines[currLine++].split(',').map { it.toInt() }
            val vessel = line[0]
            val call = line[1]
            val index = Pair(vessel, call)

            val otime = line[2]

            val vcc = if (otime == -1) {
                VesselCargo.IncompatibleVesselCargo
            } else {
                val ocost = line[3]
                val dtime = line[4]
                val dcost = line[5]
                VesselCargo(otime, ocost, dtime, dcost)
            }
            trace { "vcc [$index] = ${line.subList(2, line.size)}" }
            vcmap[index] = vcc
        }

        //make sure we read the whole file correctly
        check(currLine == lines.size) {
            "Finished loading file, but current line is $currLine while number of lines in the files is ${lines.size}"
        }

        log { "Successfully parsed ${lines.size} lines of data (Comment excluded)" }

    }

    fun checkNodeRange(node: Int): Int {
        if (node <= 0 || node > nrOfNodes) {
            throw IllegalArgumentException("Invalid node $node, Min is 1 (inc) max is $nrOfNodes (inc)")
        }
        return node
    }

    fun vesselFromId(id: Int): Vessel {
        return vessels[id - 1]
    }

    fun cargoFromId(id: Int): Cargo {
        return cargoes[id - 1]
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
}
