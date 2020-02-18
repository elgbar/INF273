package no.uib.inf273

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import no.uib.inf273.Logger.debug
import no.uib.inf273.Logger.log
import no.uib.inf273.processor.DataParser
import no.uib.inf273.processor.SolutionGenerator
import no.uib.inf273.search.LocalSearchA3
import no.uib.inf273.search.RandomSearch
import no.uib.inf273.search.Search
import no.uib.inf273.search.SimulatedAnnealingSearchA3
import kotlin.random.Random
import kotlin.system.measureTimeMillis


class Main(
    parser: ArgParser
) {

    ////////////////////////
    // Required arguments //
    ////////////////////////

    val filePath: String by parser.storing(
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

    val search: Search by parser.mapping(
        "--search-local-a3" to LocalSearchA3,
        "--sl3" to LocalSearchA3,
        "--search-random" to RandomSearch,
        "--sr" to RandomSearch,
        "--search-simulated-annealing-a3" to SimulatedAnnealingSearchA3,
        "--ssa3" to SimulatedAnnealingSearchA3,
        help = "What search method to use"
    ).default(Search.NoSearch)

    val seed: Long by parser.storing("The random seed to use") { toLong() }.default(Random.nextLong())

    val benchmark: Boolean by parser.flagging(
        "--benchmark-assignment-3",
        help = "Enable benchmarking as specified in Assignment 3. Search option will be ignored."
    ).default(false)

    val data: DataParser
    val solgen: SolutionGenerator

    val tune: Boolean by parser.flagging(
        "--tune",
        help = "Use and print the best calculated parameters. Note that a tuning is a VERY heavy operation and will take a long time."
    )

    init {
        Logger.logLevel = logLevel

        log("Random seed = $seed")
        rand = Random(seed)

        val content = readInternalFile(filePath)

        check(!content.isNullOrEmpty()) { "Failed to read file as it is null or empty" }

        data = DataParser(content)
        solgen = SolutionGenerator(data)

        if (benchmark) {
            val result = benchmarkA3()
            log { "Results for instance $filePath" }

            for ((alg, triple) in result) {
                printResults(alg, triple, true)
            }
        } else {
            require(search != Search.NoSearch) { "Search method must be specified when no other option is selected." }

            printResults(search, runAlgorithm(search, 10, solgen, tune), false)
        }
    }

    /**
     * Benchmark Local search, random search and simulated annealing as specified in assignment 3.
     *
     * Note that this is only done for the specified instance and not for all five.
     *
     * @return A map of the search mapping to average obj value, best obj val, then running time in ms
     */
    fun benchmarkA3(): Map<Search, Triple<Double, Int, Long>> {

        val map: MutableMap<Search, Triple<Double, Int, Long>> = HashMap()

        for (search in listOf(RandomSearch, LocalSearchA3, SimulatedAnnealingSearchA3)) {
            map[search] = runAlgorithm(search, 10, solgen, tune)
        }
        return map
    }

    fun printResults(search: Search, result: Triple<Double, Int, Long>, singleLine: Boolean) {

        val defaultObjVal = solgen.generateStandardSolution().objectiveValue(false)
        val (avg, best, time) = result
        val improvement = 100 * (defaultObjVal - best) / defaultObjVal

        if (singleLine) {
            log { "${search.javaClass.simpleName}, $avg, $best, $improvement%, $time ms" }
        } else {
            log("Searching with algorithm ${search.javaClass}")
            log("initial obj val $defaultObjVal")
            log("Best obj value  $best")
            log("avg obj value . $avg")
            log("Improvement . . $improvement%")
            log("Time  . . . . . $time ms")

        }
    }

    companion object {

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
         * @return A triple with values in order: average objective value, best objective value, time takes in milliseconds
         */
        fun runAlgorithm(
            search: Search,
            samples: Int,
            solgen: SolutionGenerator,
            tune: Boolean
        ): Triple<Double, Int, Long> {
            debug { "Running algorithm ${search.javaClass.simpleName}" }
            var totalObj = 0.0
            var bestObj = Int.MAX_VALUE

            if (tune) {
                search.tune(solgen, samples, true)
            }

            val time = measureTimeMillis {
                for (i in 0 until samples) {
                    val sol = search.search(solgen.generateStandardSolution())
                    check(sol.isFeasible(true)) { "returned solution is not feasible: ${sol.arr.contentToString()}" }
                    val objVal = sol.objectiveValue(modified = false)
                    totalObj += objVal
                    if (objVal < bestObj) {
                        bestObj = objVal
                    }
                }
            }
            return Triple(totalObj / samples, bestObj, time)
        }
    }
}

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::Main).run { }
}


