package no.uib.inf273

import no.uib.inf273.Logger.debug
import no.uib.inf273.Logger.log
import no.uib.inf273.processor.DataHolder
import no.uib.inf273.processor.Solution
import no.uib.inf273.processor.SolutionGenerator
import java.io.File


object Main {

    const val FALLBACK_FILE = "Call_7_Vehicle_3.txt"

    lateinit var data: DataHolder
    lateinit var solgen: SolutionGenerator

    fun init(args: Array<String>) {

        Logger.logLevel = Logger.LOG

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
//
//        val valid = sol.isValid(false)
//        val feasible = sol.isFeasible(modified = false, checkValid = false)
//        val objFun = sol.objectiveValue(false)
//
//        print("valid? $valid feasible $feasible objfun $objFun")

        var sol: Solution = solgen.generateRandomSolution()
        while (!sol.isFeasible(modified = true, checkValid = false)) {
            sol = solgen.generateRandomSolution()
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
