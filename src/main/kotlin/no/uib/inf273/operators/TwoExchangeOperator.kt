package no.uib.inf273.operators

import no.uib.inf273.Main
import no.uib.inf273.extra.randomizeWithin
import no.uib.inf273.processor.Solution

/**
 * Swap two elements within a vessel range
 */
object TwoExchangeOperator : Operator {

    override fun operate(sol: Solution) {

        val barriers = sol.getVesselRanges()
        //two indices where the random value between them will always be within a vessel
        // do not pick the last array as changing that has not impact on the objective value
        val (from, until) = barriers[Main.rand.nextInt(barriers.size - 1)]

        sol.arr.randomizeWithin(from, until, Main.rand)
    }
}
