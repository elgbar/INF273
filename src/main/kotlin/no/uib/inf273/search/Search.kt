package no.uib.inf273.search

import no.uib.inf273.processor.Solution
import kotlin.random.Random

interface Search {


    /**
     * Random to be used within the search
     */
    val rand: Random
        get() = Random.Default

    fun search(
        initSolution: Solution,
        iterations: Int = 10_000,
        p1: Float = 0.0f,
        p2: Float = 0.4f,
        p3: Float = 1 - p1 - p2
    ): Solution

}
