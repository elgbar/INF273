package no.uib.inf273.search

import no.uib.inf273.Logger
import no.uib.inf273.Main
import no.uib.inf273.operators.MinimizeFreight
import no.uib.inf273.operators.ReinsertOnceOperator
import no.uib.inf273.operators.TwoExchangeOperator
import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator
import java.math.BigDecimal
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.random.Random

object SimulatedAnnealingSearchA3 : Search {

    override val log: Logger = Logger()

    ///////////////////////
    // Manual parameters //
    ///////////////////////

    var p1 = 0.05
    var p2 = 0.50

    /**
     * How many runs we should do to get an average temperature
     */
    var testRuns = 10000

    //////////////////////////
    // Automatic parameters //
    //////////////////////////

    /**
     * T0
     */
    private var initTemp: Double = 0.0
        set(value) {
            require(0 < value) { "Temperature must be a positive number" }
            field = value
        }

    /**
     * 𝛼
     */
    private var coolingFactor: Double = 0.995
        set(value) {
            require(0 < value && value < 1) { "The cooling factor must be between 0 and 1 got $value" }
            field = value
        }

    override fun search(sol: Solution, iterations: Int): Solution {
        require(p1 in 0.0..p2 && p2 + p1 < 1) {
            "Invalid probabilities. They must be in acceding order and in range [0,1). | p1=${p1}, p2=${p2}"
        }
        require(0 < iterations) { "Iteration must be a positive number" }
        require(sol.isFeasible(true)) { "Initial solution is not feasible" }

        calculateTemp(sol = sol, iterations = iterations)
        log.debugs { listOf("Initial temperature $initTemp", "Cooling factor $coolingFactor") }

        //Best known solution
        val best = Solution(sol.data, sol.arr.clone())
        var bestObjVal = best.objectiveValue(false) //objective value of the best known solution

        //current solution
        val curr = Solution(sol.data, sol.arr.clone())
        var currObjVal: Long

        val incombent = Solution(sol.data, sol.arr.clone())
        var incombentObjVal = incombent.objectiveValue(false)

        var temp = initTemp

        var noChange = 0
        var better = 0
        var worse = 0

        log.debug { "---" }

        for (i in 1..iterations) {
            change(curr)

            if (i % 1000 == 0) {
                log.debug { "$noChange $better $worse" }
                noChange = 0
                better = 0
                worse = 0
            }

            if (curr.arr.contentEquals(best.arr)) {
                noChange++
                continue
            }

            currObjVal = curr.objectiveValue(true)

            //update when better, ∆E = currObjVal - incombentObjVal
            val deltaE = currObjVal - incombentObjVal
            if (deltaE < 0) {

                better++

                curr.arr.copyInto(incombent.arr)
                incombentObjVal = currObjVal

                if (currObjVal < bestObjVal) {
                    log.trace { "New best answer ${best.arr.contentToString()} with objective value $currObjVal. Diff ${currObjVal - bestObjVal} " }
                    curr.arr.copyInto(best.arr)
                    bestObjVal = currObjVal
                }
            } else if (boltzmannProbability(deltaE, temp)) {
                curr.arr.copyInto(incombent.arr)
                incombentObjVal = currObjVal
                worse++
            }

            temp *= coolingFactor

            //copy the best solution to the current solution
            // this avoids allocating new objects or memory
            incombent.arr.copyInto(curr.arr)
        }
        return best
    }

    override fun tune(solgen: SolutionGenerator, iterations: Int, report: Boolean) {
        //Calculate the wanted initial temperature
        log.log { "Warming up for tuning" }
        for (i in 0 until 2) {
            Main.runAlgorithm(this, 10, solgen, false)
        }
        log.log { "Warm up done" }


        calculateTemp(sol = solgen.generateStandardSolution(), iterations = iterations, tune = true)
        calcBestP(solgen)
    }

    /**
     * Run a randomly selected operator on the given solution
     */
    private fun change(sol: Solution) {
        val rsi = Main.rand.nextFloat()
        val op = when {
            rsi < p1 -> TwoExchangeOperator
            rsi < p1 + p2 -> MinimizeFreight
            else -> ReinsertOnceOperator
        }
        log.trace { "Using op ${op.javaClass.simpleName}" }
        op.operate(sol)
        if (log.isDebugEnabled()) {
            check(sol.isFeasible(modified = true, checkValid = true)) {
                "Solution no long feasible after using operator ${op.javaClass.simpleName}"
            }
        }
    }

    /**
     * Calculate the probability of accepting a worse solution
     */
    private fun boltzmannProbability(deltaE: Long, temp: Double): Boolean {
        return Main.rand.nextDouble() < exp(-deltaE / temp)
    }

    /**
     * @param pMin Minimum temperature in range `[0, `[pMax]`)`
     * @param pMax Maximum temperature in range `(`[pMin]`, 1]`
     */
    fun calculateTemp(
        pMin: Double = 0.01,
        pMax: Double = 0.80,
        sol: Solution,
        iterations: Int,
        tune: Boolean = false
    ) {
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
            } while (curr.arr.contentEquals(sol.arr))

            check(curr.isFeasible(false))

