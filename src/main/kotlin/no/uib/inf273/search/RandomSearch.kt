package no.uib.inf273.search

import no.uib.inf273.Logger.debug
import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator

object RandomSearch : Search {

    var swaps: Int = -1

    override fun search(sol: Solution, iterations: Int): Solution {

        val data = sol.data
        val gen = SolutionGenerator(data)

        //Best known solution
        val best = Solution(data, sol.arr.clone())
        //objective value of the best known solution
        var bestObjVal = best.objectiveValue(false)

        debug {
            "Initial solution is ${best.arr.contentToString()}\n" +
                    "\"Initial obj val is $bestObjVal\""
        }

        for (i in 0 until iterations) {

            val curr = gen.generateRandomSolution()

            if (curr.isFeasible(modified = false, checkValid = false)) {
                val newObjVal = curr.objectiveValue(modified = false)
                if (bestObjVal < newObjVal) {
                    curr.arr.copyInto(best.arr)
                    debug { "New best answer ${best.arr.contentToString()} with objective value $newObjVal. Diff is  ${newObjVal - bestObjVal} " }
                    bestObjVal = newObjVal
                }
            }
        }
        return best
    }
}
