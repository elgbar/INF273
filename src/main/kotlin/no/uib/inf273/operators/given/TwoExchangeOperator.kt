package no.uib.inf273.operators.given

import no.uib.inf273.extra.randomizeExchange
import no.uib.inf273.operators.Operator
import no.uib.inf273.processor.Solution

/**
 * Swap two elements within a vessel range
 */
object TwoExchangeOperator : Operator() {

    override fun operate(sol: Solution) {
        val (vIndex, from, until) = sol.findNonEmptyVessel(true) ?: return
        val sub = sol.arr.copyOfRange(from, until)
        val feasible = operateVesselTilFeasible(sol, vIndex, sub) { it.randomizeExchange() }
        //update solution if feasible
        if (feasible) {
            sub.copyInto(sol.arr, from)
        }
    }
}
