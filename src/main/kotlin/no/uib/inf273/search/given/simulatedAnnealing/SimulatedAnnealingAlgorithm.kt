package no.uib.inf273.search.given.simulatedAnnealing

import no.uib.inf273.Main
import no.uib.inf273.operators.Operator
import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator
import no.uib.inf273.search.Algorithm
import java.math.BigDecimal
import kotlin.math.exp
import kotlin.math.ln
import kotlin.system.measureTimeMillis

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
     * How many runs we should do to get an average temperature
     */
    var testRuns = 100

    override fun updateLogLevel(level: Int) {
        super.updateLogLevel(level)

        log.debug { "Updating operators ${ops.map { it.second }} and fallback $fallbackOp logging level to $level" }
        //make all ops the same logging level as the algorithm
        for ((_, op) in ops) {
            op.log.logLevel = log.logLevel
        }
        fallbackOp.log.logLevel = log.logLevel
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

    val opTimes = HashMap<Operator, Pair<Double, Int>>()

    private fun change(sol: Solution) {
        val op = findOperator()
        log.debug { "Using op $op" }
        val time = measureTimeMillis {
            op.operate(sol)
        }

        val (oldTime, oldCount) = (opTimes[op] ?: 0.0 to 0)
        opTimes[op] = oldTime + time to oldCount + 1

        log.debug { "Took $time ms to operate" }
        if (log.isDebugEnabled()) {
            check(sol.isFeasible(modified = true, checkValid = true)) {
                "Solution no long feasible after using operator ${op.javaClass.simpleName}"
            }
        }
    }

    override fun search(sol: Solution, iterations: Int): Solution {
        require(0 < iterations) { "Iteration must be a positive number" }
        require(sol.isFeasible(true)) { "Initial solution is not feasible" }

        val (initTemp, coolingFactor) = calculateParameters(
            sol = sol,
            iterations = iterations,
            testRuns = testRuns,
            change = this::change
        )
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
                    )} ${worse.format("worse")} ${accWorse.format("acceptedWorse")} (${"%.2f".format(accWorse.toDouble() / worse.toDouble() * 100.0)}%)"
                }
                identical = 0
                better = 0
                worse = 0
                sameObjVal = 0
                accWorse = 0
            }

//            if (curr.arr.contentEquals(best.arr)) {
//                identical++
//                continue
//            }

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

        log.logs {
            val maxOpName = opTimes.keys.map { it.toString().length }.max()
            val formatStr = "%-${maxOpName}s"
            opTimes.mapValues { (_, it) ->
                val (time, count) = it
                val avgTime = if (count == 0) 0.0
                else time / count
                avgTime to count
            }.toList().sortedByDescending {
                it.second.first
            }.mapTo(ArrayList()) { (op, time) ->
                "${formatStr.format(op)} %.3f ms (%4d times)".format(time.first, time.second)
            }.apply {
                add(0, "${formatStr.format("Operator")} Average")
            }
        }
        opTimes.clear()

        return best
    }

    override fun tune(solgen: SolutionGenerator, iterations: Int, report: Boolean) {
        //Calculate the wanted initial temperature
        log.log { "Warming up for tuning" }
        Main.runAlgorithm(this, 5, solgen, false, 10_000)
        log.log { "Warm up done" }

        calculateParameters(
            sol = solgen.generateStandardSolution(),
            iterations = iterations,
            testRuns = testRuns,
            change = this::change
        )
    }

    companion object {

        /**
         * @param pMin Minimum temperature in range `[0, `[pMax]`)`
         * @param pMax Maximum temperature in range `(`[pMin]`, 1]`
         */
        fun calculateParameters(
            pMin: Double = 0.01,
            pMax: Double = 0.80,
            sol: Solution,
            iterations: Int,
            testRuns: Int,
            coolFacDiv: Int = 4,
            change: (Solution) -> Unit
        ): Pair<Double, Double> {
            require(0 < pMin && pMin < pMax && pMax <= 1)

            val solObj = sol.objectiveValue(false)
            var minObj = solObj
            var maxObj = solObj
            var totalObj: BigDecimal = BigDecimal.ZERO
            var feasibleRuns = 0

            val curr = sol.copy()
            val coolingFactor: Double

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
            coolingFactor = calcCoolingFac(coolFacDiv)

            Main.log.debugs {

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
            return initTemp to coolingFactor
        }
    }
}
