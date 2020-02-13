package no.uib.inf273.search

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.operator.Operator
import no.uib.inf273.processor.Solution
import kotlin.math.pow

object SimulatedAnnealingSearchA3 : Search {

    var p1: Float = 0.25f
    var p2: Float = 0.5f

    /**
     * T0
     */
    var initTemp = 0
    /**
     * 𝛼
     */
    var coolingFactor = 0

    override fun search(sol: Solution, iterations: Int): Solution {
        require(0 <= LocalSearchA3.p1 && LocalSearchA3.p1 < LocalSearchA3.p2 && LocalSearchA3.p2 + LocalSearchA3.p1 < 1) {
            "Invalid probabilities. They must be in acceding order and in range [0,1). | p1=${LocalSearchA3.p1}, p2=${LocalSearchA3.p2}"
        }
        require(0 < iterations) { "Iteration must be a positive number" }
        require(sol.isFeasible(true)) { "Initial solution is not feasible" }

        //Best known solution
        val best = Solution(sol.data, sol.arr.clone())
        var bestObjVal = best.objectiveValue(false) //objective value of the best known solution

        //current solution
        val curr = Solution(sol.data, sol.arr.clone())
        var currObjVal: Int

        val incombent = Solution(sol.data, sol.arr.clone())
        var incombentObjVal: Int = incombent.objectiveValue(false)

        var temp = initTemp

        for (i in 0 until iterations) {
            val rsi = Main.rand.nextFloat()
            val op = when {
                rsi < LocalSearchA3.p1 -> Operator.TwoExchangeOperator
                rsi < LocalSearchA3.p1 + LocalSearchA3.p2 -> Operator.TreeExchangeOperator
                else -> Operator.ReinsertOnceOperator
            }

            Logger.trace { "Using op ${op.javaClass.simpleName}" }

            //copy the best solution to the current solution
            // this avoids allocating new objects or memory
            incombent.arr.copyInto(curr.arr)

            op.operate(curr)

            if (curr.isFeasible(modified = false, checkValid = false)) {

                currObjVal = curr.objectiveValue(false)

                //update when better, ∆E = currObjVal - incombentObjVal
                val deltaE = currObjVal - incombentObjVal
                if (deltaE < 0) {

                    //incombent ⟸ 𝑁𝑒𝑤𝑆𝑜𝑙𝑢𝑡𝑖𝑜𝑛
                    curr.arr.copyInto(incombent.arr)
                    incombentObjVal = currObjVal

                    if (currObjVal < bestObjVal) {
                        Logger.debug { "New best answer ${best.arr.contentToString()} with objective value $currObjVal. Diff is  ${currObjVal - bestObjVal} " }
                        curr.arr.copyInto(best.arr)
                        bestObjVal = currObjVal
                    }
                } else if (boltzmannProbability(deltaE, temp)) {
                    curr.arr.copyInto(incombent.arr)
                    incombentObjVal = currObjVal
                }
            }
            temp *= coolingFactor
        }
        return best
    }

    /**
     * Calculate the probability of accepting a worse solution
     */
    private fun boltzmannProbability(deltaE: Int, temp: Int): Boolean {
        return Main.rand.nextDouble() < Math.E.pow(-deltaE / temp)
    }
}
