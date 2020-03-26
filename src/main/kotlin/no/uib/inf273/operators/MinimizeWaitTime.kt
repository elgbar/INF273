package no.uib.inf273.operators

import no.uib.inf273.extra.randomizeExchange
import no.uib.inf273.processor.Solution

/**
 * Minimize the cargo that is waiting the longest globally.This operators find the cargo which is currently waiting for port opening the longest and to and change the vessel cargo order in such a way the the maximum wait time is lower.
 *
 * @author Elg
 */
object MinimizeWaitTime : Operator() {

    override fun operate(sol: Solution) {
        val subs = sol.splitToSubArray(true)

        //find the vessel with the greatest waiting time
        // note that we do not select the vessel with the greatest average waiting time just the global max waiting time
        val vesselMeta = subs.mapIndexed() { index, sub ->
            sol.generateVesselRouteMetadata(index, sub)
        }.maxBy {
            it.portTardiness.max() ?: -1
        } ?: error("Failed to find a vessel")

        var maxTardiness = vesselMeta.portTardiness.max()
        if (maxTardiness == null) {
            log.debug { "No cargoes to optimize" }
            return
        }

        val vIndex = vesselMeta.vesselIndex

        //find what cargo is waiting the longest
        val sub = subs[vIndex]

        val maxTries = 20
        var tryNr = 0

        val solCopy = sol.copy()

        //then try and minimize the waiting time
        do {
            if (operateVesselTilFeasible(solCopy, vIndex, sub) {
                    
                    it.randomizeExchange()
                }) {
                val newMeta = solCopy.generateVesselRouteMetadata(vIndex, sub)
                val newMaxTardiness = newMeta.portTardiness.max()
                    ?: error("Failed to calculate max new tardiness for ${newMeta.portTardiness}")

                if (newMaxTardiness < maxTardiness) {
                    log.debug { "New smaller maximum tardiness found! new = $newMaxTardiness, old = $maxTardiness" }
                    break
                }
            }
        } while (tryNr++ < maxTries)
    }
}
