package no.uib.inf273.search

import no.uib.inf273.processor.Solution

interface Search {

    /**
     * Search with the given search algorithm, no guarantee is given towards the initial solution [sol] being modified
     *
     * @throws IllegalArgumentException If [iterations] is a non-positive number
     */
    fun search(
        sol: Solution,
        iterations: Int = 10_000
    ): Solution

    object NoSearch : Search {

        override fun search(sol: Solution, iterations: Int): Solution {
            error("Cannot use the No search object to search. This is just a dummy implementation")
        }
    }
}
