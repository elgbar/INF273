package no.uib.inf273.operators.given

import no.uib.inf273.operators.Operator
import no.uib.inf273.processor.Solution

/**
 * An operator that picks two unique vessels (including spot carriers) then moves cargo from the origin vessel to the destination vessel.
 */
class ReinsertOnceOperator(
    private val discourageSpotCarrierPercent: Double = 0.0,
    private val maxCargoesToBruteForce: Int = DEFAULT_MAX_CARGOES_IN_VESSEL_TO_USE_EXACT_APPROACH
) : Operator() {

    companion object {
        val INST = ReinsertOnceOperator()
    }

    override fun operate(sol: Solution) {
        val sub = sol.splitToSubArray(true)

        var cargo: Int
        var indices: Pair<Int, Int>
        do {
            //select two vessels where the origin vessel have cargoes
            indices = selectTwoRandomVessels(sub, discourageSpotCarrierPercent)

            //pick a random cargo within the origin vessel
            cargo = randomCargo(sub, indices.first)
        } while (!sol.data.canVesselTakeCargo(indices.second, cargo))

        val (orgVesselIndex, destVesselIndex) = indices

        if (moveCargo(sol, sub, orgVesselIndex, destVesselIndex, cargo, maxCargoesToBruteForce)) {
            log.debug {
                "Cargo ${sol.data.cargoFromId(cargo)} can be moved from $orgVesselIndex " +
                        "to dest $destVesselIndex"
            }
        } else {
            log.debug { "No move made" }
        }
    }
}
