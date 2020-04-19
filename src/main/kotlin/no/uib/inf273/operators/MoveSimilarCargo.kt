package no.uib.inf273.operators

import no.uib.inf273.Main
import no.uib.inf273.processor.Solution

/**
 * Find cargoes that are similar (in origin and destination ports, and time frame) and move them into the same vessel.
 *
 * This operator helps with diversification as it moves to new vessels.
 *
 * @author Elg
 */
object MoveSimilarCargo : Operator() {

    override fun operate(sol: Solution) {
        val subs = sol.splitToSubArray(true)

        //TODO make this work for more and less cargoes
        //find all vessels with exactly one cargoes, shuffle to make more random!
        val vessels = findVessels(sol, subs, minCargoes = 1, maxCargoes = 1).shuffled(Main.rand)

        //TODO loop over all? for now select first
        val (vIndex, sub) = vessels.firstOrNull() ?: return

        val refCargo = sub[0]

        //first look for any cargoes in the dummy vessel
        val dummyVessel = subs.last().toSet()
        val dummyIndex = sol.data.dummyVesselIndex

        //list of cargoes that contain this refCargo
        val similarityList =
            sol.data.getSortedSimilarityList(vIndex).filter {
                //the existing cargo is one of the cargoes
                //The other cargo is within the dummy
                it.first == refCargo && dummyVessel.contains(it.second) ||
                        it.second == refCargo && dummyVessel.contains(it.first)
            }

        for ((c1, c2) in similarityList) {

            val cargo = if (c1 == refCargo) c2 else c1
            if (moveCargo(sol, subs, dummyIndex, vIndex, cargo)
            ) {
                log.debug { "Successfully moved cargo to $vIndex" }
//                break
            }
        }
        return
    }
}
