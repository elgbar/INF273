package no.uib.inf273.operators.given

import no.uib.inf273.Main
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

        var cargo: Int
        var indices: Pair<Int, Int>
        do {
            //select two vessels where the origin vessel have cargoes
            indices = selectTwoRandomVessels(sub, discourageFreightPercent)

            //pick a random cargo within the origin vessel
            cargo = sub[indices.first].random(Main.rand)
        } while (!sol.data.canVesselTakeCargo(indices.second, cargo))

        val orgVesselIndex = indices.first
        val destVesselIndex = indices.second

        if (moveCargo(sol, sub, orgVesselIndex, destVesselIndex, cargo)) {
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
