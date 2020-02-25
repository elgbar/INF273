package no.uib.inf273.operators

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.extra.filter
import no.uib.inf273.extra.insert
import no.uib.inf273.processor.Solution

/**
 * An operator that picks two unique vessels (including freights) then moves cargo from the origin vessel to the destination vessel.
 */
object ReinsertOnceOperator : Operator {


    override val log = Logger()

    override fun operate(sol: Solution) {
        val sub = sol.splitToSubArray(true)

        //select two vessels where the origin vessel have cargoes
        var orgVesselIndex: Int
        var destVesselIndex: Int
        do {
            orgVesselIndex = Main.rand.nextInt(sub.size)
            destVesselIndex = Main.rand.nextInt(sub.size)
        } while (orgVesselIndex == destVesselIndex || sub[orgVesselIndex].isEmpty())

        //pick a random cargo within the origin vessel
        val elem = sub[orgVesselIndex].random(Main.rand)

        //add both at the end of the new vessel. Do this first as the new destination vessel might not be feasible

        //the destination array needs to be two element larger for the new cargo to fit
        val destNew = sub[destVesselIndex].copyOf(sub[destVesselIndex].size + 2)
        //we place the new elements in the back
        destNew[destNew.size - 1] = elem
        destNew[destNew.size - 2] = elem

        val destFeasible = Operator.exchangeOnceTilFeasible(sol, destVesselIndex, destNew)
        if (!destFeasible) {
            log.trace { "Failed to reinsert $elem from vessel $orgVesselIndex to $destVesselIndex as no feasible arrangement could be found" }
            return
        }
        sub[destVesselIndex] = destNew

        //remove cargo from the original vessel

        //create new array with two less elements as they will no longer be here
        val orgNew = sub[orgVesselIndex].filter(elem, IntArray(sub[orgVesselIndex].size - 2))

        val orgFeasible = Operator.exchangeOnceTilFeasible(sol, orgVesselIndex, orgNew)
        if (!orgFeasible) {
            log.trace { "Failed to reinsert $elem from vessel $orgVesselIndex to $orgVesselIndex as no feasible arrangement could be found" }
            return
        }
        sub[orgVesselIndex] = orgNew
        sol.joinToArray(sub)
    }
}
