package no.uib.inf273.search

import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator

object RandomSearch : Search() {

    override fun search(sol: Solution, iterations: Int): Solution {

        val data = sol.data
        val gen = SolutionGenerator(data)

        //Best known solution
        val best = Solution(data, sol.arr.clone())
        //objective value of the best known solution
        var bestObjVal = best.objectiveValue(false)

        log.debugs {
            listOf(
                "Initial solution is ${best.arr.contentToString()}",
                "Initial obj val is $bestObjVal"
            )
        }

        for (i in 0 until iterations) {

            val curr = gen.generateRandomSolution()
            if (curr.isFeasible(false)) {
                val newObjVal = curr.objectiveValue(modified = false)
                if (bestObjVal < newObjVal) {
                    curr.arr.copyInto(best.arr)
                    log.debug { "New best answer ${best.arr.contentToString()} with objective value $newObjVal. Diff is  ${newObjVal - bestObjVal} " }
                    bestObjVal = newObjVal
                }
            }

        }
        return best
    }

    override fun tune(solgen: SolutionGenerator, iterations: Int, report: Boolean) {
        //do nothing all is random
    }
}
