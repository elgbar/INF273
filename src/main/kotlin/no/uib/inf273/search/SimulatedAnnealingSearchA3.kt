package no.uib.inf273.search

import no.uib.inf273.Logger
import no.uib.inf273.Logger.debug
import no.uib.inf273.Logger.debugs
import no.uib.inf273.Main
import no.uib.inf273.operator.Operator
import no.uib.inf273.processor.Solution
import java.math.BigDecimal
import kotlin.math.ln
import kotlin.math.pow

object SimulatedAnnealingSearchA3 : Search {


    ///////////////////////
    // Manual parameters //
    ///////////////////////

    var p1: Float = 0.25f
    var p2: Float = 0.5f

    /**
     * How many runs we should do to get an average temperature
     */
    var testRuns = 100_000

    //////////////////////////
    // Automatic parameters //
    //////////////////////////

    /**
     * T0
     */
    private var initTemp: Double = 10.0
        set(value) {
            require(0 < value) { "Temperature must be a positive number" }
            field = value
        }
    /**
     * 𝛼
     */
    private var coolingFactor: Double = 0.01
        set(value) {
            require(0 < value && value < 1) { "The cooling factor must be between 0 and 1" }
            field = value
        }

    override fun search(sol: Solution, iterations: Int): Solution {
        require(0 <= LocalSearchA3.p1 && LocalSearchA3.p1 < LocalSearchA3.p2 && LocalSearchA3.p2 + LocalSearchA3.p1 < 1) {
            "Invalid probabilities. They must be in acceding order and in range [0,1). | p1=${LocalSearchA3.p1}, p2=${LocalSearchA3.p2}"
        }
        require(0 < iterations) { "Iteration must be a positive number" }
        require(sol.isFeasible(true)) { "Initial solution is not feasible" }

        calculateTemp(sol = sol)

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
            change(curr)

            if (curr.isFeasible(modified = true, checkValid = false)) {

                currObjVal = curr.objectiveValue(false)

                //update when better, ∆E = currObjVal - incombentObjVal
                val deltaE = currObjVal - incombentObjVal
                if (deltaE < 0) {

                    curr.arr.copyInto(incombent.arr)
                    incombentObjVal = currObjVal

                    if (currObjVal < bestObjVal) {
                        debug { "New best answer ${best.arr.contentToString()} with objective value $currObjVal. Diff is  ${currObjVal - bestObjVal} " }
                        curr.arr.copyInto(best.arr)
                        bestObjVal = currObjVal
                    }
                } else if (boltzmannProbability(deltaE, temp)) {
                    curr.arr.copyInto(incombent.arr)
                    incombentObjVal = currObjVal
                }
            }
            temp *= coolingFactor

            //copy the best solution to the current solution
            // this avoids allocating new objects or memory
            incombent.arr.copyInto(curr.arr)
        }
        return best
    }

    /**
     * Run a randomly selected operator on the given solution
     */
    private fun change(sol: Solution) {
        val rsi = Main.rand.nextFloat()
        val op = when {
            rsi < LocalSearchA3.p1 -> Operator.TwoExchangeOperator
            rsi < LocalSearchA3.p1 + LocalSearchA3.p2 -> Operator.TreeExchangeOperator
            else -> Operator.ReinsertOnceOperator
        }
        Logger.trace { "Using op ${op.javaClass.simpleName}" }
        op.operate(sol)
    }

    /**
     * Calculate the probability of accepting a worse solution
     */
    private fun boltzmannProbability(deltaE: Int, temp: Double): Boolean {
        return Main.rand.nextDouble() < Math.E.pow(-deltaE / temp)
    }

    /**
     * @param pMin Minimum temperature in range `[0, `[pMax]`)`
     * @param pMax Maximum temperature in range `(`[pMin]`, 1]`
     */
    fun calculateTemp(pMin: Double = 0.01, pMax: Double = 0.8, sol: Solution) {
        require(0 < pMin && pMin < pMax)
        require(pMax <= 1)

        val solObj = sol.objectiveValue(false)
        var minObj = solObj
        var maxObj = solObj
        var totalObj: BigDecimal = BigDecimal.ZERO
        var feasibleRuns = 0

        val curr = Solution(sol.data, sol.arr.copyOf())

        for (i in 0 until testRuns) {
            do {
                sol.arr.copyInto(curr.arr)
                change(curr)
            } while (!curr.isFeasible(modified = true, checkValid = false) && !curr.arr.contentEquals(sol.arr))

            val objVal = curr.objectiveValue(false)
            if (objVal < minObj) minObj = objVal
            if (objVal > maxObj) maxObj = objVal
            totalObj = totalObj.add(objVal.toBigDecimal())
            feasibleRuns++
        }

        val avgObj = totalObj.div(feasibleRuns.toBigDecimal())
        val deltaE = solObj - avgObj.toDouble()
        check(deltaE > 0) { "deltaE = $deltaE" }

        initTemp = -deltaE / ln(pMax)

        debugs {
            listOf(
                "Calculating temp results",
                "",
                "Minimum objective value $minObj",
                "Maximum objective value $maxObj",
                "Average objective value $avgObj ($totalObj / $feasibleRuns)",
                "",
                "Cooling schedule factor $coolingFactor",
                "Initial temperature     $initTemp",
                "",
                "Test values",
                "pMax = $pMax | pMin = $pMin"
            )
        }
    }
}
