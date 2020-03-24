package no.uib.inf273.operators.given

import no.uib.inf273.Main
import no.uib.inf273.extra.randomizeExchange
import no.uib.inf273.operators.Operator
import no.uib.inf273.processor.Solution

/**
 * An operator that picks two unique vessels (including freights) then moves cargo from the origin vessel to the destination vessel.
 */
class ReinsertOnceOperator(private val discourageFreightPercent: Double = 0.0) : Operator() {

    companion object {
        val INST = ReinsertOnceOperator()
    }

    override fun operate(sol: Solution) {
        val sub = sol.splitToSubArray(true)

        //select two vessels where the origin vessel have cargoes
        val (orgVesselIndex, destVesselIndex) = selectTwoRandomVessels(sub, discourageFreightPercent)

        //pick a random cargo within the origin vessel
        val cargo = sub[orgVesselIndex].random(Main.rand)

        if (moveCargo(sol, sub, orgVesselIndex, destVesselIndex, cargo) {
                it.randomizeExchange()
            }) {
            log.debug {
                "Cargo ${sol.data.cargoFromId(cargo)} can be moved from $orgVesselIndex " +
                        "to dest $destVesselIndex"
            }
        } else {
            log.debug {
                "No move made"
            }
        }
    }
}
