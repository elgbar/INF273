package no.uib.inf273.operators

import no.uib.inf273.Logger
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
//        val cargoIndex = Main.rand.nextInt(sub[orgVesselIndex].size)
//        val elem = sub[orgVesselIndex][cargoIndex]

        val elem = sub[orgVesselIndex].random(Main.rand)

        //add both at the end of the new vessel. Do this first as the new destination vessel might not be feasible

        //Again the destination array needs to be two elements larger to allow the elements from origin to be here
        val destNew = sub[destVesselIndex].copyOf(sub[destVesselIndex].size + 2)
        //we place the new elements in the back
        destNew[destNew.size - 1] = elem
        destNew[destNew.size - 2] = elem
        while (!sol.isVesselFeasible(destVesselIndex, destNew)) {
            //the new vessel is not feasible shuffle it around til it is feasible
            val feasible = Operator.shuffleVesselTilFeasible(sol, destVesselIndex, destNew)
            if (!feasible) {
                Logger.trace { "Failed to reinsert $elem from vessel $orgVesselIndex to $destVesselIndex as no feasible arrangement could be found" }
            }
            return
        }

        //remove cargo from the original vessel

        //create new array with two less elements as they will no longer be here
        val orgNew = IntArray(sub[orgVesselIndex].size - 2)
        sub[orgVesselIndex].filter(elem, orgNew)
        sub[orgVesselIndex] = orgNew



        sub[destVesselIndex] = destNew

        sol.joinToArray(sub)

        //TODO Ensure the solution is feasible
    }
}
