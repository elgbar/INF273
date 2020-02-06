package no.uib.inf273.search

import no.uib.inf273.operator.Operator
import no.uib.inf273.processor.Solution

object LocalSearch : Search {


    override fun search(initSolution: Solution, iterations: Int, p1: Float, p2: Float, p3: Float): Solution {
        require(0 <= p1 && p1 < p2 && p2 < p3 && p3 < 1) {
            "Invalid probabilities. They must be in acceding order and in range [0,1). | p1=$p1, p2=$p2, p3=$p3"
        }
        require(0 < iterations) { "Iteration must be a positive number" }

        //Best known solution
        val best = Solution(initSolution.data, initSolution.solArr.clone())
        //objective value of the best known solution
        var bestObjVal = best.objectiveValue(false)

        //current solution
        val curr = initSolution
        var currObjVal: Int

        for (i in 1..iterations) {
            val rsi = -1.0f//rand.nextFloat()
            val op = when {
                rsi < p1 -> Operator.ReinsertOnceOperator
                rsi < p1 + p2 -> Operator.TwoExchangeOperator
                else -> Operator.TreeExchangeOperator
            }

            //copy the best solution to the current solution
            // this avoids allocating new objects or memory
            best.solArr.copyInto(curr.solArr)

            op.operate(curr)
            currObjVal = curr.objectiveValue(false)

            //update when better
            //TODO do not do the feasibility check here but in the operator
            if (curr.isFeasible() && currObjVal < bestObjVal) {
                curr.solArr.copyInto(best.solArr)
                bestObjVal = currObjVal
            }
        }
        return best
    }


}
