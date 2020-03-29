package no.uib.inf273.search

import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator

/**
 * An algorithm based an simulated annealing and performance of operators
 *
 * We have two sets of operators diversification (d-ops) and intensification (i-ops) operators.
 * To choose if we want to use d or i ops we use the current temperature (from simulated annealing)
 * To the weight of each d-op and i-op is based on the performance of each of the operators.
 *
 * There should be a small cache that disallows recently seen solution (like in tabu), implementation can be a fixed stack of the 100 or so hashes of the solution array
 *
 * ## Specific to the implementation
 * There is a special i-op that is used when the number of cargoes in the chosen vessel is 1 <= cargoes <= `n` that simply brute
 * forces the best solution. Where `n` should be around 5 (ish complexity is `(n*2)!` as vessels have `2 * cargoes` elements)
 *
 * @author Elg
 */
class A1 : Algorithm() {
    override fun search(sol: Solution, iterations: Int): Solution {
        TODO("not implemented")
    }

    override fun tune(solgen: SolutionGenerator, iterations: Int, report: Boolean) {
        TODO("not implemented")
    }

}