            val objVal = curr.objectiveValue(false)
            if (objVal < minObj) minObj = objVal
            if (objVal > maxObj) maxObj = objVal
            totalObj += objVal.toBigDecimal()
            feasibleRuns++
        }

        val avgObj = totalObj / feasibleRuns.toBigDecimal()
        val deltaE = solObj.toBigDecimal() - avgObj
        check(deltaE > BigDecimal.ZERO) { "deltaE = $deltaE" }


        initTemp = -deltaE.toDouble() / ln(pMax)
        val endTemp = -deltaE.toDouble() / ln(pMin)


        fun calcCoolingFac(div: Int): Double {
            return exp((ln(endTemp) - ln(initTemp)) / (iterations / div))
        }
        coolingFactor = calcCoolingFac(8)


        if (tune) {


            log.log { "Calculating best cooling factor when initial temperature is $initTemp" }

            val solgen = SolutionGenerator(sol.data)

            var bestAvg =
                Pair(Int.MAX_VALUE, Triple(Double.MAX_VALUE, solgen.generateStandardSolution(), Long.MAX_VALUE))
            var bestObjVal =
                Pair(Int.MAX_VALUE, Triple(Double.MAX_VALUE, solgen.generateStandardSolution(), Long.MAX_VALUE))
            var bestTime =
                Pair(Int.MAX_VALUE, Triple(Double.MAX_VALUE, solgen.generateStandardSolution(), Long.MAX_VALUE))

            for (step in 1..10) {
                coolingFactor = calcCoolingFac(step)

                log.log { "Calculating temperature .... ${(step / 10)}% done" }

                //reset the random seed between each check to make it equal
                Main.rand = Random(1337)

                val triple = Main.runAlgorithm(this, 10, solgen, false)
                if (triple.first < bestAvg.second.first) bestAvg = Pair(step, triple)
                if (triple.second.objectiveValue(true) < bestObjVal.second.second.objectiveValue(false)) bestObjVal =
                    Pair(step, triple)
                if (triple.third < bestTime.second.third) bestTime = Pair(step, triple)
            }

            //report the findings
            log.logs {
                listOf(
                    "Best average objective value. . $bestAvg",
                    "Best absolute objective value . $bestObjVal",
                    "Best total time . . . . . . . . $bestTime"
                )
            }
            coolingFactor = calcCoolingFac(bestAvg.first)
        }
        log.debugs {

            listOf(
                "Calculating temp results",
                "",
                "Minimum objective value $minObj",
                "Maximum objective value $maxObj",
                "Average objective value $avgObj ($totalObj / $feasibleRuns)",
                "",
                "Cooling schedule factor $coolingFactor",
                "",
                "Test values",
                "pMax = $pMax | pMin = $pMin",
                ""
            )
        }
    }

    fun calcBestCooling(solgen: SolutionGenerator) {
        val inc = 0.05
        val samples = 10

    }

    fun calcBestP(solgen: SolutionGenerator) {
        val inc = 0.025
        val maxp2 = 0.5
        val samples = 10


        var bestAvg = Pair(0.0 to maxp2, Triple(Double.MAX_VALUE, solgen.generateStandardSolution(), Long.MAX_VALUE))
        var bestObjVal = Pair(0.0 to maxp2, Triple(Double.MAX_VALUE, solgen.generateStandardSolution(), Long.MAX_VALUE))
        var bestTime = Pair(0.0 to maxp2, Triple(Double.MAX_VALUE, solgen.generateStandardSolution(), Long.MAX_VALUE))

        log.logs {
            listOf(
                "Calculating best probabilities using increments of $inc and values between 0 and $maxp2 with $samples samples per iterations ",
                "In total around ${(samples * (maxp2 / inc).pow(2)).toInt()} iterations is expected"
            )
        }

        var i = 0

        for (np2 in generateSequence(inc) { if (it < maxp2) it + inc else null }) {
            //np2 is the outer border for p2. It will be a number between 0 and maxp2
            // when this is set we need to test all possible values of p1 (which will be between 0 and np2)

            for (np1 in generateSequence(0.0) { if (it + inc < np2 && it + inc + np2 < maxp2) it + inc else null }) {

                //For each p1 and p2 we run a benchmark and log those who are best
//                println("p1 $np1 | p2 $np2")
                p1 = np1
                p2 = np2

                //reset the random seed between each check to make it equal
                Main.rand = Random(1337)

                i += samples
                val triple = Main.runAlgorithm(this, samples, solgen, false)
                if (triple.first < bestAvg.second.first) bestAvg = Pair(np1 to np2, triple)
                if (triple.second.objectiveValue(true) < bestObjVal.second.second.objectiveValue(false)) bestObjVal =
                    Pair(np1 to np2, triple)
                if (triple.third < bestTime.second.third) bestTime = Pair(np1 to np2, triple)
            }

            log.log { "Calculating probabilities .... ${((np2 / maxp2) * 100).toInt()}% done" }
        }

        log.log { "Used $i iterations" }

        log.logs {
            listOf(
                "Best average objective value. . $bestAvg",
                "Best absolute objective value . $bestObjVal",
                "Best total time . . . . . . . . $bestTime"
            )
        }
        val (np1, np2) = bestAvg.first
        p1 = np1
        p2 = np2
    }
}
