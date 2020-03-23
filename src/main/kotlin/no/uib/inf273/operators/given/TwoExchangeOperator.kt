package no.uib.inf273.operators.given

import no.uib.inf273.Logger
import no.uib.inf273.operators.Operator
import no.uib.inf273.processor.Solution

/**
 * Swap two elements within a vessel range
 */
object TwoExchangeOperator : Operator() {

    override val log = Logger()

    override fun operate(sol: Solution) {
        val (vIndex, from, until) = sol.findNonEmptyVessel(true) ?: return
        val sub = sol.arr.copyOfRange(from, until)
        val feasible =
            exchangeOnceTilFeasible(sol, vIndex, sub)
        if (feasible)
            sub.copyInto(sol.arr, from)
    }
}
