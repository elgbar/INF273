package no.uib.inf273.search.given.simulatedAnnealing

import no.uib.inf273.Main
import no.uib.inf273.operators.Operator
import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator
import no.uib.inf273.search.Algorithm
import java.math.BigDecimal
import kotlin.math.exp
import kotlin.math.ln
import kotlin.random.Random

/**
 * @author Elg
 */
abstract class SimulatedAnnealingAlgorithm(
    private vararg val ops: Pair<Double, Operator>,
    private val fallbackOp: Operator
) : Algorithm() {

    init {
        var lastProp = 0.0
        for ((prob, op) in ops) {
            require(lastProp <= prob && prob < 1.0) {
                "Invalid probability $prob for operator $op. Operators must be in acceding order and in range [0,1), last has value of $lastProp. | ops = ${ops.contentToString()}"
            }
            lastProp = prob
        }
    }

    /**
     * 𝛼
     */
    private var coolingFactor: Double = 1.0 - Double.MIN_VALUE
        set(value) {
            require(0 < value && value < 1) { "The cooling factor must be between 0 and 1 got $value" }
            field = value
        }

    /**
     * How many runs we should do to get an average temperature
     */
    var testRuns = 100

    override fun updateLogLevel(level: Int) {
        super.updateLogLevel(level)
        //make all ops the same logging level as the algorithm
        for ((_, op) in ops) {
            op.log.logLevel = log.logLevel
        }
    }

    private fun findOperator(): Operator {
        val percent: Double = Main.rand.nextDouble()
        for ((prob, op) in ops) {
            if (percent < prob) {
                return op
            }
        }
        return fallbackOp
    }

    private fun change(sol: Solution) {
        val op = findOperator()
        log.debug { "Using op $op" }
        op.operate(sol)
        if (log.isDebugEnabled()) {
            check(sol.isFeasible(modified = true, checkValid = true)) {
                "Solution no long feasible after using operator ${op.javaClass.simpleName}"
            }
        }
    }

    override fun search(sol: Solution, iterations: Int): Solution {
        require(0 < iterations) { "Iteration must be a positive number" }
        require(sol.isFeasible(true)) { "Initial solution is not feasible" }

        val initTemp = calculateTemp(sol = sol, iterations = iterations)
        log.debugs {
            listOf(
                "Initial temperature $initTemp",
                "Cooling factor $coolingFactor"
            )
        }

        //Best known solution
        val best = sol.copy()
        var bestObjVal = best.objectiveValue(false) //objective value of the best known solution

        //current solution
        val curr = sol.copy()
        var currObjVal: Long

        val incombent = sol.copy()
        var incombentObjVal = incombent.objectiveValue(false)

        var temp = initTemp

        var identical = 0
        var sameObjVal = 0
        var better = 0
        var worse = 0
        var accWorse = 0

        log.debug { "---" }
        log.log { "identical sameObjVal better worse acceptedWorse" }


        /**
         * Calculate the probability of accepting a worse solution
         */
        fun boltzmannProbability(deltaE: Long, temp: Double): Boolean {
            return Main.rand.nextDouble() < exp(-deltaE / temp)
        }

        for (i in 1..iterations) {
            change(curr)

            if (i % 1000 == 0) {
                fun Int.format(nameLen: String) = "%${nameLen.length}s".format(this)

                log.log {
                    "${identical.format("identical")} ${sameObjVal.format("sameObjVal")} ${better.format(
                        "better"
                    )} ${worse.format("worse")} ${accWorse.format("acceptedWorse")}"
                }
                identical = 0
                better = 0
                worse = 0
                sameObjVal = 0
                accWorse = 0
            }

            if (curr.arr.contentEquals(best.arr)) {
                identical++
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
                    log.trace { "New best answer with objective value $currObjVal. Diff ${currObjVal - bestObjVal} " }
                    curr.arr.copyInto(best.arr)
                    bestObjVal = currObjVal
                }
            } else if (deltaE > 0) {
                worse++

//                println("$i,${exp(-deltaE / temp)}")
                if (boltzmannProbability(deltaE, temp)) {
                    accWorse++
                    log.trace { "Accepted worse solution $currObjVal. Diff ${currObjVal - bestObjVal} " }
                    curr.arr.copyInto(incombent.arr)
                    incombentObjVal = currObjVal
                }
            } else {
                sameObjVal++
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
    ): Double {
        require(0 < pMin && pMin < pMax && pMax <= 1)

        val solObj = sol.objectiveValue(false)
        var minObj = solObj
        var maxObj = solObj
        var totalObj: BigDecimal = BigDecimal.ZERO
        var feasibleRuns = 0

        val curr = sol.copy()

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


        val initTemp = -deltaE.toDouble() / ln(pMax)
        val endTemp = -deltaE.toDouble() / ln(pMin)
        require(initTemp > 0) { "Initial temperature must be positive" }
        require(endTemp > 0) { "End temperature must be positive" }


        fun calcCoolingFac(div: Int): Double {
            return exp((ln(endTemp) - ln(initTemp)) / (iterations / div))
        }
        coolingFactor = calcCoolingFac(7)


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
        return initTemp
    }

