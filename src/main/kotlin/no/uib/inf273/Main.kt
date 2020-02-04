package no.uib.inf273

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import no.uib.inf273.processor.DataHolder
import no.uib.inf273.processor.SolutionGenerator


class Main(parser: ArgParser) {


    private val logLevel by parser.mapping(
        "--info" to Logger.INFO,
        "--debug" to Logger.DEBUG,
        "--trace" to Logger.TRACE,
        help = "Logging level"
    ).default(Logger.INFO)

    val file by parser.storing("-f", "--file", help = "Name of file to use")
    val data: DataHolder
    val solgen: SolutionGenerator

    init {
        Logger.logLevel = logLevel

        val content = readInternalFile(file)

        check(!content.isNullOrEmpty()) { "Failed to read file as it is null or empty" }

        data = DataHolder(content)
        solgen = SolutionGenerator(data)
    }


    /**
     * Read an internal file to text
     *
     * @return The content of the file or `null` if the file cannot be read
     */
    private fun readInternalFile(path: String): String? {
        return Main::class.java.classLoader.getResource(path)?.readText()
    }

    companion object {
        const val FALLBACK_FILE = "Call_7_Vehicle_3.txt"
    }
}

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::Main).run { }
}
