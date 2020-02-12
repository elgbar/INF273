package no.uib.inf273.search

import no.uib.inf273.Logger.debug
import no.uib.inf273.Main.Companion.rand
import no.uib.inf273.operator.Operator
import no.uib.inf273.processor.Solution

/**
 * Modified local search for assignment 3
 */
object LocalSearchA3 : Search {

    var p1: Float = 0.25f
    var p2: Float = 0.5f

    override fun search(sol: Solution, iterations: Int): Solution {
        require(0 <= p1 && p1 < p2 && p2 + p1 < 1) {
            "Invalid probabilities. They must be in acceding order and in range [0,1). | p1=$p1, p2=$p2"
        }
        require(0 < iterations) { "Iteration must be a positive number" }

        //Best known solution
        val best = Solution(sol.data, sol.arr.clone())
        //objective value of the best known solution
        var bestObjVal = best.objectiveValue(false)

        //current solution
        val curr = sol
        var currObjVal: Int

        for (i in 0 until iterations) {
            val rsi = rand.nextFloat()
            val op = when {
                rsi < p1 -> Operator.TwoExchangeOperator
                rsi < p1 + p2 -> Operator.TreeExchangeOperator
                else -> Operator.ReinsertOnceOperator
            }

            debug { "Using op ${op.javaClass.simpleName}" }

            //copy the best solution to the current solution
            // this avoids allocating new objects or memory
            best.arr.copyInto(curr.arr)

            op.operate(curr)
            currObjVal = curr.objectiveValue(false)

            //update when better
            //TODO do not do the feasibility check here but in the operator
            if (curr.isFeasible() && currObjVal < bestObjVal) {
                debug { "New best answer ${best.arr.contentToString()} with objective value $currObjVal. Diff is  ${currObjVal - bestObjVal} " }
                curr.arr.copyInto(best.arr)
                bestObjVal = currObjVal
            }
        }
        return best
    }


}
