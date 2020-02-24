package no.uib.inf273.operators

import no.uib.inf273.Main
import no.uib.inf273.extra.randomizeWithin
import no.uib.inf273.processor.Solution

/**
 * Swap two elements within a vessel range
 */
object TwoExchangeOperator : Operator {

    override fun operate(sol: Solution) {

        val barriers: List<Pair<Int, Int>> = sol.getVesselRanges()
        var vIndex: Int
        var from: Int
        var until: Int


        do {
            vIndex = Main.rand.nextInt(barriers.size - 1)
            //two indices where the random value between them will always be within a vessel
            val (t, f) = barriers[vIndex]
            from = t
            until = f

            // Select a subarray that has more than two elements, if less there are no way to change the objective value
            //FIXME an edge-case where every vessel have less than two cargoes, ie if we have more vessels than cargoes
        } while (until - from > 2)


        val sub: IntArray = sol.arr.copyOfRange(from, until)

        //Number of combinations is `(length of sub / 2)!`, it's a bit big so after size of 10 ish we just tries 10k times
        // Note that the minimum size is 4 due to the barrier selection above and increases by two each time as a cargo
        // contains to element in the array.
        //
        //We have this method as we do not want to iterate too much for the smaller sizes
        val maxTries = when (sub.size) {
            4 -> 24
            6 -> 120
            8 -> 720
            10 -> 5040
            else -> 10_000
        }

        var tryNr = 1

        do {
            //we're out of tries do nothing
            if (tryNr++ <= maxTries) {
                return
            }

            sub.randomizeWithin()
        } while (!sub.contentEquals(sol.arr) && !sol.isVesselFeasible(vIndex, sub))

        sub.copyInto(sol.arr, until)
    }
}
