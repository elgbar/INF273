package no.uib.inf273

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import no.uib.inf273.processor.DataParser
import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator
import no.uib.inf273.search.A5
import no.uib.inf273.search.Algorithm
import no.uib.inf273.search.given.LocalAlgorithmA3
import no.uib.inf273.search.given.RandomAlgorithm
import no.uib.inf273.search.given.simulatedAnnealing.SimulatedAnnealingAlgorithmA3
import no.uib.inf273.search.given.simulatedAnnealing.SimulatedAnnealingAlgorithmA4
import java.math.BigDecimal
import kotlin.random.Random
import kotlin.system.measureTimeMillis


class Main(
    parser: ArgParser
) {

    ////////////////////////
    // Required arguments //
    ////////////////////////

    private val filePath: String by parser.storing(
        "-f",
        "--file",
        help = "Name of file to use."
    )

    ////////////////////////
    // Optional arguments //
    ////////////////////////

    private val logLevel by parser.mapping(
        "--info" to Logger.INFO,
        "--debug" to Logger.DEBUG,
        "--trace" to Logger.TRACE,
        help = "Logging level"
    ).default(Logger.INFO)

    private val algorithm: Algorithm by parser.mapping(
        "--search-local-a3" to LocalAlgorithmA3,
        "--sl3" to LocalAlgorithmA3,
        "--search-random" to RandomAlgorithm,
        "--sr" to RandomAlgorithm,
        "--search-simulated-annealing-a3" to SimulatedAnnealingAlgorithmA3,
        "--ssa3" to SimulatedAnnealingAlgorithmA3,
        "--search-simulated-annealing-a4" to SimulatedAnnealingAlgorithmA4,
        "--ssa4" to SimulatedAnnealingAlgorithmA4,
        "--a5" to A5,
        help = "What search algorithm to use"
    ).default(Algorithm.NoAlgorithm)

    private val seed: Long by parser.storing("The random seed to use") { toLong() }.default(Random.nextLong())

    private val benchmark: Boolean by parser.flagging(
        "--benchmark-assignment-3",
        help = "Enable benchmarking as specified in Assignment 3. Search option will be ignored."
    ).default(false)

    private val tune: Boolean by parser.flagging(
        "--tune",
        help = "Use and print the best calculated parameters. Note that a tuning is a VERY heavy operation and will take a long time."
    )

    private val samples: Int by parser.storing("How many samples to take") { toInt() }.default { 10 }
    private val iterations: Int by parser.storing("How many iteration each algorithm use") { toInt() }
        .default { 10_000 }

    private val showSolution: Boolean by parser.flagging("--show-solution", help = "Show the best generated solution")

    // Generated variables //

    val data: DataParser
    private val solgen: SolutionGenerator

    init {
        log.logLevel = logLevel
        if (!benchmark && logLevel != Logger.INFO) {
            algorithm.updateLogLevel(logLevel)
        }

        log.log("Random seed = $seed")
        rand = Random(seed)

        val content = readInternalFile(filePath)

        check(!content.isNullOrEmpty()) { "Failed to read file as it is null or empty" }

        data = DataParser(content)
        solgen = SolutionGenerator(data)

        if (benchmark) {
            val result = benchmarkA3()
            log.log { "Results for instance $filePath" }

            for ((alg, triple) in result) {
                printResults(alg, triple, true)
            }
        } else {
            require(algorithm != Algorithm.NoAlgorithm) { "Search method must be specified when no other option is selected." }
            val time = measureTimeMillis {
                printResults(algorithm, runAlgorithm(algorithm, samples, solgen, tune, iterations), false)
            }
            log.log("Running $samples samples took in total $time ms")
        }
    }

    /**
     * Benchmark Local search, random search and simulated annealing as specified in assignment 3.
     *
     * Note that this is only done for the specified instance and not for all five.
     *
     * @return A map of the search mapping to average obj value, best obj val, then running time in ms
     */
    private fun benchmarkA3(): Map<Algorithm, Triple<Double, Solution, Long>> {
        val map: MutableMap<Algorithm, Triple<Double, Solution, Long>> = HashMap()
        log.log { "Benchmark Assignment 3 " }

        val totalTime = measureTimeMillis {
            for (algorithm in listOf(
                RandomAlgorithm, LocalAlgorithmA3,
                SimulatedAnnealingAlgorithmA3
            )) {
                log.log { "Running ${algorithm.javaClass.simpleName}" }
                map[algorithm] = runAlgorithm(algorithm, 10, solgen, tune, 10_000)
            }
        }
        log.log("Total benchmarking time took $totalTime ms")
        return map
    }


    private fun printResults(algorithm: Algorithm, result: Triple<Double, Solution, Long>, singleLine: Boolean) {

        val (avgObjVal, best, time) = result
        val defaultObjVal = solgen.generateStandardSolution().objectiveValue(false).toDouble().toBigDecimal()
        val bestObjVal = best.objectiveValue(true)

        val improvementAvg =
            100.0.toBigDecimal() * (defaultObjVal - avgObjVal.toBigDecimal()) / defaultObjVal
        val improvementBest =
            100.0.toBigDecimal() * (defaultObjVal - bestObjVal.toDouble().toBigDecimal()) / defaultObjVal

        if (singleLine) {
            log.log { "${algorithm.javaClass.simpleName}, $avgObjVal, $bestObjVal, $improvementBest%, $time ms, ${best.arr.contentToString()}" }
        } else {
            log.logs {
                listOf(
                    "Searching with algorithm. . . $algorithm",
                    "File. . . . . . . . . . . . . $filePath",
                    "Initial objective value . . . $defaultObjVal",
                    "Best objective value. . . . . $bestObjVal",
                    "Average objective value . . . $avgObjVal",
                    "Improvement (best). . . . . . $improvementBest%",
                    "Improvement (avg) . . . . . . $improvementAvg%",
                    "Diff improvement (best-avg) . ${improvementBest - improvementAvg}%",
                    "Average time. . . . . . . . . ${time / 1000.0} seconds"
                )
            }
            if (showSolution) {
                log.log(best.arr.contentToString())
            }
        }
    }

    companion object {

        /**
         * Global logger
         */
        val log = Logger("Main")

        lateinit var rand: Random

        /**
         * Read an internal file to text
         *
         * @return The content of the file or `null` if the file cannot be read
         */
        fun readInternalFile(path: String): String? {
            return Main::class.java.classLoader.getResource(path)?.readText()
        }

        /**
         * Run an algorithm [samples] times and report back results.
         *
         * @return A triple with values in order: average objective value, best solution, average time in milliseconds rounded down
         */
        fun runAlgorithm(
            algorithm: Algorithm,
            samples: Int,
            solgen: SolutionGenerator,
            tune: Boolean,
            iterations: Int
        ): Triple<Double, Solution, Long> {
            log.debug { "Running algorithm ${algorithm.javaClass.simpleName}" }
            var totalObj = BigDecimal.ZERO
            var bestObj = Long.MAX_VALUE
            var times = 0L

            if (tune) {
                algorithm.tune(solgen, samples, true)
            }

            var best = solgen.generateStandardSolution()

            for (i in 0 until samples) {
                var sol0: Solution? = null
                val time = measureTimeMillis {
                    sol0 = algorithm.search(solgen.generateStandardSolution(), iterations)
                }
                val sol: Solution = sol0!!
                times += time

                check(sol.isFeasible(true)) {
                    "returned solution (using ${algorithm.javaClass.simpleName}) is not feasible: ${sol.arr.contentToString()}"
                }
                val objVal = sol.objectiveValue(true)
                totalObj += objVal.toBigDecimal()
                if (objVal < bestObj) {
                    bestObj = objVal
                    best = sol
                }
            }

            check(best.isFeasible(true)) {
                "best not feasible"
            }

            return Triple((totalObj / samples.toBigDecimal()).toDouble(), best, times / samples)
        }
    }
}

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::Main).run { }
}
