package no.uib.inf273.operators

import no.uib.inf273.Main
import no.uib.inf273.extra.filter
import no.uib.inf273.processor.Solution

/**
 * An operator that picks two unique vessels (including freights) then moves cargo from the origin vessel to the destination vessel.
 */
object ReinsertOnceOperator : Operator {

    override fun operate(sol: Solution) {
        val sub = sol.splitToSubArray(false)

        //select two vessels where the origin vessel have cargoes
        var orgVesselIndex: Int
        var destVesselIndex: Int
        do {
            orgVesselIndex = Main.rand.nextInt(sub.size)
            destVesselIndex = Main.rand.nextInt(sub.size)
        } while (orgVesselIndex == destVesselIndex || sub[orgVesselIndex].isEmpty())

        //pick a random cargo within the origin vessel
        val cargoIndex = Main.rand.nextInt(sub[orgVesselIndex].size)

        val elem = sub[orgVesselIndex][cargoIndex]

        //remove cargo from the original vessel
        val orgNew = IntArray(sub[orgVesselIndex].size - 2)
        sub[orgVesselIndex].filter(elem, orgNew)
        sub[orgVesselIndex] = orgNew

        //then add both at the end of the new vessel
        val destNew = sub[destVesselIndex].copyOf(sub[destVesselIndex].size + 2)
        destNew[destNew.size - 1] = elem
        destNew[destNew.size - 2] = elem
        sub[destVesselIndex] = destNew

        sol.joinToArray(sub)

        //TODO Ensure the solution is feasible
    }
}