//    fun calcBestP(solgen: SolutionGenerator) {
//        val inc = 0.025
//        val maxp2 = 0.5
//        val samples = 10
//
//        var bestAvg = Pair(0.0 to maxp2, Triple(Double.MAX_VALUE, solgen.generateStandardSolution(), Long.MAX_VALUE))
//        var bestObjVal = Pair(0.0 to maxp2, Triple(Double.MAX_VALUE, solgen.generateStandardSolution(), Long.MAX_VALUE))
//        var bestTime = Pair(0.0 to maxp2, Triple(Double.MAX_VALUE, solgen.generateStandardSolution(), Long.MAX_VALUE))
//
//        SimulatedAnnealingSearchA3.log.logs {
//            listOf(
//                "Calculating best probabilities using increments of $inc and values between 0 and $maxp2 with $samples samples per iterations ",
//                "In total around ${(samples * (maxp2 / inc).pow(2)).toInt()} iterations is expected"
//            )
//        }
//
//        var i = 0
//
//        for (np2 in generateSequence(inc) { if (it < maxp2) it + inc else null }) {
//            //np2 is the outer border for p2. It will be a number between 0 and maxp2
//            // when this is set we need to test all possible values of p1 (which will be between 0 and np2)
//
//            for (np1 in generateSequence(0.0) { if (it + inc < np2 && it + inc + np2 < maxp2) it + inc else null }) {
//
//                //For each p1 and p2 we run a benchmark and log those who are best
////                println("p1 $np1 | p2 $np2")
//                p1 = np1
//                p2 = np2
//
//                //reset the random seed between each check to make it equal
//                Main.rand = Random(1337)
//
//                i += samples
//                val triple = Main.runAlgorithm(this, samples, solgen, false)
//                if (triple.first < bestAvg.second.first) bestAvg = Pair(np1 to np2, triple)
//                if (triple.second.objectiveValue(true) < bestObjVal.second.second.objectiveValue(false)) bestObjVal =
//                    Pair(np1 to np2, triple)
//                if (triple.third < bestTime.second.third) bestTime = Pair(np1 to np2, triple)
//            }
//
//            SimulatedAnnealingSearchA3.log.log { "Calculating probabilities .... ${((np2 / maxp2) * 100).toInt()}% done" }
//        }
//
//        SimulatedAnnealingSearchA3.log.log { "Used $i iterations" }
//
//        SimulatedAnnealingSearchA3.log.logs {
//            listOf(
//                "Best average objective value. . $bestAvg",
//                "Best absolute objective value . $bestObjVal",
//                "Best total time . . . . . . . . $bestTime"
//            )
//        }
//        val (np1, np2) = bestAvg.first
//        p1 = np1
//        p2 = np2
//    }
}
