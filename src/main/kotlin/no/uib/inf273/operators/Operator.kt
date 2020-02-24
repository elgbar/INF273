package no.uib.inf273.operators

import no.uib.inf273.processor.Solution

interface Operator {

    /**
     * Run the operator on the given solution. When returning the solution is guaranteed to be [Solution.isValid].
     *
     * @param sol A feasible solution.
     *
     */
    fun operate(sol: Solution)
}
