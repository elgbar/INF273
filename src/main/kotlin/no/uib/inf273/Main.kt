package no.uib.inf273

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
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
import java.io.File
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
        "-f", "--file",
        help = "Name of file to use."
    ).default(FILE_C007_V03)

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
        help = "Search algorithm to use"
    ).default(Algorithm.NoAlgorithm)

    private val seed: Long by parser.storing("The random seed to use") { toLong() }.default(Random.nextLong())

    private val benchmarkA3: Boolean by parser.flagging(
        "--benchmark-assignment-3",
        help = "Enable benchmarking as specified in Assignment 3. Search option will be ignored."
    ).default(false).addValidator {
        if (benchmarkA3 && benchmarkA5) throw InvalidArgumentException(
            "Cannot benchmark A3 with A5 benchmark enabled"
        )
    }

    private val benchmarkA5: Boolean by parser.flagging(
        "--benchmark-assignment-5",
        help = "Enable benchmarking as specified in Assignment 5. Search option will be ignored."
    ).default(false).addValidator {
        if (benchmarkA5 && benchmarkA3) throw InvalidArgumentException(
            "Cannot benchmark A5 with A3 benchmark enabled"
        )
    }

    private val tune: Boolean by parser.flagging(
        "--tune",
        help = "Use and print the best calculated parameters. Note that a tuning is a VERY heavy operation and will take a long time."
    )

    private val samples: Int by parser.storing("How many samples to take") { toInt() }.default { 10 }
    private val time: Int by parser.storing("Max time in seconds to use, default is ${Int.MAX_VALUE} aka Int.MAX_VALUE") { toInt() }
        .default { Int.MAX_VALUE }
    private val iterations: Int by parser.storing("How many iteration each algorithm use") { toInt() }
        .default { 10_000 }

    private val showSolution: Boolean by parser.flagging("--show-solution", help = "Show the best generated solution")
    private val singleLine: Boolean by parser.flagging("--single-line", help = "Show result on a single line")

    private var benchmark = benchmarkA3 || benchmarkA5

    // Generated variables //

    var data: DataParser
        private set
    private var solgen: SolutionGenerator

    init {
        log.logLevel = logLevel
        if (!benchmarkA3 && logLevel != Logger.INFO) {
            algorithm.updateLogLevel(logLevel)
        }

        if (time != Int.MAX_VALUE) {
            when (algorithm) {
                A5 -> A5.maxTimeSeconds = this.time
                else -> error("Algorithm $algorithm does not support maximum time")
            }
        }

        log.log("Random seed = $seed")
        rand = Random(seed)

        val content = readFile(filePath)

        check(!content.isNullOrEmpty()) { "Failed to read file as it is null or empty. File path $filePath" }

        data = DataParser(content)
        solgen = SolutionGenerator(data)

        when {
            benchmarkA3 -> benchmarkA3()
            benchmarkA5 -> benchmarkA5()
            else -> {
                require(algorithm != Algorithm.NoAlgorithm) { "Search method must be specified when no other option is selected." }
                val time = measureTimeMillis {
                    printResults(algorithm, runAlgorithm(algorithm, samples, solgen, tune, iterations), singleLine)
                }
                log.log("Running $samples samples took in total $time ms")
            }
        }
    }

    /**
     * Benchmark Local search, random search and simulated annealing as specified in assignment 3.
     *
     * Note that this is only done for the specified instance and not for all five.
     *
     * @return A map of the search mapping to average obj value, best obj val, then running time in ms
     */
    private fun benchmarkA3() {
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
        log.log { "Results for instance $filePath" }

        for ((alg, triple) in map) {
            printResults(alg, triple, true)
        }
    }

    private fun benchmarkA5() {
        log.log { "Benchmark Assignment 5" }

        val totalTime = measureTimeMillis {
            for (file in listOf(FILE_C007_V03, FILE_C018_V05, FILE_C035_V07, FILE_C080_V20, FILE_C130_V40)) {

                val content = readFile(file)
                data = DataParser(content)
                solgen = SolutionGenerator(data)

                val iter = data.nrOfCargo * data.nrOfCargo * 100
                A5.maxTimeSeconds = (data.nrOfCargo * 2.2).toInt()

                log.log { "Running file $file with $iter iterations (max ${A5.maxTimeSeconds} seconds)" }

                val triple = runAlgorithm(A5, samples, solgen, tune, iter)
                printResults(A5, triple, true)
            }
        }
        log.log { "Total benchmarking time took $totalTime ms" }
        log.log { "Results for instance $filePath" }


    }

    private fun printResults(algorithm: Algorithm, result: Triple<Double, Solution, Long>, singleLine: Boolean) {

        val (avgObjVal, best, time) = result
        val defaultObjVal = solgen.generateStandardSolution().objectiveValue(false).toDouble().toBigDecimal()
        val bestObjVal = best.objectiveValue(true).toDouble()

        val improvementAvg =
            100.0.toBigDecimal() * (defaultObjVal - avgObjVal.toBigDecimal()) / defaultObjVal
        val improvementBest =
            100.0.toBigDecimal() * (defaultObjVal - bestObjVal.toBigDecimal()) / defaultObjVal

        if (singleLine) {
            log.log { "${algorithm.javaClass.simpleName}, ${avgObjVal.toLong()}, ${bestObjVal.toLong()}, $improvementBest%, ${time / 1000.0} seconds, ${best.arr.contentToString()}" }
        } else {
            log.logs {
                listOf(
                    "Searching with algorithm. . . $algorithm",
                    "File. . . . . . . . . . . . . $filePath",
                    "Initial objective value . . . $defaultObjVal",
                    "Best objective value. . . . . ${bestObjVal.toLong()}",
                    "Average objective value . . . ${avgObjVal.toLong()}",
                    "Improvement (best). . . . . . $improvementBest%",
                    "Improvement (avg) . . . . . . $improvementAvg%",
                    "Diff improvement (best-avg) . ${improvementBest - improvementAvg}%",
                    "Average time. . . . . . . . . ${time / 1000.0} seconds"
                )
            }
            if (showSolution || benchmark) {
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
        fun readFile(path: String): String {
            val content = Main::class.java.classLoader.getResource(path)?.readText() ?: File(path).readText()
            check(!content.isBlank()) { "Failed to read file '$path' as it is null or empty" }
            return content
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

const val FILE_C007_V03 = "Call_7_Vehicle_3.txt"
const val FILE_C018_V05 = "Call_18_Vehicle_5.txt"
const val FILE_C035_V07 = "Call_035_Vehicle_07.txt"
const val FILE_C080_V20 = "Call_080_Vehicle_20.txt"
const val FILE_C130_V40 = "Call_130_Vehicle_40.txt"

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::Main).run { }
}
