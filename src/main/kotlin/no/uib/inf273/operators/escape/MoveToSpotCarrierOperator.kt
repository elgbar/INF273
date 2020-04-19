package no.uib.inf273.operators.escape

import no.uib.inf273.Main
import no.uib.inf273.operators.Operator
import no.uib.inf273.processor.Solution
import kotlin.math.min

/**
 * Free space in vessels by moving cargoes back to the spot carrier. It is the opposite of [no.uib.inf273.operators.MinimizeNotTransported]
 *
 * This is an escape operator.
 * @author Elg
 */
object MoveToSpotCarrierOperator : Operator() {

    override fun operate(sol: Solution) {
        val sub = sol.splitToSubArray(true)

        val n = Main.rand.nextInt(1, min(4, sol.data.nrOfCargo))
        for (i in 0..n) {
            val vesselIndex = selectRandomVessel(sub, 1, false)
            if (vesselIndex == INVALID_VESSEL) {
                //this probably will never happen, as to move cargo out of spot is the easiest way of lowering obj val
                // but let's be on the safe side.
                return
            }

            val cargo = randomCargo(sub, vesselIndex)
            moveCargo(
                sol,
                sub,
                vesselIndex,
                sol.data.dummyVesselIndex,
                cargo,
                DEFAULT_MAX_CARGOES_IN_VESSEL_TO_USE_EXACT_APPROACH - 1 //less as we do this a lot
            )
        }
    }
}
