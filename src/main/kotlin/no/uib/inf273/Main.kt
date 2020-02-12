package no.uib.inf273

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import no.uib.inf273.Logger.log
import no.uib.inf273.processor.DataParser
import no.uib.inf273.processor.SolutionGenerator
import no.uib.inf273.search.LocalSearchA3
import no.uib.inf273.search.RandomSearch
import no.uib.inf273.search.Search
import no.uib.inf273.search.SimulatedAnnealingSearch
import kotlin.random.Random
import kotlin.system.measureTimeMillis


class Main(
    parser: ArgParser
) {

    ////////////////////////
    // Required arguments //
    ////////////////////////

    val file by parser.storing("-f", "--file", help = "Name of file to use")

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
        "--search-random-a3" to RandomSearch,
        "--sr3" to RandomSearch,
        "--search-sim-ann-a3" to SimulatedAnnealingSearch,
        "--ssa3" to SimulatedAnnealingSearch,
        help = "What search method to use"
    ).default(Search.NoSearch)

    val seed: Long by parser.storing("The random seed to use") { toLong() }.default(Random.nextLong())

    val benchmark: Boolean by parser.flagging(
        "--benchmark-assignment-3",
        help = "Enable benchmarking as specified in Assignment 3. Search option will be ignored."
    ).default(false)

    val data: DataParser
    val solgen: SolutionGenerator


    init {
        Logger.logLevel = logLevel

        Logger.debug { "Random seed: $seed" }
        rand = Random(seed)

        val content = readInternalFile(file)

        check(!content.isNullOrEmpty()) { "Failed to read file as it is null or empty" }

        data = DataParser(content)
        solgen = SolutionGenerator(data)

        if (benchmark) {
            val result = benchmarkA3()
            log { "Results for instance $file" }
            val defaultObjVal = solgen.generateStandardSolution().objectiveValue(false)
            for ((alg, triple) in result) {
                val (avg, best, ms) = triple
                val improvement = 100 * (defaultObjVal - best) / defaultObjVal
                log { "${alg.javaClass.simpleName} | $avg | $best | $improvement (%) | $ms ms" }
            }
        } else {
            require(search != Search.NoSearch) { "Search method must be specified when no other option is selected." }


            val init = solgen.generateStandardSolution()
            val sol = search.search(solgen.generateStandardSolution())

            check(sol.isFeasible()) { "returned solution is not feasible: ${sol.arr.contentToString()}" }

            check(init !== sol) { "same solution returned" }

            log("")
            log("Searching with ${search.javaClass}")
            log("Best solution using local search is ${sol.arr.contentToString()} | init ${init.arr.contentToString()}")
            log("objective value ${sol.objectiveValue()} | initial obj val ${init.objectiveValue()}")
            log("Improvement ${100 * (init.objectiveValue() - sol.objectiveValue()) / init.objectiveValue()}%")
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

        val repeats = 10

        for (search in listOf(RandomSearch, LocalSearchA3, SimulatedAnnealingSearch)) {
            log { "Running algorithm ${search.javaClass.simpleName}" }
            var totalObj = 0.0
            var bestObj = Int.MAX_VALUE

            val time = measureTimeMillis {
                for (i in 1..repeats) {
                    val sol = search.search(solgen.generateStandardSolution())
                    val objVal = sol.objectiveValue(modified = false)
                    totalObj += objVal
                    if (objVal < bestObj) {
                        bestObj = objVal
                    }
                }
            }
            map[search] = Triple(totalObj / repeats, bestObj, time)
        }

        return map
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
    }
}

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::Main).run { }
}
