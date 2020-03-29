package no.uib.inf273.operators

import no.uib.inf273.extra.randomizeExchange
import no.uib.inf273.processor.Solution

/**
 * Minimize the cargo that is waiting the longest globally.
 * This operators find the cargo which is currently waiting for port opening the longest and to and change the vessel cargo
 * order in such a way the the maximum wait time is lower.
 *
 * This operator helps with intensification as it optimizes a vessels cargo route.
 *
 * @author Elg
 */
object MinimizeWaitTime : Operator() {

    override fun operate(sol: Solution) {
        val subs = sol.splitToSubArray(true)

        //find the vessel with the greatest waiting time
        // note that we do not select the vessel with the greatest average waiting time just the global max waiting time
        val vesselMeta = subs.mapIndexed() { index, sub ->
            sol.generateVesselRouteMetadata(
                index,
                sub
            ) // generate all info we know about this vessel (objval, port tardiness, etc)
        }.filter { !sol.data.isDummyVessel(it.vesselIndex) && it.arr.size > 2 }.maxBy {
            it.portTardiness.max() ?: -1
        }

        if (vesselMeta == null) {
            //Nothing to do, vessel is either the dummy vessel or has zero or one cargo
            log.debug { "Failed to find a vessel to change" }
            return
        }

        val maxTardiness = vesselMeta.portTardiness.max() ?: error("Failed to find a cargo in vessel $vesselMeta")

        val vIndex = vesselMeta.vesselIndex

        //find what cargo is waiting the longest
        val sub = subs[vIndex]

        val maxTries = 20
        var tryNr = 0

        val solCopy = sol.copy()

        //then try and minimize the waiting time
        do {
            if (operateVesselTilFeasible(solCopy, vIndex, sub) { it.randomizeExchange() }) {
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
