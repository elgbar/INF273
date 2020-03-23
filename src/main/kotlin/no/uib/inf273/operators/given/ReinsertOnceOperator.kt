package no.uib.inf273.operators.given

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.extra.randomizeExchange
import no.uib.inf273.operators.Operator
import no.uib.inf273.processor.Solution

/**
 * An operator that picks two unique vessels (including freights) then moves cargo from the origin vessel to the destination vessel.
 */
object ReinsertOnceOperator : Operator() {


    override val log = Logger()

    override fun operate(sol: Solution) {
        val sub = sol.splitToSubArray(true)

        //select two vessels where the origin vessel have cargoes
        val (orgVesselIndex, destVesselIndex) = selectTwoRandomVessels(sub)

        //pick a random cargo within the origin vessel
        val cargo = sub[orgVesselIndex].random(Main.rand)

        moveCargo(sol, sub, orgVesselIndex, destVesselIndex, cargo) {
            it.randomizeExchange()
        }
    }
}
