package no.uib.inf273

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import no.uib.inf273.processor.DataParser
import no.uib.inf273.processor.SolutionGenerator
import no.uib.inf273.search.LocalSearch
import no.uib.inf273.search.RandomSearch
import no.uib.inf273.search.Search
import no.uib.inf273.search.SimulatedAnnealingSearch
import kotlin.random.Random


class Main(parser: ArgParser) {

    private val logLevel by parser.mapping(
        "--info" to Logger.INFO,
        "--debug" to Logger.DEBUG,
        "--trace" to Logger.TRACE,
        help = "Logging level"
    ).default(Logger.INFO)

    val file by parser.storing("-f", "--file", help = "Name of file to use")
    val data: DataParser
    val solgen: SolutionGenerator
    val search: Search by parser.mapping(
        "--search-local" to LocalSearch,
        "--sl" to LocalSearch,
        "--search-random" to RandomSearch,
        "--sr" to RandomSearch,
        "--search-sim-ann" to SimulatedAnnealingSearch,
        "--ssa" to SimulatedAnnealingSearch,
        help = "What search method to use"
    )

    val seed: Long by parser.storing("The random seed to use") { toLong() }.default(Random.nextLong())


    init {
        Logger.logLevel = logLevel

        Logger.debug { "Random seed: $seed" }
        rand = Random(seed)

        val content = readInternalFile(file)

        check(!content.isNullOrEmpty()) { "Failed to read file as it is null or empty" }

        data = DataParser(content)
        solgen = SolutionGenerator(data)

        search.search(solgen.generateStandardSolution())

    }

    companion object {

        lateinit var rand: Random
            private set

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
