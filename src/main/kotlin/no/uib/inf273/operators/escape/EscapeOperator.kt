package no.uib.inf273.operators.escape

import no.uib.inf273.operators.Operator
import no.uib.inf273.processor.Solution

/**
 * Escape from a local optima by using escape operators designed to make very large changes, not necessarily making it better.
 *
 * The difference between this and an normal operator is that this guarantees feasibility after running [operate]
 */
abstract class EscapeOperator : Operator() {

    /**
     * When returning the solution must be feasible
     *
     * @see operate
     */
    abstract fun escape(sol: Solution)

    final override fun operate(sol: Solution) {
        escape(sol)
        require(sol.isFeasible(modified = true, checkValid = true)) {
            "Operator not feasible after trying to scape"
        }
    }
}
