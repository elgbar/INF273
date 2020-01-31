package no.uib.inf273

import no.uib.inf273.Logger.debug
import no.uib.inf273.Logger.log
import no.uib.inf273.processor.DataHolder
import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator
import java.io.File
import java.util.*


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


//        val givenData = intArrayOf(3, 3, 0, 7, 1, 7, 1, 0, 5, 5, 0, 2, 2, 4, 4, 6, 6)
//        val sol = Solution(data, givenData)
//        log { "valid?    ${sol.isValid(false)}" }
//        log { "feasible? ${sol.isFeasible(modified = false, checkValid = false)}" }
//        log { "obj value ${sol.objectiveValue(false)}" }

        val r = Random()

        val sol: Solution = solgen.generateRandomSolution()
        while (!sol.isFeasible(checkValid = false)) {
            solgen.generateRandomSolution(rng = r, solution = sol)
            debug { "random valid solution = $sol" }
        }
        log { "random valid & feasible solution = $sol" }
        log { "obj fun = ${sol.objectiveValue(false)}" }
    }


    /**
     * Read an internal file to text
     *
     * @return The content of the file or `null` if the file cannot be read
     */
    private fun readInternalFile(path: String = FALLBACK_FILE): String? {
        return Main::class.java.classLoader.getResource(path)?.readText()
    }
}

fun main(args: Array<String>) {
    Main.init(args)
}
