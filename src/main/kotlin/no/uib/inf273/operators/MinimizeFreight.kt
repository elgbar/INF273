package no.uib.inf273.operators

import no.uib.inf273.Main
import no.uib.inf273.extra.randomizeExchange
import no.uib.inf273.processor.Solution

/**
 * Minimize the number of cargoes we use freight to transport.
 * Cost of not transporting is very high compared to even the worst route!
 * If we minimize number of cargoes we do not transport the cost will (hopefully) go down
 */
object MinimizeFreight : Operator() {
    
    override fun operate(sol: Solution) {
        val subs = sol.splitToSubArray(true)
        val freights = subs.last()
        if (freights.isEmpty()) return //freight is empty nothing we can do

        val orgIndex = subs.size - 1 //origin is always dummy vessel

        val orgSubCpy = subs[orgIndex].clone()

        //sort the cargoes by how much they cost of not transporting
        val sortedCargoes = freights.toHashSet().sortedByDescending { sol.data.cargoFromId(it).ntCost }
        log.debug { "Can move ${sortedCargoes.size} cargoes from dummy!" }

        outer@
        for (cargo in sortedCargoes) {

            //is there anything we can sort this list by?
            // maybe use the arches somehow, we have the origin at least but not the destination (unless we say that we take it there directly)
            val validDestVessels = sol.data.vessels.filter { it.canTakeCargo(cargo) }.shuffled(Main.rand)
            for (vessel in validDestVessels) {
                val destIndex = vessel.index()

                //keep a copy of the destination in case we couldn't move the cargo there for some reason
                val destSubCpy = subs[destIndex].clone()

                //move the cargo to the new vessel
                val moved = moveCargo(sol, subs, orgIndex, destIndex, cargo) {
                    it.randomizeExchange()
                }

                if (moved) {
                    log.debug { "Cargo ${sol.data.cargoFromId(cargo)} can be moved from freight to dest $vessel" }
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
