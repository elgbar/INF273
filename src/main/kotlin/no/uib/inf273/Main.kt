package no.uib.inf273

import no.uib.inf273.processor.DataHolder
import no.uib.inf273.processor.SolutionGenerator
import java.io.File


object Main {

    const val FALLBACK_FILE = "Call_7_Vehicle_3.txt"

    lateinit var data: DataHolder
    lateinit var solgen: SolutionGenerator

    fun init(args: Array<String>) {

        Logger.logLevel = Logger.TRACE

        val content = if (args.isEmpty()) {
            readInternalFile()
        } else {
            val path = args[0]
            File(path).readText()
        }

        check(!content.isNullOrEmpty()) { "Failed to read file as it is null or empty" }

        data = DataHolder(content)
        solgen = SolutionGenerator(data)
    }


    /**
     * Read an internal file to text
     *
     * @return The content of the file or `null` if the file cannot be read
     */
    fun readInternalFile(path: String = FALLBACK_FILE): String? {
        return Main::class.java.classLoader.getResource(path)?.readText()
    }
}

fun main(args: Array<String>) {
    Main.init(args)
}
