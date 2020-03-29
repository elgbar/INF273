package no.uib.inf273.operators

import no.uib.inf273.data.Cargo
import no.uib.inf273.processor.DataParser
import no.uib.inf273.processor.Solution

/**
 * Minimize the number of cargoes we use freight to transport.
 * Cost of not transporting is very high compared to even the worst route!
 * If we minimize number of cargoes we do not transport the cost will (hopefully) go down.
 *
 * This operator helps with diversification as it moves to new vessels
 */
object MinimizeNotTransported : Operator() {

    private val cache: MutableMap<DataParser, List<Cargo>> = HashMap()

    override fun operate(sol: Solution) {
        val subs = sol.splitToSubArray(true)
        val freights = subs.last().toSet()
        if (freights.isEmpty()) return //freight is empty nothing we can do

        val orgIndex = subs.size - 1 //origin is always dummy vessel

        val orgSubCpy = subs[orgIndex].clone()

        val allSortedNT = cache.computeIfAbsent(sol.data) {
            it.cargoes.toList().sortedByDescending { c -> c.ntCost }
        }
        val sortedCargoes = allSortedNT.filter { freights.contains(it.id) }
        //sort the cargoes by how much they cost of not transporting
//        val sortedCargoes = freights.toHashSet().sortedByDescending { sol.data.cargoFromId(it).ntCost }
        log.debug { "Can move ${sortedCargoes.size} cargoes from dummy!" }

        //we prefer to move cargoes to vessels with fewer cargoes on board
        val sortedVessels = sol.data.vessels.sortedBy { subs[it.index()].size }

        outer@
        for (cargo in sortedCargoes) {
            val cargoId = cargo.id

            val validDestVessels = sortedVessels.filter { it.canTakeCargo(cargoId) }
            for (vessel in validDestVessels) {
                val destIndex = vessel.index()

                //keep a copy of the destination in case we couldn't move the cargo there for some reason
                val destSubCpy = subs[destIndex].clone()

                //move the cargo to the new vessel
                val moved = moveCargo(sol, subs, orgIndex, destIndex, cargo.id)

                if (moved) {
                    log.trace { "Cargo $cargo can be moved from freight to dest $vessel" }
                    break@outer
                } else {
                    //we didn't move so the subarray needs to be restored
                    subs[orgIndex] = orgSubCpy.clone()
                    //no need to clone the destination as that will change for each iteration
                    subs[destIndex] = destSubCpy
                }
            }
        }
    }
}
